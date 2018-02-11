package cl.pde;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * The enum <b>Images</b> allows to list images used by plugin.<br>
 */
public enum Images
{
  FEATURE("icons/ftr_xml_obj.png"),
  PRODUCT("icons/product_xml_obj.png"),
  EXPAND_ALL("icons/expand_all.png"),
  COLLAPSE_ALL("icons/collapse_all.png"),
  LAUNCH_CONFIGURATION("icons/launchConfiguration.png"),
  SINGLETON("icons/singleton.png"),
  GET_ALL_FEATURES("icons/getAllFeatures.png"),
  GET_ALL_PRODUCTS("icons/getAllProducts.png"),
  GET_ALL_LAUNCH_CONFIGURATIONS("icons/getAllLaunchConfigurations.png"),
  INVALID_PROJECT("icons/invalid_project.png"),
  TREE("icons/tree.gif"),
  ;

  private String path;

  private Images(String path)
  {
    this.path = path;
  }

  public String getPath()
  {
    return path;
  }

  public Image getImage()
  {
    return PDEViewActivator.getImage(this);
  }

  public ImageDescriptor getImageDescriptor()
  {
    return PDEViewActivator.getImageDescriptor(this);
  }

}
