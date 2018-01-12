package cl.pde.views.search_pde;

import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import cl.pde.Activator;
import cl.pde.views.ExpandTreeViewerListener;
import cl.pde.views.NotTreeParentPatternFilter;
import cl.pde.views.NotifyResourceChangeListener;
import cl.pde.views.PDESelectionListener;
import cl.pde.views.PdeLabelProvider;
import cl.pde.views.Util;
import cl.pde.views.actions.ExpandAllNodesAction;
import cl.pde.views.actions.OpenNodeAction;

/**
 * The class <b>SearchPDEView</b> allows to.<br>
 */
public class SearchPDEView extends ViewPart
{
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "cl.pde.searchPDEView";

  private FilteredTree searchFilteredTree;
  private PatternFilter filter;
  private TreeViewer searchViewer;

  private DrillDownAdapter drillDownAdapter;

  private Action expandAllNodesAction;
  private Action collapseAllNodesAction;
  private Action doubleClickOpenNodeAction;

  private ISelectionListener selectionListener;
  private NotifyResourceChangeListener notifyResourceChangeListener;

  /**
   * The constructor.
   */
  public SearchPDEView()
  {
  }

  /**
   * This is a callback that will allow us
   * to create the viewer and initialize it.
   */
  @Override
  public void createPartControl(Composite parent)
  {
    filter = new NotTreeParentPatternFilter();
    searchFilteredTree = new FilteredTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
    searchViewer = searchFilteredTree.getViewer();
    searchFilteredTree.setBackground(searchViewer.getTree().getBackground());

    drillDownAdapter = new DrillDownAdapter(searchViewer);

    searchViewer.setContentProvider(new SearchPDEViewContentProvider());
    searchViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new PdeLabelProvider()));

    //
    PDEPlugin.getDefault().getLabelProvider().connect(this);
    notifyResourceChangeListener = new NotifyResourceChangeListener();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener(notifyResourceChangeListener, IResourceChangeEvent.POST_CHANGE);

    //
    searchViewer.getTree().addDisposeListener(e -> {
      workspace.removeResourceChangeListener(notifyResourceChangeListener);
      PDEPlugin.getDefault().getLabelProvider().disconnect(SearchPDEView.this);
      PDEPlugin.getDefault().getLabelProvider().dispose();
    });
    searchViewer.addTreeListener(new ExpandTreeViewerListener());

    // Create the help context id for the viewer's control
    PlatformUI.getWorkbench().getHelpSystem().setHelp(searchViewer.getControl(), Activator.PLUGIN_ID + ".productView");

    getSite().setSelectionProvider(searchViewer);

    makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();

    //
    ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();

    Predicate<Object> predicate = resource -> {
      if (resource instanceof IAdaptable)
        resource = ((IAdaptable) resource).getAdapter(IResource.class);

      if (resource instanceof IProject)
        return Util.isValidPlugin((IProject) resource);

      return false;
    };
    Function<Object, Object> inputFunction = resource -> {
      if (resource instanceof IAdaptable)
        resource = ((IAdaptable) resource).getAdapter(IResource.class);

      if (resource instanceof IProject)
        return resource;
      return null;
    };
    selectionListener = new PDESelectionListener(searchViewer, notifyResourceChangeListener, predicate, inputFunction);
    selectionService.addPostSelectionListener(selectionListener);
    selectionListener.selectionChanged(null, selectionService.getSelection());
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
    menuMgr.addMenuListener(manager -> SearchPDEView.this.fillContextMenu(manager));
    Menu menu = menuMgr.createContextMenu(searchViewer.getControl());
    searchViewer.getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, searchViewer);
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
    expandAllNodesAction = new ExpandAllNodesAction(searchViewer, true);
    collapseAllNodesAction = new ExpandAllNodesAction(searchViewer, false);

    //
    doubleClickOpenNodeAction = new OpenNodeAction(searchViewer);
  }

  private void hookDoubleClickAction()
  {
    searchViewer.addDoubleClickListener(event -> doubleClickOpenNodeAction.run());
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus()
  {
    searchViewer.getControl().setFocus();
  }

}
