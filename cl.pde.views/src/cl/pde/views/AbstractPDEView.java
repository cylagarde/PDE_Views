package cl.pde.views;

import java.util.function.Predicate;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import cl.pde.views.actions.CopyIdToClipboardAction;
import cl.pde.views.actions.CopyTreeToClipboardAction;
import cl.pde.views.actions.ExpandAllNodesAction;
import cl.pde.views.actions.OpenNodeAction;

/**
 * The class <b>AbstractPDEView</b> allows to.<br>
 */
public abstract class AbstractPDEView extends ViewPart
{
  private FilteredTree filteredTree;

  private DrillDownAdapter drillDownAdapter;

  private Action copyIdToClipboardAction;
  private Action getAllAction;
  private Action expandAllNodesAction;
  private Action collapseAllNodesAction;
  private Action expandCurrentNodeAction;
  private Action collapseCurrentNodeAction;
  private Action doubleClickOpenNodeAction;
  private Action copyTreeToClipboardAction;

  private ISelectionListener selectionListener;
  private NotifyResourceChangeListener notifyResourceChangeListener;

  /**
   * The constructor.
   */
  public AbstractPDEView()
  {
  }

  /**
   * This is a callback that will allow us
   * to create the viewer and initialize it.
   */
  @Override
  public void createPartControl(Composite parent)
  {
    NotTreeParentPatternFilter filter = new NotTreeParentPatternFilter();
    filter.canSearchOnElementPredicate = getCanSearchOnElementPredicate();
    filteredTree = new AbstractCheckboxFilteredTree(parent, filter)
    {
      @Override
      protected String[] getCheckboxLabels()
      {
        return AbstractPDEView.this.getCheckboxLabels();
      }
    };
    getTreeViewer().setContentProvider(getViewContentProvider());

    // Create the help context id for the viewer's control
    PlatformUI.getWorkbench().getHelpSystem().setHelp(getTreeViewer().getControl(), getHelpContextId());

    drillDownAdapter = new DrillDownAdapter(getTreeViewer());

    //
    PDEPlugin.getDefault().getLabelProvider().connect(this);
    notifyResourceChangeListener = new NotifyResourceChangeListener();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener(notifyResourceChangeListener, IResourceChangeEvent.POST_CHANGE);

    //
    getTreeViewer().getTree().addDisposeListener(e -> {
      workspace.removeResourceChangeListener(notifyResourceChangeListener);
      PDEPlugin.getDefault().getLabelProvider().disconnect(AbstractPDEView.this);
      PDEPlugin.getDefault().getLabelProvider().dispose();
    });
    getTreeViewer().addTreeListener(new ExpandTreeViewerListener());

    getSite().setSelectionProvider(getTreeViewer());

    makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();
  }

  protected abstract Predicate<Object> getCanSearchOnElementPredicate();

  /**
   * return the help context id
   */
  protected abstract String getHelpContextId();

  /**
   * Return the IContentProvider used by the treeViewer
   */
  protected abstract IContentProvider getViewContentProvider();

  /**
   * Return checkbox labels
   */
  protected abstract String[] getCheckboxLabels();

  /**
   * Return the TreeViewer used by the view
   */
  public final TreeViewer getTreeViewer()
  {
    return filteredTree.getViewer();
  }

  @Override
  public void dispose()
  {
    if (selectionListener != null)
    {
      ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
      selectionService.removePostSelectionListener(selectionListener);
      selectionListener = null;
    }

    super.dispose();
  }

  private void hookContextMenu()
  {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(manager -> fillContextMenu(manager));
    Menu contentMenu = menuMgr.createContextMenu(getTreeViewer().getControl());
    getTreeViewer().getControl().setMenu(contentMenu);
    getSite().registerContextMenu(menuMgr, getTreeViewer());
  }

  /**
   * Fill the context menu
   * @param manager
   */
  protected void fillContextMenu(IMenuManager manager)
  {
    if (copyIdToClipboardAction.isEnabled())
      manager.add(copyIdToClipboardAction);
    if (copyTreeToClipboardAction.isEnabled())
      manager.add(copyTreeToClipboardAction);
    manager.add(new Separator());

    if (expandCurrentNodeAction.isEnabled())
    {
      manager.add(expandCurrentNodeAction);
      manager.add(new Separator());
    }
    else if (collapseCurrentNodeAction.isEnabled())
    {
      manager.add(collapseCurrentNodeAction);
      manager.add(new Separator());
    }

    drillDownAdapter.addNavigationActions(manager);
    // Other plug-ins can contribute there actions here
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
  }

  protected void contributeToActionBars()
  {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
    fillLocalToolBar(bars.getToolBarManager());
  }

  /**
   * Fill the local pull down menu
   * @param manager
   */
  protected void fillLocalPullDown(IMenuManager manager)
  {
    manager.add(expandAllNodesAction);
    manager.add(collapseAllNodesAction);
    manager.add(new Separator());
  }

  /**
   * Fill the local toolbar
   * @param manager
   */
  protected void fillLocalToolBar(IToolBarManager manager)
  {
    manager.add(getAllAction);
    manager.add(new Separator());
    manager.add(expandAllNodesAction);
    manager.add(collapseAllNodesAction);
    manager.add(new Separator());
    drillDownAdapter.addNavigationActions(manager);
  }

  /**
   * Create all actions
   */
  protected void makeActions()
  {
    copyIdToClipboardAction = new CopyIdToClipboardAction(getTreeViewer());
    getAllAction = createAllItemsAction();
    expandAllNodesAction = new ExpandAllNodesAction(getTreeViewer(), true, true);
    expandCurrentNodeAction = new ExpandAllNodesAction(getTreeViewer(), true, false);
    collapseAllNodesAction = new ExpandAllNodesAction(getTreeViewer(), false, true);
    collapseCurrentNodeAction = new ExpandAllNodesAction(getTreeViewer(), false, false);
    copyTreeToClipboardAction = new CopyTreeToClipboardAction(getTreeViewer());

    //
    doubleClickOpenNodeAction = new OpenNodeAction(getTreeViewer());
  }

  /**
   * Create action to get all items
   */
  protected abstract Action createAllItemsAction();

  /**
   * Set input
   * @param input
   */
  public void setInput(Object input)
  {
    filteredTree.getFilterControl().setVisible(true);

    Util.setUseCache(true);

    try
    {
      getTreeViewer().setInput(input);

      // refresh
      notifyResourceChangeListener.refreshWhenResourceChanged(getTreeViewer());
    }
    finally
    {
      Util.setUseCache(false);
    }
  }

  private void hookDoubleClickAction()
  {
    getTreeViewer().addDoubleClickListener(event -> doubleClickOpenNodeAction.run());
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus()
  {
    getTreeViewer().getControl().setFocus();
  }
}
