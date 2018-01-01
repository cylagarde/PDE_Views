package cl.pde.views;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

import cl.pde.Activator;

/**
 * The class <b>NotifyResourceChangeListener</b> allows to.<br>
 */
public class NotifyResourceChangeListener implements IResourceChangeListener
{
  NotifyChangedResourceDeltaVisitor manifestChangedResourceDeltaVisitor = new NotifyChangedResourceDeltaVisitor();
  Set<IResource> resourceSet = new HashSet<>();
  TreeViewer treeViewer;
  IFile file;
  Function<IFile, Object> inputFunction;

  /**
   *
   * @param resource
   */
  public void addResource(IResource resource)
  {
    resourceSet.add(resource);
  }

  /**
   *
   */
  public void clearAllResources()
  {
    resourceSet.clear();
  }

  /**
   * @param object
   */
  public void forEachResource(Consumer<? super IResource> action)
  {
    resourceSet.forEach(action);
  }

  @Override
  public void resourceChanged(IResourceChangeEvent event)
  {
    //
    switch(event.getType())
    {
      case IResourceChangeEvent.POST_CHANGE:
        try
        {
          event.getDelta().accept(manifestChangedResourceDeltaVisitor);
        }
        catch(CoreException e)
        {
          Activator.logError("Error: " + e, e);
        }
        break;
    }
  }

  /**
   * The class <b>NotifyChangedResourceDeltaVisitor</b>.<br>
   */
  class NotifyChangedResourceDeltaVisitor implements IResourceDeltaVisitor
  {
    @Override
    public boolean visit(IResourceDelta delta)
    {
      IResource res = delta.getResource();
      //      System.out.println("POST_CHANGE " + res + " " + delta.getFlags() + "  " + delta.getKind());

      if ((delta.getFlags() & IResourceDelta.CONTENT) != 0)
      {
        if (resourceSet.contains(res))
        {
          System.out.println("res changed " + res + "   must update=" + delta);
          treeViewer.getTree().getDisplay().asyncExec(() -> setUpdated(treeViewer, file, inputFunction));
        }
      }

      //
      return true; // visit the children
    }
  }

  /**
   * @param featureViewer
   * @param file
   * @param inputSupplier
   */
  public void setUpdated(TreeViewer treeViewer, IFile file, Function<IFile, Object> inputFunction)
  {
    this.treeViewer = treeViewer;
    this.file = file;
    this.inputFunction = inputFunction;

    //
    Object input = inputFunction.apply(file);
    if (input == null)
      return;

    clearAllResources();
    addResource(file);

    //
    treeViewer.getTree().setRedraw(false);
    treeViewer.setInput(input);

    treeViewer.expandAll();
    treeViewer.getTree().setRedraw(true);

    Consumer<Object> consumer = o -> {
      if (o instanceof TreeObject)
      {
        TreeObject treeObject = (TreeObject) o;
        if (treeObject.data != null)
        {
          IResource resource = Util.getResource(treeObject.data);
          if (resource != null)
            addResource(resource);
        }
      }
    };
    Util.traverseRoot((ITreeContentProvider) treeViewer.getContentProvider(), input, consumer);
  }

}
