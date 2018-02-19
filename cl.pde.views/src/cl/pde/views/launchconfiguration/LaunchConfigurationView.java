package cl.pde.views.launchconfiguration;

import java.util.function.Predicate;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IContentProvider;

import cl.pde.PDEViewActivator;
import cl.pde.views.AbstractPDEView;
import cl.pde.views.Constants;
import cl.pde.views.TreeParent;
import cl.pde.views.actions.GetAllLaunchConfigurationsAction;

/**
 * The class <b>LaunchConfigurationView</b> allows to display the content of a launch configuration.<br>
 */
public class LaunchConfigurationView extends AbstractPDEView
{
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "cl.pde.launchConfigurationView";

  @Override
  protected String getHelpContextId()
  {
    return PDEViewActivator.PLUGIN_ID + ".launchConfigurationView";
  }

  @Override
  protected String[] getCheckboxLabels()
  {
    String[] checkboxLabels = {Constants.WORKSPACE_NODE, Constants.TARGET_PLATFORM_NODE, Constants.INCLUDED_PLUGINS_NODE, Constants.INCLUDED_FEATURES_NODE, Constants.REQUIRED_PLUGINS_NODE};
    return checkboxLabels;
  }

  @Override
  protected Action createAllItemsAction()
  {
    return new GetAllLaunchConfigurationsAction(this);
  }

  @Override
  protected IContentProvider getViewContentProvider()
  {
    return new LaunchConfigurationViewContentProvider();
  }

  @Override
  protected Predicate<Object> getCanSearchOnElementPredicate()
  {
    return e -> !(e instanceof TreeParent);
  }
}
