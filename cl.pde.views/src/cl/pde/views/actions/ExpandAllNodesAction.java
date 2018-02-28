package cl.pde.views.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;

import cl.pde.Images;
import cl.pde.PDEViewActivator;
import cl.pde.views.TreeParent;
import cl.pde.views.Util;

/**
 * The class <b>ExpandAllNodesAction</b> allows to expand all nodes of a treeViewer.<br>
 */
public class ExpandAllNodesAction extends AbstractTreeViewerAction
{
  final boolean expand;
  final boolean actionOnAllNodes;

  /**
   * Constructor
   * @param treeViewer
   * @param expand true then expand otherwise collapse
   * @param actionOnAllNodes true then expand all nodes otherwise only selected nodes
   */
  public ExpandAllNodesAction(AbstractTreeViewer treeViewer, boolean expand, boolean actionOnAllNodes)
  {
    super(treeViewer);
    this.expand = expand;
    this.actionOnAllNodes = actionOnAllNodes;
    setText(expand? (actionOnAllNodes? "Expand all" : "Expand selected node") : (actionOnAllNodes? "Collapse all" : "Collapse selected node"));
    setToolTipText(getText());
    setImageDescriptor(PDEViewActivator.getImageDescriptor(expand? Images.EXPAND_ALL : Images.COLLAPSE_ALL));
  }

  /*
   * @see org.eclipse.jface.action.Action#isEnabled()
   */
  @Override
  public boolean isEnabled()
  {
    if (super.isEnabled())
    {
      IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
      if (selection.isEmpty())
        return actionOnAllNodes;

      // check if node is not expanded
      if (!actionOnAllNodes && expand)
      {
        Object[] items = selection.toArray();
        for(Object item : items)
        {
          if (treeViewer.getExpandedState(item))
            return false;

          // check if node has leaves
          if (item instanceof TreeParent)
          {
            TreeParent parent = (TreeParent) item;
            if (!parent.hasChildren())
              return false;
          }
        }
      }
      return true;
    }
    return false;
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
        {
          boolean result = treeViewer.getInput() == null? true : MessageDialog.openQuestion(treeViewer.getControl().getShell(), "Question", "Expand all nodes can take time.\nDo you want to continue?"); // YES/NO
          if (result)
            treeViewer.expandAll();
        }
        else
          treeViewer.collapseAll();
      }
      else
      {
        IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
        Object[] items = selection.toArray();
        for(Object item : items)
        {
          if (expand)
            treeViewer.expandToLevel(item, AbstractTreeViewer.ALL_LEVELS);
          else
            treeViewer.collapseToLevel(item, AbstractTreeViewer.ALL_LEVELS);
        }
      }
    }
    finally
    {
      Util.setUseCache(false);
      treeViewer.getControl().setRedraw(true);
    }
  }
}
