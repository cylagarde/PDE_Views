package cl.pde.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.core.PDECore;

import cl.pde.Images;
import cl.pde.PDEViewActivator;
import cl.pde.views.plugin.PluginView;

/**
 * The class <b>GetAllProductsAction</b> allows to.<br>
 */
public class GetAllPluginsAction extends Action
{
  PluginView pluginView;

  /**
   * Constructor
   * @param pluginView
   */
  public GetAllPluginsAction(PluginView pluginView)
  {
    this.pluginView = pluginView;
    setText("Get all plugins");
    setToolTipText("Get all plugins");
    setImageDescriptor(PDEViewActivator.getImageDescriptor(Images.GET_ALL_PLUGINS));
  }

  @Override
  public void run()
  {
    TreeViewer pluginViewer = pluginView.getTreeViewer();
    pluginViewer.getControl().setRedraw(false);
    try
    {
      pluginView.setInput(PDECore.getDefault().getModelManager().getAllModels());
    }
    finally
    {
      pluginViewer.getControl().setRedraw(true);
    }
  }
}
