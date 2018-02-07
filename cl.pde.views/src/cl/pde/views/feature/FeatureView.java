package cl.pde.views.feature;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IContentProvider;

import cl.pde.PDEViewActivator;
import cl.pde.views.AbstractPDEView;
import cl.pde.views.Constants;
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

  @Override
  protected String getHelpContextId()
  {
    return PDEViewActivator.PLUGIN_ID + ".featureView";
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
    return new GetAllFeaturesAction(this);
  }

  @Override
  protected IContentProvider getViewContentProvider()
  {
    return new FeatureViewContentProvider();
  }
}
