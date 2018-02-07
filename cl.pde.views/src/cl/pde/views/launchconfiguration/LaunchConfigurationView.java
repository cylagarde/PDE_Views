package cl.pde.views.launchconfiguration;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;

import cl.pde.PDEViewActivator;
import cl.pde.views.AbstractCheckboxFilteredTree;
import cl.pde.views.AbstractPDEView;
import cl.pde.views.Constants;
import cl.pde.views.NotTreeParentPatternFilter;
import cl.pde.views.actions.GetAllLaunchConfigurationsAction;

/**
 * The class <b>LaunchConfigurationView</b> allows to.<br>
 */
public class LaunchConfigurationView extends AbstractPDEView
{
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "cl.pde.launchConfigurationView";

  private FilteredTree launchConfigurationFilteredTree;

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
    String[] checkboxLabels = {Constants.WORKSPACE_NODE, Constants.TARGET_PLATFORM_NODE, Constants.INCLUDED_PLUGINS_NODE, Constants.INCLUDED_FEATURES_NODE, Constants.REQUIRED_PLUGINS_NODE};
    launchConfigurationFilteredTree = new AbstractCheckboxFilteredTree(parent, filter)
    {
      @Override
      protected String[] getCheckboxLabels()
      {
        return checkboxLabels;
      }
    };
    getTreeViewer().setContentProvider(new LaunchConfigurationViewContentProvider());

    // Create the help context id for the viewer's control
    PlatformUI.getWorkbench().getHelpSystem().setHelp(getTreeViewer().getControl(), PDEViewActivator.PLUGIN_ID + ".launchConfigurationView");

    super.createPartControl(parent);
  }

  /*
   * @see cl.pde.views.AbstractPDEView#getTreeViewer()
   */
  @Override
  public TreeViewer getTreeViewer()
  {
    return launchConfigurationFilteredTree.getViewer();
  }

  /*
   * @see cl.pde.views.AbstractPDEView#createAllItemsAction()
   */
  @Override
  protected Action createAllItemsAction()
  {
    return new GetAllLaunchConfigurationsAction(this);
  }
}
