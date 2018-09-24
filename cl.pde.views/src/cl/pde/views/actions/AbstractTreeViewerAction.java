package cl.pde.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;

/**
 * The class <b>AbstractTreeViewerAction</b> define abstract action on treeViewer.<br>
 */
public abstract class AbstractTreeViewerAction extends Action
{
  protected final AbstractTreeViewer treeViewer;

  /**
   * Constructor
   * @param treeViewer
   */
  protected AbstractTreeViewerAction(AbstractTreeViewer treeViewer)
  {
    this.treeViewer = treeViewer;
  }
}
