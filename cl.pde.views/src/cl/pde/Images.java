package cl.pde;

/**
 * The enum <b>Images</b> allows to.<br>
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
  INVALID_PROJECT("icons/invalid_project.png"),
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
}
