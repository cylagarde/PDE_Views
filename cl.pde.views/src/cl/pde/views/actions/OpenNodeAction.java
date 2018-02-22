package cl.pde.views.actions;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import cl.pde.views.TreeObject;
import cl.pde.views.Util;

/**
 * The class <b>OpenNodeAction</b> allows to open editor for the selected item.<br>
 */
public class OpenNodeAction extends AbstractTreeViewerAction
{
  /**
   * Constructor
   * @param treeViewer
   */
  public OpenNodeAction(AbstractTreeViewer treeViewer)
  {
    super(treeViewer);
  }

  @Override
  public void run()
  {
    ISelection selection = treeViewer.getSelection();
    Object obj = ((IStructuredSelection) selection).getFirstElement();
    if (obj instanceof TreeObject)
    {
      TreeObject treeObject = (TreeObject) obj;
      Util.open(treeObject.data);
    }
  }
}
