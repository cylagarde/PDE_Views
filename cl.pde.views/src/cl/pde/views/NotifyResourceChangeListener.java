package cl.pde.views;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

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
  Object inputResource;
  Function<Object, Object> inputProviderFunction;

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
      IResource changedResource = delta.getResource();
      //      System.out.println("POST_CHANGE " + res + " " + delta.getFlags() + "  " + delta.getKind());

      if ((delta.getFlags() & IResourceDelta.CONTENT) != 0)
      {
        if (resourceSet.contains(changedResource))
          treeViewer.getTree().getDisplay().asyncExec(() -> setUpdated(treeViewer, inputResource, inputProviderFunction));
      }

      //
      return true; // visit the children
    }
  }

  /**
   * @param treeViewer
   * @param inputResource
   * @param inputProviderFunction
   */
  public void setUpdated(TreeViewer treeViewer, Object inputResource, Function<Object, Object> inputProviderFunction)
  {
    this.treeViewer = treeViewer;
    this.inputResource = inputResource;
    this.inputProviderFunction = inputProviderFunction;

    resourceSet.clear();

    // get input for treeViewer
    Object input = inputProviderFunction.apply(inputResource);
    treeViewer.getTree().setRedraw(false);
    try
    {
      treeViewer.setInput(input);

      treeViewer.expandAll();
    }
    finally
    {
      treeViewer.getTree().setRedraw(true);
    }

    if (inputResource instanceof IResource)
      resourceSet.add((IResource) inputResource);

    // add all resources
    Consumer<Object> consumer = o -> {
      if (o instanceof TreeObject)
      {
        TreeObject treeObject = (TreeObject) o;
        if (treeObject.data != null)
        {
          IResource res = Util.getResource(treeObject.data);
          if (res != null)
            resourceSet.add(res);
        }
      }
    };
    Util.traverseRoot((ITreeContentProvider) treeViewer.getContentProvider(), input, consumer);

    resourceSet.forEach(System.out::println);
  }

}
