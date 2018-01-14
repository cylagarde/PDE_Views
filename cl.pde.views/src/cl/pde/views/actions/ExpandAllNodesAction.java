package cl.pde.views.actions;

import org.eclipse.jface.viewers.AbstractTreeViewer;

import cl.pde.Activator;
import cl.pde.Images;
import cl.pde.views.Util;

/**
 * The class <b>ExpandAllNodesAction</b> allows to.<br>
 */
public class ExpandAllNodesAction extends AbstractTreeViewerAction
{
  final boolean expand;

  /**
   * Constructor
   * @param treeViewer
   * @param expand true then expand otherwise collapse
   */
  public ExpandAllNodesAction(AbstractTreeViewer treeViewer, boolean expand)
  {
    super(treeViewer);
    this.expand = expand;
    setText(expand? "Expand all" : "Collapse all");
    setToolTipText(expand? "Expand all nodes" : "Collapse all nodes");
    setImageDescriptor(Activator.getImageDescriptor(expand? Images.EXPAND_ALL : Images.COLLAPSE_ALL));
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
      if (expand)
        treeViewer.expandAll();
      else
        treeViewer.collapseAll();
    }
    finally
    {
      Util.setUseCache(false);
      treeViewer.getControl().setRedraw(true);
    }
  }
}
