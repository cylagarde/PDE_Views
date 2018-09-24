package cl.pde.views.actions;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
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

      //
      if (!actionOnAllNodes)
      {
        Object[] items = selection.toArray();

        // check if node is not expanded
        for(Object item : items)
        {
          if (treeViewer.getExpandedState(item) == expand)
            return false;

          // check if node has leaves
          if (item instanceof TreeParent)
          {
            TreeParent parent = (TreeParent) item;
            if (parent.hasChildren())
              return true;
          }
        }
        return false;
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
        if (treeViewer.getInput() != null)
          expandOrCollapseItems(new Object[]{treeViewer.getInput()}, expand? 4 : AbstractTreeViewer.ALL_LEVELS);
      }
      else
      {
        IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
        Object[] items = selection.toArray();
        if (items != null && items.length != 0)
          expandOrCollapseItems(items, expand? 4 : AbstractTreeViewer.ALL_LEVELS);
      }
    }
    finally
    {
      Util.setUseCache(false);
      treeViewer.getControl().setRedraw(true);
    }
  }

  /**
   * @param items
   */
  private void expandOrCollapseItems(Object[] items, int depth)
  {
    if (expand)
    {
      IInputValidator integerValidator = newText -> {
        try
        {
          int level = Integer.parseInt(newText);
          if (level < -1)
            return "depth must be >= -1";
          return null;
        }
        catch(Exception e)
        {
          return e.toString();
        }
      };
      InputDialog inputDialog = new InputDialog(treeViewer.getControl().getShell(), "Expand node", "Enter a depth (-1 -> expand all nodes : can be slow)", String.valueOf(depth), integerValidator);
      if (inputDialog.open() != InputDialog.OK)
        return;
      depth = Integer.parseInt(inputDialog.getValue());
    }

    for(Object item : items)
    {
      if (expand)
        treeViewer.expandToLevel(item, depth);
      else
        treeViewer.collapseToLevel(item, depth);
    }
  }
}
