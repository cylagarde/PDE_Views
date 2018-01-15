package cl.pde.views;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

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
  NotifyChangedResourceDeltaVisitor notifyChangedResourceDeltaVisitor = new NotifyChangedResourceDeltaVisitor();
  Map<IResource, TreeObject> resourceMap = new LinkedHashMap<>();
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
          event.getDelta().accept(notifyChangedResourceDeltaVisitor);
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
        if (resourceMap.containsKey(changedResource))
        {
          TreeObject treeObject = resourceMap.get(changedResource);
          if (treeObject != null)
          {
            //            System.out.println("refresh " + treeObject);

            if (treeObject instanceof TreeParent)
            {
              TreeParent treeParent = (TreeParent) treeObject;
              treeParent.reset();
            }

            treeViewer.getControl().getDisplay().asyncExec(() -> {
              treeViewer.getControl().setRedraw(false);
              try
              {
                Object[] expandedElements = treeViewer.getExpandedElements();

                // refresh node
                treeViewer.refresh(treeObject);

                treeViewer.setExpandedElements(expandedElements);
                expandedElements = treeViewer.getExpandedElements();
              }
              finally
              {
                treeViewer.getControl().setRedraw(true);
              }
            });
          }
        }
      }

      //
      return true; // visit the children
    }
  }

  /**
   * Refresh treeViewer when resource changes
   * @param treeViewer
   */
  public void refreshWhenResourceChanged(TreeViewer treeViewer)
  {
    this.treeViewer = treeViewer;
    resourceMap.clear();

    //
    long time = System.currentTimeMillis();

    // add all resources
    Predicate<Object> predicate = o -> {
      if (o instanceof TreeObject)
      {
        TreeObject treeObject = (TreeObject) o;
        if (treeObject.data != null)
        {
          IResource resource = Util.getResource(treeObject.data);
          if (resource != null)
            resourceMap.put(resource, treeObject);
        }
      }
      return true;
    };
    Util.traverseRoot((ITreeContentProvider) treeViewer.getContentProvider(), treeViewer.getInput(), predicate);

    System.out.println("TIME=" + (System.currentTimeMillis() - time));

    // resourceMap.forEach((key, value) -> System.out.println(key + " " + value));
  }
}
