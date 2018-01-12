package cl.pde.views.launchconfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.pde.core.plugin.IFragment;
import org.eclipse.pde.core.plugin.IFragmentModel;
import org.eclipse.pde.core.plugin.IMatchRules;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.launching.IPDELauncherConstants;

import cl.pde.Activator;
import cl.pde.Images;
import cl.pde.views.Constants;
import cl.pde.views.TreeObject;
import cl.pde.views.TreeParent;
import cl.pde.views.UseCacheTreeContentProvider;
import cl.pde.views.feature.FeatureViewContentProvider;

/**
 * The class <b>LaunchConfigurationViewContentProvider</b> allows to.<br>
 */
public class LaunchConfigurationViewContentProvider extends UseCacheTreeContentProvider
{
  @Override
  public Object[] getElements(Object parent)
  {
    if (parent instanceof ILaunchConfiguration)
    {
      ILaunchConfiguration launchConfiguration = (ILaunchConfiguration) parent;

      TreeParent launchConfigurationTreeParent = new TreeParent(null, launchConfiguration);
      launchConfigurationTreeParent.image = Activator.getImage(Images.LAUNCH_CONFIGURATION);
      launchConfigurationTreeParent.foreground = Constants.LAUNCH_CONFIGURATION_FOREGROUND;

      launchConfigurationTreeParent.loadChildRunnable = () -> {
        List<TreeObject> elements = getElementsFromLaunchConfiguration(launchConfiguration);
        elements.forEach(launchConfigurationTreeParent::addChild);
      };

      return new Object[]{launchConfigurationTreeParent};
    }

    return getChildren(parent);
  }

  /**
   * @param launchConfiguration
   */
  private List<TreeObject> getElementsFromLaunchConfiguration(ILaunchConfiguration launchConfiguration)
  {
    List<TreeObject> elements = new ArrayList<>();

    try
    {
      // use features or plugins
      boolean useCustomFeatures = launchConfiguration.getAttribute(IPDELauncherConstants.USE_CUSTOM_FEATURES, false);
      if (useCustomFeatures)
      {
        // load features
        loadFeaturesFromLaunchConfiguration(launchConfiguration, elements);
      }
      else
      {
        // load plugins
        loadPluginsFromLaunchConfiguration(launchConfiguration, elements);
      }
    }
    catch(CoreException e)
    {
      e.printStackTrace();
      Activator.logError(e.toString(), e);
    }

    return elements;
  }

  /**
   * Load plugins from launchConfiguration
   * @param launchConfiguration
   * @param elements
   */
  private void loadPluginsFromLaunchConfiguration(ILaunchConfiguration launchConfiguration, List<TreeObject> elements)
  {
    TreeParent workspacePlugins = createTreeParent(launchConfiguration, PDEUIMessages.AdvancedLauncherTab_workspacePlugins, IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS);
    elements.add(workspacePlugins);

    TreeParent externalPlugins = createTreeParent(launchConfiguration, PDEUIMessages.PluginsTab_target, IPDELauncherConstants.SELECTED_TARGET_PLUGINS);
    elements.add(externalPlugins);
  }

  /**
   * Create TreeParent from launchConfiguration
   * @param launchConfiguration
   * @param name
   * @param attributeKey
   */
  private TreeParent createTreeParent(ILaunchConfiguration launchConfiguration, String name, String attributeKey)
  {
    TreeParent treeParent = new TreeParent(name, null);
    treeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);

    //
    treeParent.loadChildRunnable = () -> {
      List<IPluginBase> pluginBases = loadPlugins(launchConfiguration, attributeKey);

      // sort
      Comparator<Object> PDE_COMPARATOR = Comparator.comparing(PDEPlugin.getDefault().getLabelProvider()::getText);
      Collections.sort(pluginBases, PDE_COMPARATOR);

      //
      pluginBases.forEach(pluginBase -> {
        TreeObject treeObject = new TreeObject(null, pluginBase);
        treeObject.foreground = Constants.PLUGIN_FOREGROUND;
        treeParent.addChild(treeObject);
      });
    };

    return treeParent;
  }

  /**
   * Load plugins from launchConfiguration
   * @param launchConfiguration
   * @param attributeKey
   */
  private List<IPluginBase> loadPlugins(ILaunchConfiguration launchConfiguration, String attributeKey)
  {
    List<IPluginBase> pluginBases = new ArrayList<>();
    try
    {
      String selected_target_plugins = launchConfiguration.getAttribute(attributeKey, "");
      for(String str : selected_target_plugins.split(","))
      {
        str = str.substring(0, str.indexOf(':'));
        str = str.substring(0, str.indexOf('@'));
        int index = str.indexOf('*');
        String pluginId = str;

        IPluginModelBase model = null;
        if (index >= 0)
        {
          pluginId = str.substring(0, index);
          String pluginVersion = str.substring(index + 1);
          model = PluginRegistry.findModel(pluginId, pluginVersion, IMatchRules.PERFECT, null);
        }
        if (model == null)
          model = PluginRegistry.findModel(pluginId);

        if (model instanceof IFragmentModel)
        {
          IFragment fragment = ((IFragmentModel) model).getFragment();
          pluginBases.add(fragment);
        }
        else if (model instanceof IPluginModel)
        {
          IPlugin plugin = ((IPluginModel) model).getPlugin();
          pluginBases.add(plugin);
        }
      }
    }
    catch(CoreException e)
    {
      e.printStackTrace();
      Activator.logError(e.toString(), e);
    }

    return pluginBases;
  }

  /**
   * Load features from launchConfiguration
   * @param launchConfiguration
   * @param elements
   */
  private void loadFeaturesFromLaunchConfiguration(ILaunchConfiguration launchConfiguration, List<TreeObject> elements)
  {
    List<IFeatureModel> featureModels = new ArrayList<>();
    FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();

    try
    {
      Set<String> selected_features = launchConfiguration.getAttribute(IPDELauncherConstants.SELECTED_FEATURES, Collections.emptySet());
      for(String str : selected_features)
      {
        if (str.isEmpty())
          continue;
        str = str.substring(0, str.indexOf(':'));
        //        str = str.substring(0, str.indexOf('@'));
        int index = str.indexOf('*');
        String pluginId = str;

        IFeatureModel featureModel;
        if (index >= 0)
        {
          pluginId = str.substring(0, index);
          String pluginVersion = str.substring(index + 1);

          featureModel = manager.findFeatureModel(pluginId, pluginVersion);
        }
        else
          featureModel = manager.findFeatureModel(pluginId);

        if (featureModel != null)
          featureModels.add(featureModel);
      }
    }
    catch(CoreException e)
    {
      e.printStackTrace();
      Activator.logError(e.toString(), e);
    }

    // sort
    Comparator<Object> PDE_COMPARATOR = Comparator.comparing(PDEPlugin.getDefault().getLabelProvider()::getText);
    Collections.sort(featureModels, PDE_COMPARATOR);

    featureModels.stream().map(featureModel -> {
      TreeParent treeParent = new TreeParent(null, featureModel);
      treeParent.foreground = Constants.FEATURE_FOREGROUND;

      treeParent.loadChildRunnable = () -> {
        IFeature feature = featureModel.getFeature();
        List<TreeParent> childElements = FeatureViewContentProvider.getElementsFromFeature(feature);
        childElements.forEach(treeParent::addChild);
      };

      return treeParent;
    }).forEach(elements::add);
  }
}
