package cl.pde.views.product;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEUIMessages;
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

import cl.pde.Activator;
import cl.pde.views.AbstractCheckboxFilteredTree;
import cl.pde.views.ExpandTreeViewerListener;
import cl.pde.views.NotTreeParentPatternFilter;
import cl.pde.views.NotifyResourceChangeListener;
import cl.pde.views.Util;
import cl.pde.views.actions.ExpandAllNodesAction;
import cl.pde.views.actions.OpenNodeAction;

/**
 * The class <b>ProductView</b> allows to.<br>
 */
public class ProductView extends ViewPart
{
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "cl.pde.productView";

  private FilteredTree productFilteredTree;
  private TreeViewer productViewer;

  private DrillDownAdapter drillDownAdapter;

  private Action expandAllNodesAction;
  private Action collapseAllNodesAction;
  private Action doubleClickOpenNodeAction;

  private ISelectionListener selectionListener;
  private NotifyResourceChangeListener notifyResourceChangeListener;

  /**
   * The constructor.
   */
  public ProductView()
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
    String[] checkboxLabels = {PDEUIMessages.FeatureEditor_ReferencePage_title, PDEUIMessages.FeatureEditor_IncludesPage_title, PDEUIMessages.FeatureEditor_DependenciesPage_title};
    productFilteredTree = new AbstractCheckboxFilteredTree(parent, filter)
    {
      @Override
      protected String[] getCheckboxLabels()
      {
        return checkboxLabels;
      }
    };
    productViewer = productFilteredTree.getViewer();
    productViewer.setContentProvider(new ProductViewContentProvider());

    drillDownAdapter = new DrillDownAdapter(productViewer);

    //
    PDEPlugin.getDefault().getLabelProvider().connect(this);
    notifyResourceChangeListener = new NotifyResourceChangeListener();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener(notifyResourceChangeListener, IResourceChangeEvent.POST_CHANGE);

    //
    productViewer.getTree().addDisposeListener(e -> {
      workspace.removeResourceChangeListener(notifyResourceChangeListener);
      PDEPlugin.getDefault().getLabelProvider().disconnect(ProductView.this);
      PDEPlugin.getDefault().getLabelProvider().dispose();
    });
    productViewer.addTreeListener(new ExpandTreeViewerListener());

    // Create the help context id for the viewer's control
    PlatformUI.getWorkbench().getHelpSystem().setHelp(productViewer.getControl(), Activator.PLUGIN_ID + ".productView");

    getSite().setSelectionProvider(productViewer);

    makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();

    //    //
    //    ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
    //
    //    Predicate<Object> predicate = resource -> {
    //      if (resource instanceof IFile)
    //        return ((IFile) resource).getName().toLowerCase(Locale.ENGLISH).endsWith(".product");
    //      return false;
    //    };
    //    Function<Object, Object> inputFunction = resource -> {
    //      if (resource instanceof IFile)
    //      {
    //        try
    //        {
    //          IFile file = (IFile) resource;
    //          WorkspaceProductModel workspaceProductModel = new WorkspaceProductModel(file, true);
    //          workspaceProductModel.load();
    //
    //          IProduct product = workspaceProductModel.getProduct();
    //          return product;
    //        }
    //        catch(CoreException e)
    //        {
    //          e.printStackTrace();
    //          Activator.logError("Cannot open product file", e);
    //          return null;
    //        }
    //      }
    //      return null;
    //    };
    //    selectionListener = new PDESelectionListener(productViewer, notifyResourceChangeListener, predicate, inputFunction);
    //    selectionService.addPostSelectionListener(selectionListener);
    //    selectionListener.selectionChanged(null, selectionService.getSelection());
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
    menuMgr.addMenuListener(manager -> ProductView.this.fillContextMenu(manager));
    Menu menu = menuMgr.createContextMenu(productViewer.getControl());
    productViewer.getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, productViewer);
  }

  private void contributeToActionBars()
  {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
    fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillLocalPullDown(IMenuManager manager)
  {
    manager.add(expandAllNodesAction);
    manager.add(collapseAllNodesAction);
    manager.add(new Separator());
  }

  private void fillContextMenu(IMenuManager manager)
  {
    manager.add(expandAllNodesAction);
    manager.add(collapseAllNodesAction);
    manager.add(new Separator());
    drillDownAdapter.addNavigationActions(manager);
    // Other plug-ins can contribute there actions here
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
  }

  private void fillLocalToolBar(IToolBarManager manager)
  {
    manager.add(expandAllNodesAction);
    manager.add(collapseAllNodesAction);
    manager.add(new Separator());
    drillDownAdapter.addNavigationActions(manager);
  }

  private void makeActions()
  {
    expandAllNodesAction = new ExpandAllNodesAction(productViewer, true);
    collapseAllNodesAction = new ExpandAllNodesAction(productViewer, false);

    //
    doubleClickOpenNodeAction = new OpenNodeAction(productViewer);
  }

  private void hookDoubleClickAction()
  {
    productViewer.addDoubleClickListener(event -> doubleClickOpenNodeAction.run());
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus()
  {
    productViewer.getControl().setFocus();
  }

  /**
   * @return
   */
  public TreeViewer getProductViewer()
  {
    return productViewer;
  }

  /**
   * @param productModel
   */
  public void setInput(IProductModel productModel)
  {
    Util.setUseCache(true);

    try
    {
      productViewer.setInput(productModel);

      // refresh
      notifyResourceChangeListener.refreshWhenResourceChanged(productViewer);
    }
    finally
    {
      Util.setUseCache(false);
    }
  }
}
