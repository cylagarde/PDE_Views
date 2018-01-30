package cl.pde.views.actions;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;

import cl.pde.Images;
import cl.pde.PDEViewActivator;
import cl.pde.views.Util;

/**
 * The class <b>ExpandAllNodesAction</b> allows to.<br>
 */
public class ExpandAllNodesAction extends AbstractTreeViewerAction
{
  final boolean expand;
  final boolean actionOnAllNodes;

  /**
   * Constructor
   * @param treeViewer
   * @param expand true then expand otherwise collapse
   */
  public ExpandAllNodesAction(AbstractTreeViewer treeViewer, boolean expand, boolean actionOnAllNodes)
  {
    super(treeViewer);
    this.expand = expand;
    this.actionOnAllNodes = actionOnAllNodes;
    setText(expand? (actionOnAllNodes? "Expand all" : "Expand current node") : (actionOnAllNodes? "Collapse all" : "Collapse current node"));
    setToolTipText(expand? "Expand all nodes" : "Collapse all nodes");
    setImageDescriptor(PDEViewActivator.getImageDescriptor(expand? Images.EXPAND_ALL : Images.COLLAPSE_ALL));
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run()
  {
    treeViewer.getControl().setRedraw(false);
    Util.setUseCache(true);
    try
    {
      if (actionOnAllNodes)
      {
        if (expand)
          treeViewer.expandAll();
        else
          treeViewer.collapseAll();
      }
      else
      {
        Object currentSelection = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
        if (expand)
          treeViewer.expandToLevel(currentSelection, AbstractTreeViewer.ALL_LEVELS);
        else
          treeViewer.collapseToLevel(currentSelection, AbstractTreeViewer.ALL_LEVELS);
      }
    }
    finally
    {
      Util.setUseCache(false);
      treeViewer.getControl().setRedraw(true);
    }
  }
}
