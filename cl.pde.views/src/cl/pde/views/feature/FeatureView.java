package cl.pde.views.feature;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import cl.pde.PDEViewActivator;
import cl.pde.views.AbstractCheckboxFilteredTree;
import cl.pde.views.AbstractPDEView;
import cl.pde.views.Constants;
import cl.pde.views.NotTreeParentPatternFilter;
import cl.pde.views.actions.GetAllFeaturesAction;

/**
 * The class <b>FeatureView</b> allows to.<br>
 */
public class FeatureView extends AbstractPDEView
{
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "cl.pde.featureView";

  AbstractCheckboxFilteredTree featureFilteredTree;

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
    NotTreeParentPatternFilter filter = new NotTreeParentPatternFilter();
    String[] checkboxLabels = {Constants.WORKSPACE_NODE, Constants.TARGET_PLATFORM_NODE, Constants.INCLUDED_PLUGINS_NODE, Constants.INCLUDED_FEATURES_NODE, Constants.REQUIRED_PLUGINS_NODE};
    featureFilteredTree = new AbstractCheckboxFilteredTree(parent, filter)
    {
      @Override
      protected String[] getCheckboxLabels()
      {
        return checkboxLabels;
      }
    };
    getTreeViewer().setContentProvider(new FeatureViewContentProvider());

    // Create the help context id for the viewer's control
    PlatformUI.getWorkbench().getHelpSystem().setHelp(getTreeViewer().getControl(), PDEViewActivator.PLUGIN_ID + ".featureView");

    super.createPartControl(parent);
  }

  /*
   * @see cl.pde.views.AbstractPDEView#getTreeViewer()
   */
  @Override
  public TreeViewer getTreeViewer()
  {
    return featureFilteredTree.getViewer();
  }

  /*
   * @see cl.pde.views.AbstractPDEView#createAllItemsAction()
   */
  @Override
  protected Action createAllItemsAction()
  {
    return new GetAllFeaturesAction(this);
  }
}
