package cl.pde.views.plugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IContentProvider;

import cl.pde.PDEViewActivator;
import cl.pde.views.AbstractPDEView;
import cl.pde.views.actions.GetAllPluginsAction;

/**
 * The class <b>PluginView</b> allows to display the content of a plugin.<br>
 */
public class PluginView extends AbstractPDEView
{
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "cl.pde.pluginView";

  @Override
  protected String getHelpContextId()
  {
    return PDEViewActivator.PLUGIN_ID + ".pluginView";
  }

  @Override
  protected String[] getCheckboxLabels()
  {
    String[] checkboxLabels = {};
    return checkboxLabels;
  }

  @Override
  protected Action createAllItemsAction()
  {
    return new GetAllPluginsAction(this);
  }

  @Override
  protected IContentProvider getViewContentProvider()
  {
    return new PluginViewContentProvider();
  }
}