package cl.pde.views.launchconfiguration;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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

import cl.pde.Activator;
import cl.pde.views.ExpandTreeViewerListener;
import cl.pde.views.NotTreeParentPatternFilter;
import cl.pde.views.NotifyResourceChangeListener;
import cl.pde.views.Util;
import cl.pde.views.actions.ExpandAllNodesAction;
import cl.pde.views.actions.OpenNodeAction;

/**
 * The class <b>LaunchConfigurationView</b> allows to.<br>
 */
public class LaunchConfigurationView extends ViewPart
{
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "cl.pde.launchConfigurationView";

  private FilteredTree launchConfigurationFilteredTree;
  private TreeViewer launchConfigurationViewer;

  private DrillDownAdapter drillDownAdapter;

  private Action expandAllNodesAction;
  private Action collapseAllNodesAction;
  private Action doubleClickOpenNodeAction;

  private ISelectionListener selectionListener;
  private NotifyResourceChangeListener notifyResourceChangeListener;

  /**
   * The constructor.
   */
  public LaunchConfigurationView()
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
    launchConfigurationFilteredTree = new LaunchConfigurationFilteredTree(parent, filter);
    launchConfigurationViewer = launchConfigurationFilteredTree.getViewer();

    drillDownAdapter = new DrillDownAdapter(launchConfigurationViewer);

    //
    PDEPlugin.getDefault().getLabelProvider().connect(this);
    notifyResourceChangeListener = new NotifyResourceChangeListener();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener(notifyResourceChangeListener, IResourceChangeEvent.POST_CHANGE);

    //
    launchConfigurationViewer.getTree().addDisposeListener(e -> {
      workspace.removeResourceChangeListener(notifyResourceChangeListener);
      PDEPlugin.getDefault().getLabelProvider().disconnect(LaunchConfigurationView.this);
      PDEPlugin.getDefault().getLabelProvider().dispose();
    });
    launchConfigurationViewer.addTreeListener(new ExpandTreeViewerListener());

    // Create the help context id for the viewer's control
    PlatformUI.getWorkbench().getHelpSystem().setHelp(launchConfigurationViewer.getControl(), Activator.PLUGIN_ID + ".launchConfigurationView");

    getSite().setSelectionProvider(launchConfigurationViewer);

    makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();

    //    //
    //    ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
    //
    //    Predicate<Object> predicate = resource -> {
    //      if (resource instanceof IFile)
    //        return ((IFile) resource).getName().toLowerCase(Locale.ENGLISH).endsWith(".launch");
    //      return false;
    //    };
    //    Function<Object, Object> inputFunction = resource -> {
    //      if (resource instanceof IFile)
    //      {
    //        IFile file = (IFile) resource;
    //        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    //        ILaunchConfiguration launchConfiguration = launchManager.getLaunchConfiguration(file);
    //        return launchConfiguration;
    //      }
    //      return null;
    //    };
    //    selectionListener = new PDESelectionListener(launchConfigurationViewer, notifyResourceChangeListener, predicate, inputFunction);
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
    menuMgr.addMenuListener(manager -> LaunchConfigurationView.this.fillContextMenu(manager));
    Menu menu = menuMgr.createContextMenu(launchConfigurationViewer.getControl());
    launchConfigurationViewer.getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, launchConfigurationViewer);
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
    expandAllNodesAction = new ExpandAllNodesAction(launchConfigurationViewer, true);
    collapseAllNodesAction = new ExpandAllNodesAction(launchConfigurationViewer, false);

    //
    doubleClickOpenNodeAction = new OpenNodeAction(launchConfigurationViewer);
  }

  private void hookDoubleClickAction()
  {
    launchConfigurationViewer.addDoubleClickListener(event -> doubleClickOpenNodeAction.run());
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus()
  {
    launchConfigurationViewer.getControl().setFocus();
  }

  /**
   * @return
   */
  public TreeViewer getLaunchConfigurationViewer()
  {
    return launchConfigurationViewer;
  }

  /**
   * @param launchConfiguration
   */
  public void setInput(ILaunchConfiguration launchConfiguration)
  {
    Util.setUseCache(true);

    try
    {
      launchConfigurationViewer.setInput(launchConfiguration);

      // refresh
      notifyResourceChangeListener.refreshWhenResourceChanged(launchConfigurationViewer);
    }
    finally
    {
      Util.setUseCache(false);
    }
  }
}
