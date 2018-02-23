package cl.pde.views.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;

import cl.pde.Images;
import cl.pde.PDEViewActivator;
import cl.pde.views.Constants;
import cl.pde.views.TreeParent;
import cl.pde.views.Util;
import cl.pde.views.plugin.PluginView;

/**
 * The class <b>GetAllProductsAction</b> allows to search and display all plugins from workspace and target platform.<br>
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
    setToolTipText(getText());
    setImageDescriptor(PDEViewActivator.getImageDescriptor(Images.GET_ALL_PLUGINS));
  }

  @Override
  public void run()
  {
    TreeViewer pluginViewer = pluginView.getTreeViewer();
    pluginViewer.getControl().setRedraw(false);
    try
    {
      List<TreeParent> treeParentList = new ArrayList<>(2);

      //
      TreeParent workspaceFeatureTreeParent = new TreeParent(Constants.WORKSPACE_NODE);
      workspaceFeatureTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);

      Stream.of(PDECore.getDefault().getModelManager().getWorkspaceModels())
          .map(Util::getTreeParent)
          //          .sorted(TreeObject.TREEOBJECT_COMPARATOR)
          .forEach(workspaceFeatureTreeParent::addChild);
      if (workspaceFeatureTreeParent.getChildren().length != 0)
      {
        workspaceFeatureTreeParent.sortChildren();
        treeParentList.add(workspaceFeatureTreeParent);
      }

      //
      TreeParent externalFeatureTreeParent = new TreeParent(Constants.TARGET_PLATFORM_NODE);
      externalFeatureTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);

      Stream.of(PDECore.getDefault().getModelManager().getExternalModels())
          .map(Util::getTreeParent)
          //          .sorted(TreeObject.TREEOBJECT_COMPARATOR)
          .forEach(externalFeatureTreeParent::addChild);
      if (externalFeatureTreeParent.getChildren().length != 0)
      {
        externalFeatureTreeParent.sortChildren();
        treeParentList.add(externalFeatureTreeParent);
      }

      pluginView.setInput(treeParentList.toArray());
      pluginView.getTreeViewer().expandToLevel(2);
    }
    finally
    {
      pluginViewer.getControl().setRedraw(true);
    }
  }
}
