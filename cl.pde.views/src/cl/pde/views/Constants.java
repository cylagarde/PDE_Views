package cl.pde.views;

import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.graphics.Color;

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

  final String WORKSPACE_FEATURE = PDEUIMessages.AdvancedLauncherTab_workspacePlugins;
  final String TARGET_FEATURE = PDEUIMessages.PluginsTab_target;
}
