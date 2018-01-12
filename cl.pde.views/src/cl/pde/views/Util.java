package cl.pde.views;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IFragment;
import org.eclipse.pde.core.plugin.IFragmentModel;
import org.eclipse.pde.core.plugin.IMatchRules;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ISharedPluginModel;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureImport;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.core.plugin.ExternalPluginModelBase;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.core.text.bundle.BundleSymbolicNameHeader;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.feature.FeatureEditor;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import cl.pde.Activator;

/**
 * The class <b>Util</b> allows to.<br>
 */
public class Util
{
  public static void traverseRoot(ITreeContentProvider treeContentProvider, Object root, Consumer<Object> consumer)
  {
    Object[] elements = treeContentProvider.getElements(root);
    for(Object element : elements)
      traverseElement(treeContentProvider, element, consumer);
  }

  public static void traverseElement(ITreeContentProvider treeContentProvider, Object element, Consumer<Object> consumer)
  {
    consumer.accept(element);

    Object[] children = treeContentProvider.getChildren(element);
    for(Object child : children)
      traverseElement(treeContentProvider, child, consumer);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Return true if project is open and has PluginNature
   *
   * @param project
   */
  public static boolean isValidPlugin(IProject project)
  {
    try
    {
      if (project.isOpen() && project.hasNature(PDE.PLUGIN_NATURE) && getPluginId(project) != null)
        return true;
    }
    catch(Exception e)
    {
      Activator.logError("Error: " + e, e);
    }
    return false;
  }

  /**
   * Return the plugin id
   *
   * @param project
   */
  public static String getPluginId(IProject project)
  {
    WorkspaceBundlePluginModel pluginModel = getWorkspaceBundlePluginModel(project);
    String pluginId = pluginModel.getPlugin().getId();
    return pluginId;
  }

  /**
   * Return the WorkspaceBundlePluginModel
   *
   * @param project
   */
  public static WorkspaceBundlePluginModel getWorkspaceBundlePluginModel(IProject project)
  {
    IFile pluginXml = null;// PDEProject.getPluginXml(project);
    IFile manifest = PDEProject.getManifest(project);
    WorkspaceBundlePluginModel pluginModel = new WorkspaceBundlePluginModel(manifest, pluginXml);
    return pluginModel;
  }

  /**
   *
   * @param container
   * @param fileConsumer
   * @throws CoreException
   */
  public static void processContainer(IContainer container, Consumer<IFile> fileConsumer) throws CoreException
  {
    IResource[] members = container.members();

    for(IResource member : members)
    {
      if (member instanceof IContainer)
        processContainer((IContainer) member, fileConsumer);
      else if (member instanceof IFile)
        fileConsumer.accept((IFile) member);
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Open PDE object
   * @param pdeObject
   */
  public static void open(Object pdeObject)
  {
    if (pdeObject instanceof IPlugin)
      openPlugin((IPlugin) pdeObject);

    else if (pdeObject instanceof IFeaturePlugin)
      openFeaturePlugin((IFeaturePlugin) pdeObject);

    else if (pdeObject instanceof IFeatureChild)
      openFeatureChild((IFeatureChild) pdeObject);

    else if (pdeObject instanceof IFeatureImport)
      openFeatureImport((IFeatureImport) pdeObject);

    else if (pdeObject instanceof IFeature)
      openFeature((IFeature) pdeObject);

    else if (pdeObject instanceof IProduct)
      openProduct((IProduct) pdeObject);

    else if (pdeObject instanceof IProductFeature)
      openProductFeature((IProductFeature) pdeObject);

    else if (pdeObject instanceof IProductPlugin)
      openProductPlugin((IProductPlugin) pdeObject);

    else if (pdeObject instanceof IFragment)
      openFragment((IFragment) pdeObject);

    else if (pdeObject instanceof IFeatureModel)
      openFeatureModel((IFeatureModel) pdeObject);

    else if (pdeObject != null)
      Activator.logError("Cannot open data " + pdeObject.getClass().getName(), new Exception());
  }

  /**
   * Open featureModel
   * @param featureModel
   */
  private static void openFeatureModel(IFeatureModel featureModel)
  {
    FeatureEditor.openFeatureEditor(featureModel);
  }

  /**
   * Open plugin
   * @param pluginId
   * @param pluginVersion
   */
  private static void openPlugin(String pluginId, String pluginVersion)
  {
    IPluginModelBase pluginModelBase = getPluginModelBase(pluginId, pluginVersion);
    if (pluginModelBase instanceof IFragmentModel)
    {
      IFragment fragment = ((IFragmentModel) pluginModelBase).getFragment();
      ManifestEditor.open(fragment, false);
    }
    else if (pluginModelBase instanceof IPluginModel)
    {
      IPlugin plugin = ((IPluginModel) pluginModelBase).getPlugin();
      ManifestEditor.open(plugin, false);
    }
    else
      Activator.logError("Cannot open plugin id=" + pluginId + ", version=" + pluginVersion, new Exception());
  }

  /**
   * Open Feature
   * @param featureId
   * @param featureVersion
   */
  private static void openFeature(String featureId, String featureVersion)
  {
    FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
    IFeatureModel featureModel = manager.findFeatureModel(featureId, featureVersion);
    if (featureModel != null)
      openFeatureModel(featureModel);
    else
      Activator.logError("Cannot open feature id=" + featureId + ", version=" + featureVersion, new Exception());
  }

  /**
   * Open Plugin
   * @param plugin
   */
  private static void openPlugin(IPlugin plugin)
  {
    ManifestEditor.open(plugin, false);
  }

  /**
   * Open FeatureChild
   * @param featureChild
   */
  private static void openFeatureChild(IFeatureChild featureChild)
  {
    String featureId = featureChild.getId();
    String featureVersion = featureChild.getVersion();
    openFeature(featureId, featureVersion);
  }

  /**
   * Open FeaturePlugin
   * @param featurePlugin
   */
  private static void openFeaturePlugin(IFeaturePlugin featurePlugin)
  {
    String pluginId = featurePlugin.getId();
    String pluginVersion = featurePlugin.getVersion();
    openPlugin(pluginId, pluginVersion);
  }

  /**
   * Open FeatureImport
   * @param featureImport
   */
  private static void openFeatureImport(IFeatureImport featureImport)
  {
    getSingletonState(featureImport);
    if (featureImport.getType() == IFeatureImport.PLUGIN)
      openPlugin(featureImport.getId(), featureImport.getVersion());
    else if (featureImport.getType() == IFeatureImport.FEATURE && featureImport.getFeature() != null)
      openFeature(featureImport.getFeature());
  }

  /**
   * Open Feature
   * @param feature
   */
  private static void openFeature(IFeature feature)
  {
    String featureId = feature.getId();
    String featureVersion = feature.getVersion();
    openFeature(featureId, featureVersion);
  }

  /**
   * Open IProduct
   * @param product
   */
  private static void openProduct(IProduct product)
  {
    IResource underlyingResource = product.getModel().getUnderlyingResource();
    if (underlyingResource instanceof IFile)
    {
      IFile productFile = (IFile) underlyingResource;
      FileEditorInput productFileEditorInput = new FileEditorInput(productFile);
      IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      try
      {
        IDE.openEditor(workbenchPage, productFileEditorInput, IPDEUIConstants.PRODUCT_EDITOR_ID);
      }
      catch(PartInitException e)
      {
        Activator.logError("Cannot open product", e);
      }
    }
  }

  /**
   * Open IProductFeature
   * @param productFeature
   */
  private static void openProductFeature(IProductFeature productFeature)
  {
    String featureId = productFeature.getId();
    String featureVersion = productFeature.getVersion();
    openFeature(featureId, featureVersion);
  }

  /**
   * Open IProductPlugin
   * @param productPlugin
   */
  private static void openProductPlugin(IProductPlugin productPlugin)
  {
    String pluginId = productPlugin.getId();
    String pluginVersion = productPlugin.getVersion();
    openPlugin(pluginId, pluginVersion);
  }

  /**
   * Open IFragment
   * @param fragment
   */
  private static void openFragment(IFragment fragment)
  {
    String fragmentId = fragment.getId();
    String fragmentVersion = fragment.getVersion();
    openPlugin(fragmentId, fragmentVersion);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get location
   * @param pdeObject
   */
  public static String getLocation(Object pdeObject)
  {
    if (pdeObject instanceof IPlugin)
      return getPluginLocation((IPlugin) pdeObject);

    if (pdeObject instanceof IFeaturePlugin)
      return getFeaturePluginLocation((IFeaturePlugin) pdeObject);

    if (pdeObject instanceof IFeatureChild)
      return getFeatureChildLocation((IFeatureChild) pdeObject);

    if (pdeObject instanceof IFeatureImport)
      return getFeatureImportLocation((IFeatureImport) pdeObject);

    if (pdeObject instanceof IFeature)
      return getFeatureLocation((IFeature) pdeObject);

    //    else if (pdeObject instanceof IProduct)
    //      return getProductLocation((IProduct) pdeObject);

    if (pdeObject instanceof IProductFeature)
      return getProductFeatureLocation((IProductFeature) pdeObject);

    if (pdeObject instanceof IProductPlugin)
      return getProductPluginLocation((IProductPlugin) pdeObject);

    if (pdeObject instanceof IFragment)
      return getFragmentLocation((IFragment) pdeObject);

    if (pdeObject instanceof IFeatureModel)
      return getFeatureModelLocation((IFeatureModel) pdeObject);

    return null;
  }

  /**
   * Get IFeatureModel location
   * @param featureModel
   */
  private static String getFeatureModelLocation(IFeatureModel featureModel)
  {
    return featureModel == null? null : featureModel.getInstallLocation();
  }

  /**
   * Get plugin location
   * @param pluginId
   * @param pluginVersion
   */
  public static String getPluginLocation(String pluginId, String pluginVersion)
  {
    IPluginModelBase pluginModelBase = getPluginModelBase(pluginId, pluginVersion);
    if (pluginModelBase instanceof IFragmentModel)
    {
      IFragment fragment = ((IFragmentModel) pluginModelBase).getFragment();
      return fragment.getPluginModel().getBundleDescription().getLocation();
    }
    else if (pluginModelBase instanceof IPluginModel)
    {
      IPlugin plugin = ((IPluginModel) pluginModelBase).getPlugin();
      return plugin.getPluginModel().getBundleDescription().getLocation();
    }

    return null;
  }

  /**
   * @param pluginId
   * @param pluginVersion
   */
  private static IPluginModelBase getPluginModelBase(String pluginId, String pluginVersion)
  {
    IPluginModelBase pluginModelBase = PluginRegistry.findModel(pluginId, pluginVersion, IMatchRules.PERFECT, null);
    if (pluginModelBase == null)
      pluginModelBase = PluginRegistry.findModel(pluginId, pluginVersion, IMatchRules.EQUIVALENT, null);
    if (pluginModelBase == null)
      pluginModelBase = PluginRegistry.findModel(pluginId);
    return pluginModelBase;
  }

  /**
   * Get Feature location
   * @param featureId
   * @param featureVersion
   */
  private static String getFeatureLocation(String featureId, String featureVersion)
  {
    FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
    IFeatureModel featureModel = manager.findFeatureModel(featureId, featureVersion);
    return getFeatureModelLocation(featureModel);
  }

  /**
   * get Plugin location
   * @param plugin
   */
  private static String getPluginLocation(IPlugin plugin)
  {
    //    String pluginId = plugin.getId();
    //    String pluginVersion = plugin.getVersion();
    //    return getPluginLocation(pluginId, pluginVersion);
    return plugin.getPluginModel().getBundleDescription().getLocation();
  }

  /**
   * Get FeatureChild location
   * @param featureChild
   */
  private static String getFeatureChildLocation(IFeatureChild featureChild)
  {
    String featureId = featureChild.getId();
    String featureVersion = featureChild.getVersion();
    return getFeatureLocation(featureId, featureVersion);
  }

  /**
   * Get FeaturePlugin location
   * @param featurePlugin
   */
  private static String getFeaturePluginLocation(IFeaturePlugin featurePlugin)
  {
    String pluginId = featurePlugin.getId();
    String pluginVersion = featurePlugin.getVersion();
    return getPluginLocation(pluginId, pluginVersion);
  }

  /**
   * Get FeatureImport location
   * @param featureImport
   */
  private static String getFeatureImportLocation(IFeatureImport featureImport)
  {
    if (featureImport.getType() == IFeatureImport.PLUGIN)
      return getPluginLocation(featureImport.getId(), featureImport.getVersion());
    else if (featureImport.getType() == IFeatureImport.FEATURE && featureImport.getFeature() != null)
      return getFeatureLocation(featureImport.getFeature());
    return null;
  }

  /**
   * Get Feature location
   * @param feature
   */
  private static String getFeatureLocation(IFeature feature)
  {
    String featureId = feature.getId();
    String featureVersion = feature.getVersion();
    return getFeatureLocation(featureId, featureVersion);
  }

  /**
   * Get IProductFeature location
   * @param productFeature
   */
  private static String getProductFeatureLocation(IProductFeature productFeature)
  {
    String featureId = productFeature.getId();
    String featureVersion = productFeature.getVersion();
    return getFeatureLocation(featureId, featureVersion);
  }

  /**
   * Get IProductPlugin location
   * @param productPlugin
   */
  private static String getProductPluginLocation(IProductPlugin productPlugin)
  {
    String pluginId = productPlugin.getId();
    String pluginVersion = productPlugin.getVersion();
    return getPluginLocation(pluginId, pluginVersion);
  }

  /**
   * Get IFragment location
   * @param fragment
   */
  private static String getFragmentLocation(IFragment fragment)
  {
    String fragmentId = fragment.getId();
    String fragmentVersion = fragment.getVersion();
    return getPluginLocation(fragmentId, fragmentVersion);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get resource
   * @param pdeObject
   */
  public static IResource getResource(Object pdeObject)
  {
    if (pdeObject instanceof IModel)
      return getModelResource((IModel) pdeObject);

    if (pdeObject instanceof IPlugin)
      return getPluginResource((IPlugin) pdeObject);

    if (pdeObject instanceof IFeaturePlugin)
      return getFeaturePluginResource((IFeaturePlugin) pdeObject);

    if (pdeObject instanceof IFeatureChild)
      return getFeatureChildResource((IFeatureChild) pdeObject);

    if (pdeObject instanceof IFeatureImport)
      return getFeatureImportResource((IFeatureImport) pdeObject);

    if (pdeObject instanceof IFeature)
      return getFeatureResource((IFeature) pdeObject);

    if (pdeObject instanceof IProduct)
      return getProductResource((IProduct) pdeObject);

    if (pdeObject instanceof IProductFeature)
      return getProductFeatureResource((IProductFeature) pdeObject);

    if (pdeObject instanceof IProductPlugin)
      return getProductPluginResource((IProductPlugin) pdeObject);

    if (pdeObject instanceof ILaunchConfiguration)
      return getLaunchConfigurationResource((ILaunchConfiguration) pdeObject);

    return null;
  }

  /**
   * Get plugin resource
   * @param pluginId
   * @param pluginVersion
   */
  private static IResource getPluginResource(String pluginId, String pluginVersion)
  {
    IPluginModelBase pluginModelBase = getPluginModelBase(pluginId, pluginVersion);
    return getModelResource(pluginModelBase);
  }

  /**
   * Get feature resource
   * @param featureId
   * @param featureVersion
   */
  private static IResource getFeatureResource(String featureId, String featureVersion)
  {
    FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
    IFeatureModel featureModel = manager.findFeatureModel(featureId, featureVersion);
    return getModelResource(featureModel);
  }

  /**
   * Get IFeatureImport resource
   * @param featureImport
   */
  private static IResource getFeatureImportResource(IFeatureImport featureImport)
  {
    String featureId = featureImport.getId();
    String featureVersion = featureImport.getVersion();
    if (featureImport.getType() == IFeatureImport.PLUGIN)
      return getPluginResource(featureId, featureVersion);
    else if (featureImport.getType() == IFeatureImport.FEATURE)
      return getFeatureResource(featureId, featureVersion);
    return null;
  }

  /**
   * Get IProductPlugin resource
   * @param productPlugin
   */
  private static IResource getProductPluginResource(IProductPlugin productPlugin)
  {
    String pluginId = productPlugin.getId();
    String pluginVersion = productPlugin.getVersion();
    return getPluginResource(pluginId, pluginVersion);
  }

  /**
   * Get ILaunchConfiguration resource
   * @param launchConfiguration
   */
  private static IResource getLaunchConfigurationResource(ILaunchConfiguration launchConfiguration)
  {
    return launchConfiguration.getFile();
  }

  /**
   * Get IPlugin resource
   * @param plugin
   */
  private static IResource getPluginResource(IPlugin plugin)
  {
    String pluginId = plugin.getId();
    String pluginVersion = plugin.getVersion();
    return getPluginResource(pluginId, pluginVersion);
  }

  /**
   * Get IProductFeature resource
   * @param productFeature
   */
  private static IResource getProductFeatureResource(IProductFeature productFeature)
  {
    String featureId = productFeature.getId();
    String featureVersion = productFeature.getVersion();
    return getFeatureResource(featureId, featureVersion);
  }

  /**
   * Get IFeatureChild resource
   * @param featureChild
   */
  private static IResource getFeatureChildResource(IFeatureChild featureChild)
  {
    String featureId = featureChild.getId();
    String featureVersion = featureChild.getVersion();
    return getFeatureResource(featureId, featureVersion);
  }

  /**
   * Get IModel resource
   * @param model
   */
  private static IResource getModelResource(IModel model)
  {
    return model == null? null : model.getUnderlyingResource();
  }

  /**
   * Get IFeaturePlugin resource
   * @param featurePlugin
   */
  private static IResource getFeaturePluginResource(IFeaturePlugin featurePlugin)
  {
    String pluginId = featurePlugin.getId();
    String pluginVersion = featurePlugin.getVersion();
    return getPluginResource(pluginId, pluginVersion);
  }

  /**
   * Get IFeature resource
   * @param feature
   */
  private static IResource getFeatureResource(IFeature feature)
  {
    return getModelResource(feature.getModel());
  }

  /**
   * Get IProduct resource
   * @param product
   */
  private static IResource getProductResource(IProduct product)
  {
    return getModelResource(product.getModel());
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get IFeature
   * @param productFeature
   */
  public static IFeature getFeature(IProductFeature productFeature)
  {
    String featureId = productFeature.getId();
    String featureVersion = productFeature.getVersion();
    return getFeature(featureId, featureVersion);
  }

  /**
   * Get IFeature
   * @param featureChild
   */
  public static IFeature getFeature(IFeatureChild featureChild)
  {
    String featureId = featureChild.getId();
    String featureVersion = featureChild.getVersion();
    return getFeature(featureId, featureVersion);
  }

  /**
   * Get IFeature
   * @param featureId
   * @param featureVersion
   */
  public static IFeature getFeature(String featureId, String featureVersion)
  {
    FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
    IFeatureModel featureModel = manager.findFeatureModel(featureId, featureVersion);
    return featureModel == null? null : featureModel.getFeature();
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  final static Object NULL = new Object();
  final static Map<Object, Object> SINGLETONSTATE_CACHEMAP = new HashMap<>();

  /**
   * Open PDE object
   * @param pdeObject
   */
  public static Boolean getSingletonState(Object pdeObject)
  {
    Object key = PDEPlugin.getDefault().getLabelProvider().getText(pdeObject);
    Object singletonState = SINGLETONSTATE_CACHEMAP.get(key);
    if (singletonState == null)
    {
      singletonState = getSingletonStateImpl(pdeObject);
      if (singletonState == null)
        singletonState = NULL;
      SINGLETONSTATE_CACHEMAP.put(key, singletonState);
    }
    //    else
    //      System.out.println("++++++++++++ " + key);

    return singletonState == NULL? null : (Boolean) singletonState;
  }

  /**
   * Open PDE object
   * @param pdeObject
   */
  private static Boolean getSingletonStateImpl(Object pdeObject)
  {
    if (pdeObject instanceof IPlugin)
      return getSingletonState((IPlugin) pdeObject);

    else if (pdeObject instanceof IFeaturePlugin)
      return getSingletonState((IFeaturePlugin) pdeObject);

    else if (pdeObject instanceof IFeatureImport)
      return getSingletonState((IFeatureImport) pdeObject);

    else if (pdeObject instanceof IProductPlugin)
      return getSingletonState((IProductPlugin) pdeObject);

    else if (pdeObject instanceof IFragment)
      return getSingletonState((IFragment) pdeObject);

    return null;
  }

  /**
   *
   * @param plugin
   */
  public static Boolean getSingletonState(IPlugin plugin)
  {
    ISharedPluginModel model = plugin.getModel();
    if (model instanceof IBundlePluginModelBase)
    {
      IBundlePluginModelBase bundlePluginModel = (IBundlePluginModelBase) model;
      IBundle bundle = bundlePluginModel.getBundleModel().getBundle();
      IManifestHeader header = bundle.getManifestHeader(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME);
      if (header instanceof BundleSymbolicNameHeader)
        return ((BundleSymbolicNameHeader) header).isSingleton();
    }
    if (model instanceof ExternalPluginModelBase)
      return getSingletonState((ExternalPluginModelBase) model);

    return null;
  }

  /**
   *
   * @param featureImport
   */
  public static Boolean getSingletonState(IFeatureImport featureImport)
  {
    String featureId = featureImport.getId();
    String featureVersion = featureImport.getVersion();
    if (featureImport.getType() == IFeatureImport.PLUGIN)
    {
      IPluginModelBase pluginModelBase = getPluginModelBase(featureId, featureVersion);
      if (pluginModelBase instanceof IPluginModel)
      {
        IPlugin plugin = ((IPluginModel) pluginModelBase).getPlugin();
        return getSingletonState(plugin);
      }
    }
    return null;
  }

  /**
   * @param featurePlugin
   */
  public static Boolean getSingletonState(IFeaturePlugin featurePlugin)
  {
    String pluginId = featurePlugin.getId();
    String pluginVersion = featurePlugin.getVersion();
    IPluginModelBase pluginModelBase = getPluginModelBase(pluginId, pluginVersion);
    if (pluginModelBase instanceof IPluginModel)
    {
      IPlugin plugin = ((IPluginModel) pluginModelBase).getPlugin();
      return getSingletonState(plugin);
    }
    return null;
  }

  /**
   * @param productPlugin
   */
  public static Boolean getSingletonState(IProductPlugin productPlugin)
  {
    String pluginId = productPlugin.getId();
    String pluginVersion = productPlugin.getVersion();
    IPluginModelBase pluginModelBase = getPluginModelBase(pluginId, pluginVersion);
    if (pluginModelBase instanceof IPluginModel)
    {
      IPlugin plugin = ((IPluginModel) pluginModelBase).getPlugin();
      return getSingletonState(plugin);
    }
    return null;
  }

  /**
   * @param fragment
   */
  public static Boolean getSingletonState(IFragment fragment)
  {
    String fragmentId = fragment.getId();
    String fragmentVersion = fragment.getVersion();
    IPluginModelBase pluginModelBase = getPluginModelBase(fragmentId, fragmentVersion);
    if (pluginModelBase instanceof ExternalPluginModelBase)
      return getSingletonState((ExternalPluginModelBase) pluginModelBase);

    return null;
  }

  /**
   * @param externalPluginModelBase
   */
  private static Boolean getSingletonState(ExternalPluginModelBase externalPluginModelBase)
  {
    String installLocation = externalPluginModelBase.getInstallLocation();
    if (installLocation != null)
    {
      File installLocationFile = new File(installLocation);
      if (installLocationFile.exists())
      {
        if (installLocationFile.isFile())
        {
          try
          {
            JarInputStream jarInputStream = new JarInputStream(new FileInputStream(installLocationFile));
            Manifest manifest = jarInputStream.getManifest();
            jarInputStream.close();
            if (manifest != null)
            {
              Attributes attributes = manifest.getMainAttributes();
              String value = attributes.getValue("Bundle-SymbolicName");
              if (value != null)
                return value.contains("singleton:=true");
            }
          }
          catch(Exception e)
          {
          }
        }
        else if (installLocationFile.isDirectory())
        {
          Path manifestPath = Paths.get(installLocationFile.getPath(), "META-INF", "MANIFEST.MF");
          if (Files.exists(manifestPath))
          {
            try
            {
              return Files.lines(manifestPath).anyMatch(line -> line.startsWith("Bundle-SymbolicName:") && line.contains("singleton:=true"));
            }
            catch(Exception e)
            {
            }
          }
        }
      }
    }

    return null;
  }

}
