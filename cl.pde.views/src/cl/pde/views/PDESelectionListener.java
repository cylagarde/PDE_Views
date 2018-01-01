package cl.pde.views;

import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
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
  private final Predicate<IFile> predicate;
  private final Function<IFile, Object> inputFunction;
  IFile file;
  long lastTime = -1;

  /**
   * Constructor
   * @param treeViewer
   * @param notifyResourceChangeListener
   * @param predicate
   * @param inputFunction
   */
  public PDESelectionListener(TreeViewer treeViewer, NotifyResourceChangeListener notifyResourceChangeListener, Predicate<IFile> predicate, Function<IFile, Object> inputFunction)
  {
    this.treeViewer = treeViewer;
    this.notifyResourceChangeListener = notifyResourceChangeListener;
    this.predicate = predicate;
    this.inputFunction = inputFunction;
  }

  @Override
  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
    //        System.out.println("selectionChanged " + part + " " + selection);
    if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection)
    {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      Object firstElement = structuredSelection.getFirstElement();
      if (firstElement instanceof IFile)
      {
        IFile file = (IFile) firstElement;
        if (predicate.test(file))
        {
          if (!file.equals(this.file) || (System.currentTimeMillis() - lastTime) > 250)
          {
            this.file = file;
            lastTime = System.currentTimeMillis();

            notifyResourceChangeListener.setUpdated(treeViewer, file, inputFunction);
          }
        }
      }
    }
  }
}
