package cl.pde;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * The enum <b>Images</b> allows to list images used by plugin.<br>
 */
public enum Images
{
  PRODUCT("icons/product.png"),
  LAUNCH_CONFIGURATION("icons/launch_configuration.png"),

  EXPAND_ALL("icons/expand_all.png"),
  COLLAPSE_ALL("icons/collapse_all.png"),

  SINGLETON("icons/singleton.png"),

  GET_ALL_FEATURES("icons/getAllFeatures.png"),
  GET_ALL_PRODUCTS("icons/getAllProducts.png"),
  GET_ALL_LAUNCH_CONFIGURATIONS("icons/getAllLaunchConfigurations.png"),
  GET_ALL_PLUGINS("icons/getAllPlugins.png"),

  INVALID_PROJECT("icons/invalid_project.png"),
  TREE("icons/tree.gif"),

  JAR_ICON("org.eclipse.jdt.ui", "icons/full/obj16/jar_obj.png", "icons/full/obj16/jar_obj.gif"),
  ;

  final String pluginId;
  final String path;
  final String otherwisePath;

  private Images(String path)
  {
    this(PDEViewActivator.PLUGIN_ID, path);
  }

  private Images(String pluginId, String path)
  {
    this(pluginId, path, null);
  }

  private Images(String pluginId, String path, String otherwisePath)
  {
    this.pluginId = pluginId;
    this.path = path;
    this.otherwisePath = otherwisePath;
  }

  public Image getImage()
  {
    return PDEViewActivator.getImage(this);
  }

  public ImageDescriptor getImageDescriptor()
  {
    return PDEViewActivator.getImageDescriptor(this);
  }

  String getKey()
  {
    return pluginId + ":" + path;
  }

  String getOtherwiseKey()
  {
    return pluginId + ":" + otherwisePath;
  }
}
