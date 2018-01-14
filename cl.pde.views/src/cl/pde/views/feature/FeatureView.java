package cl.pde.views.feature;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
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
import cl.pde.views.actions.ExpandAllNodesAction;
import cl.pde.views.actions.GetAllFeaturesAction;
import cl.pde.views.actions.OpenNodeAction;

/**
 * The class <b>FeatureView</b> allows to.<br>
 */
public class FeatureView extends ViewPart
{
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "cl.pde.featureView";

  private FilteredTree featureFilteredTree;
  private TreeViewer featureViewer;

  private DrillDownAdapter drillDownAdapter;

  private Action expandAllNodesAction;
  private Action collapseAllNodesAction;
  private Action getAllFeaturesAction;
  private Action doubleClickOpenNodeAction;

  private ISelectionListener selectionListener;
  private NotifyResourceChangeListener notifyResourceChangeListener;

  /**
   * The constructor.
   */
  public FeatureView()
  {
  }

  /**
   * This is a callback that will allow us
   * to create the viewer and initialize it.
   */
  @Override
  public void createPartControl(Composite parent)
  {
    //
    PatternFilter filter = new NotTreeParentPatternFilter();
    featureFilteredTree = new FeatureFilteredTree(parent, filter);
    featureViewer = featureFilteredTree.getViewer();

    drillDownAdapter = new DrillDownAdapter(featureViewer);

    //
    PDEPlugin.getDefault().getLabelProvider().connect(this);
    notifyResourceChangeListener = new NotifyResourceChangeListener();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener(notifyResourceChangeListener, IResourceChangeEvent.POST_CHANGE);

    //
    featureViewer.getTree().addDisposeListener(e -> {
      workspace.removeResourceChangeListener(notifyResourceChangeListener);
      PDEPlugin.getDefault().getLabelProvider().disconnect(FeatureView.this);
      PDEPlugin.getDefault().getLabelProvider().dispose();
    });
    featureViewer.addTreeListener(new ExpandTreeViewerListener());

    // Create the help context id for the viewer's control
    PlatformUI.getWorkbench().getHelpSystem().setHelp(featureViewer.getControl(), Activator.PLUGIN_ID + ".featureView");

    getSite().setSelectionProvider(featureViewer);

    makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();

    //
    ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();

    Predicate<Object> selectionPredicate = resource -> {
      if (resource instanceof IFile)
        return ICoreConstants.FEATURE_FILENAME_DESCRIPTOR.equals(((IFile) resource).getName().toLowerCase(Locale.ENGLISH));
      //      if (resource instanceof TreeObject)
      //      {
      //        Object data = ((TreeObject) resource).data;
      //        return (data instanceof IFeature) || (data instanceof IProductFeature) || (data instanceof IFeatureChild) || (data instanceof IFeatureModel);
      //      }
      //      System.err.println("selectionPredicate " + resource.getClass());
      return false;
    };
    Function<Object, Object> inputFunction = resource -> {
      if (resource instanceof IFile)
      {
        IFile file = (IFile) resource;
        WorkspaceFeatureModel workspaceFeatureModel = new WorkspaceFeatureModel(file);
        workspaceFeatureModel.load();
        return workspaceFeatureModel;
      }
      //      if (resource instanceof TreeObject)
      //        resource = ((TreeObject) resource).data;
      //      if (resource instanceof IFeature)
      //        return resource;
      //      if (resource instanceof IProductFeature)
      //      {
      //        IProductFeature productFeature = (IProductFeature) resource;
      //        return Util.getFeature(productFeature);
      //      }
      //      if (resource instanceof IFeatureChild)
      //      {
      //        IFeatureChild featureChild = (IFeatureChild) resource;
      //        return Util.getFeature(featureChild);
      //      }
      //      if (resource instanceof IFeatureModel)
      //      {
      //        IFeatureModel featureModel = (IFeatureModel) resource;
      //        return featureModel.getFeature();
      //      }
      return resource;
    };
    selectionListener = new PDESelectionListener(featureViewer, notifyResourceChangeListener, selectionPredicate, inputFunction);
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
    menuMgr.addMenuListener(manager -> fillContextMenu(manager));
    Menu menu = menuMgr.createContextMenu(featureViewer.getControl());
    featureViewer.getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, featureViewer);
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
    manager.add(getAllFeaturesAction);
    manager.add(new Separator());
    manager.add(expandAllNodesAction);
    manager.add(collapseAllNodesAction);
    manager.add(new Separator());
    drillDownAdapter.addNavigationActions(manager);
  }

  private void makeActions()
  {
    expandAllNodesAction = new ExpandAllNodesAction(featureViewer, true);
    collapseAllNodesAction = new ExpandAllNodesAction(featureViewer, false);

    //
    getAllFeaturesAction = new GetAllFeaturesAction(this);

    //
    doubleClickOpenNodeAction = new OpenNodeAction(featureViewer);
  }

  /**
   *
   * @param input
   */
  public void refresh(Object input)
  {
    featureViewer.setInput(input);
  }

  private void hookDoubleClickAction()
  {
    featureViewer.addDoubleClickListener(event -> doubleClickOpenNodeAction.run());
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus()
  {
    featureViewer.getControl().setFocus();
  }
}
