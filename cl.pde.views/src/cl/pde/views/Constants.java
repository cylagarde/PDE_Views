package cl.pde.views;

import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * The class <b>Constants</b> allows to.<br>
 */
public interface Constants
{
  final Color PLUGIN_FOREGROUND = null;
  //  final Color PLUGIN_FOREGROUND = new Color(null, 0, 207, 0);
  final Color FEATURE_FOREGROUND = new Color(null, 149, 125, 71);
  final Color PROJECT_FOREGROUND = null;
  final Color LAUNCH_CONFIGURATION_FOREGROUND = null;
  final Color PRODUCT_FOREGROUND = null;

  final Color[] PDE_COLORS = {PLUGIN_FOREGROUND, FEATURE_FOREGROUND, PROJECT_FOREGROUND, LAUNCH_CONFIGURATION_FOREGROUND, PRODUCT_FOREGROUND};

  final Color VERSION_FOREGROUND = new Color(null, 0, 127, 174);
  final Color ERROR_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_RED);

  // Nodes
  final String WORKSPACE_NODE = PDEUIMessages.AdvancedLauncherTab_workspacePlugins;
  final String TARGET_PLATFORM_NODE = PDEUIMessages.PluginsTab_target;
  final String INCLUDED_PLUGINS_NODE = PDEUIMessages.FeatureEditor_ReferencePage_title;
  final String INCLUDED_FEATURES_NODE = PDEUIMessages.FeatureEditor_IncludesPage_title;
  final String REQUIRED_PLUGINS_NODE = PDEUIMessages.FeatureEditor_DependenciesPage_title;
  final String ADDITIONAL_PLUGIN_NODE = "Additional Plug-ins"; //PDEUIMessages.FeatureBlock_AdditionalPluginsEntry_plugins; not compatible with older PDE
  final String FEATURES_NODE = "Features";
  final String PLUGINS_NODE = "Plugins";

  // Content types
  final String FEATURE_CONTENT_TYPE = "org.eclipse.pde.featureManifest";
  final String PRODUCT_CONTENT_TYPE = "org.eclipse.pde.productFile";

}
