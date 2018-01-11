package cl.pde.views;

import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The class <b>PDESelectionListener</b> allows to.<br>
 */
public class PDESelectionListener implements ISelectionListener
{
  private final TreeViewer treeViewer;
  private final NotifyResourceChangeListener notifyResourceChangeListener;
  private final Predicate<Object> predicate;
  private final Function<Object, Object> inputFunction;
  Object resource;
  long lastTime = -1;

  /**
   * Constructor
   * @param treeViewer
   * @param notifyResourceChangeListener
   * @param predicate
   * @param inputFunction
   */
  public PDESelectionListener(TreeViewer treeViewer, NotifyResourceChangeListener notifyResourceChangeListener, Predicate<Object> predicate, Function<Object, Object> inputFunction)
  {
    this.treeViewer = treeViewer;
    this.notifyResourceChangeListener = notifyResourceChangeListener;
    this.predicate = predicate;
    this.inputFunction = inputFunction;
  }

  @Override
  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
    //    System.out.println("selectionChanged " + part + " " + selection + " " + predicate);
    if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection)
    {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      Object firstElement = structuredSelection.getFirstElement();

      if (predicate.test(firstElement))
      {
        if (firstElement != this.resource || System.currentTimeMillis() - lastTime > 250)
        {
          this.resource = firstElement;
          lastTime = System.currentTimeMillis();

          notifyResourceChangeListener.setUpdated(treeViewer, resource, inputFunction);
        }
      }

      //      if (firstElement instanceof IAdaptable)
      //        firstElement = ((IAdaptable) firstElement).getAdapter(IResource.class);
      //
      //      if (firstElement instanceof IResource)
      //      {
      //        IResource resource = (IResource) firstElement;
      //        if (predicate.test(resource))
      //        {
      //          if (resource != this.resource || System.currentTimeMillis() - lastTime > 250)
      //          {
      //            this.resource = resource;
      //            lastTime = System.currentTimeMillis();
      //
      //            notifyResourceChangeListener.setUpdated(treeViewer, resource, inputFunction);
      //          }
      //        }
      //      }
    }
  }
}
