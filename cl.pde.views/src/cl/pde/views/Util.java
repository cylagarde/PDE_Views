package cl.pde.views;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IFragment;
import org.eclipse.pde.core.plugin.IFragmentModel;
import org.eclipse.pde.core.plugin.IMatchRules;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ISharedPluginModel;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.feature.FeatureChild;
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
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.core.plugin.ExternalPluginModelBase;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.core.text.bundle.BundleSymbolicNameHeader;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.internal.ui.PDELabelProvider;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.editor.feature.FeatureEditor;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import cl.pde.Images;
import cl.pde.PDEViewActivator;

/**
 * The class <b>Util</b> allows to.<br>
 */
public class Util
{
  private static final Set<String> MESSAGE_ALREADY_PRINTED_SET = new HashSet<>();

  private static IProgressMonitor split(IProgressMonitor monitor, int totalWork)
  {
    return new SubProgressMonitor(monitor, totalWork);
  }

  /**
   * Traverse tree
   * @param tree
   * @param predicate
   * @param monitor
   */
  public static int traverseTree(Tree tree, Predicate<TreeItem> predicate, IProgressMonitor monitor)
  {
    int count = 1;
    TreeItem[] items = tree.getItems();
    monitor.beginTask("", items.length);
    //SubMonitor subMonitor = SubMonitor.convert(monitor, items.length);
    for(TreeItem item : items)
    {
      //      count += traverseItem(item, predicate, subMonitor.split(1));
      count += traverseItem(item, predicate, split(monitor, 1), "");
      if (monitor.isCanceled())
        break;
    }
    return count;
  }

  /**
   * Traverse item
   * @param item
   * @param predicate
   * @param monitor
   */
  public static int traverseItem(TreeItem item, Predicate<TreeItem> predicate, IProgressMonitor monitor, String indent)
  {
    //    System.out.println(indent + item);
    int count = 1;
    if (predicate.test(item))
    {
      TreeItem[] items = item.getItems();
      monitor.beginTask("", items.length);
      //      SubMonitor subMonitor = SubMonitor.convert(monitor, items.length);
      for(TreeItem child : items)
      {
        count += traverseItem(child, predicate, split(monitor, 1), indent + "  ");
        //        count += traverseItem(child, predicate, subMonitor.split(1));
        if (monitor.isCanceled())
          break;
      }
    }
    return count;
  }

  /**
   * Traverse Root
   * @param treeContentProvider
   * @param root
   * @param predicate
   * @param monitor
   */
  public static int traverseRoot(ITreeContentProvider treeContentProvider, Object root, Predicate<Object> predicate, IProgressMonitor monitor)
  {
    int count = 1;
    Object[] elements = treeContentProvider.getElements(root);
    monitor.beginTask("", elements.length);
    //    SubMonitor subMonitor = SubMonitor.convert(monitor, elements.length);
    for(Object element : elements)
    {
      count += traverseElement(treeContentProvider, element, predicate, split(monitor, 1));
      //      count += traverseElement(treeContentProvider, element, predicate, subMonitor.split(1));
      if (monitor.isCanceled())
        break;
    }
    return count;
  }

  /**
   * Traverse Element
   * @param treeContentProvider
   * @param element
   * @param predicate
   * @param monitor
   */
  public static int traverseElement(ITreeContentProvider treeContentProvider, Object element, Predicate<Object> predicate, IProgressMonitor monitor)
  {
    int count = 1;
    if (predicate.test(element))
    {
      Object[] children = treeContentProvider.getChildren(element);
      //      count += children.length;
      monitor.beginTask("", children.length);
      //      SubMonitor subMonitor = SubMonitor.convert(monitor, children.length);
      for(Object child : children)
      {
        count += traverseElement(treeContentProvider, child, predicate, split(monitor, 1));
        //        count += traverseElement(treeContentProvider, child, predicate, subMonitor.split(1));
        if (monitor.isCanceled())
          break;
      }
    }
    return count;
  }

  /**
   * Process container
   * @param container
   * @param fileConsumer
   * @throws CoreException
   */
  public static void traverseContainer(IContainer container, Predicate<IResource> filePredicate, IProgressMonitor monitor) throws CoreException
  {
    if (filePredicate.test(container))
    {
      IResource[] members = container.members();
      monitor.beginTask("", members.length);
      //      SubMonitor subMonitor = SubMonitor.convert(monitor, members.length);
      for(IResource member : members)
      {
        if (member instanceof IContainer)
        {
          traverseContainer((IContainer) member, filePredicate, split(monitor, 1));
          //          traverseContainer((IContainer) member, filePredicate, subMonitor.split(1));
        }
        else if (member instanceof IFile)
        {
          if (!filePredicate.test(member))
            break;
        }
      }
    }
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
      PDEViewActivator.logError("Error: " + e, e);
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
    IFile pluginXml = PDEProject.getPluginXml(project);
    IFile manifest = PDEProject.getManifest(project);
    WorkspaceBundlePluginModel pluginModel = new WorkspaceBundlePluginModel(manifest, pluginXml);
    if (!manifest.exists())
    {
      File manifestFile = manifest.getRawLocation().toFile();
      if (manifestFile.exists())
      {
        try
        {
          try (FileInputStream fis = new FileInputStream(manifestFile))
          {
            pluginModel.load(fis, false);
          }
        }
        catch(Exception e)
        {
          PDEViewActivator.logError("Cannot load " + manifestFile);
        }
      }
    }
    //    pluginModel.load();
    return pluginModel;
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

    else if (pdeObject instanceof IProductModel)
      openProductModel((IProductModel) pdeObject);

    else if (pdeObject instanceof ILaunchConfiguration)
      openLaunchConfiguration((ILaunchConfiguration) pdeObject);

    else if (pdeObject != null)
    {
      String msg = "Unsupported open for " + pdeObject.getClass().getName();
      if (MESSAGE_ALREADY_PRINTED_SET.add(msg))
        PDEViewActivator.logError(msg, new Exception());
    }
  }

  /**
   * Open launchConfiguration
   * @param launchConfiguration
   */
  private static void openLaunchConfiguration(ILaunchConfiguration launchConfiguration)
  {
    IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    IResource launchConfigurationResource = getLaunchConfigurationResource(launchConfiguration);
    if (workbenchPage != null && launchConfigurationResource instanceof IFile)
    {
      try
      {
        IDE.openEditor(workbenchPage, (IFile) launchConfigurationResource);
      }
      catch(PartInitException e)
      {
        PDEViewActivator.logError("Cannot open product " + launchConfigurationResource, e);
      }
    }
  }

  /**
   * Open featureModel
   * @param productModel
   */
  private static void openProductModel(IProductModel productModel)
  {
    IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    IResource productModelResource = getProductModelResource(productModel);
    if (workbenchPage != null && productModelResource instanceof IFile)
    {
      try
      {
        IDE.openEditor(workbenchPage, (IFile) productModelResource, IPDEUIConstants.PRODUCT_EDITOR_ID);
      }
      catch(PartInitException e)
      {
        PDEViewActivator.logError("Cannot open product " + productModelResource, e);
      }
    }
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
  private static void openPlugin(IPluginModelBase pluginModelBase)
  {
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
    else if (pluginModelBase != null)
    {
      String message = "Cannot open plugin for " + pluginModelBase.getClass().getName();
      PDEViewActivator.logError(message);
      Shell shell = Display.getDefault().getActiveShell();
      MessageDialog.openError(shell, "Error", message);
    }
  }

  /**
   * Open plugin
   * @param pluginId
   * @param pluginVersion
   */
  private static void openPlugin(String pluginId, String pluginVersion)
  {
    IPluginModelBase pluginModelBase = getPluginModelBase(pluginId, pluginVersion);
    openPlugin(pluginModelBase);
  }

  /**
   * Open Feature
   * @param featureId
   * @param featureVersion
   */
  private static void openFeature(String featureId, String featureVersion)
  {
    IFeatureModel featureModel = getFeatureModel(featureId, featureVersion);
    if (featureModel != null)
      openFeatureModel(featureModel);
    else
      PDEViewActivator.logError("Cannot open feature id=" + featureId + ", version=" + featureVersion);
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
        PDEViewActivator.logError("Cannot open product", e);
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
    String location = null;

    if (pdeObject instanceof IPlugin)
      location = getPluginLocation((IPlugin) pdeObject);

    else if (pdeObject instanceof IFeaturePlugin)
      location = getFeaturePluginLocation((IFeaturePlugin) pdeObject);

    else if (pdeObject instanceof IFeatureChild)
      location = getFeatureChildLocation((IFeatureChild) pdeObject);

    else if (pdeObject instanceof IFeatureImport)
      location = getFeatureImportLocation((IFeatureImport) pdeObject);

    else if (pdeObject instanceof IFeature)
      location = getFeatureLocation((IFeature) pdeObject);

    else if (pdeObject instanceof IProductFeature)
      location = getProductFeatureLocation((IProductFeature) pdeObject);

    else if (pdeObject instanceof IProductPlugin)
      location = getProductPluginLocation((IProductPlugin) pdeObject);

    else if (pdeObject instanceof IFragment)
      location = getFragmentLocation((IFragment) pdeObject);

    else if (pdeObject instanceof IFeatureModel)
      location = getFeatureModelLocation((IFeatureModel) pdeObject);

    else if (pdeObject instanceof IProductModel)
      location = getProductModelLocation((IProductModel) pdeObject);

    else if (pdeObject instanceof ILaunchConfiguration)
      location = getLaunchConfigurationLocation((ILaunchConfiguration) pdeObject);

    else if (pdeObject != null)
    {
      String msg = "Unsupported location for " + pdeObject.getClass().getName();
      if (MESSAGE_ALREADY_PRINTED_SET.add(msg))
        PDEViewActivator.logError(msg, new Exception());
    }

    return location;
  }

  /**
   * Get ILaunchConfiguration location
   * @param launchConfiguration
   */
  private static String getLaunchConfigurationLocation(ILaunchConfiguration launchConfiguration)
  {
    return launchConfiguration.getFile().getLocation().toString();
  }

  /**
   * Get IProductModel location
   * @param productModel
   */
  private static String getProductModelLocation(IProductModel productModel)
  {
    return productModel == null? null : productModel.getInstallLocation();
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
   * Get pluginModelBase location
   * @param pluginModelBase
   */
  private static String getPluginModelLocation(IPluginModelBase pluginModelBase)
  {
    return pluginModelBase == null? null : pluginModelBase.getBundleDescription().getLocation();
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
    //    if (pluginModelBase == null && VersionUtil.isEmptyVersion(pluginVersion))
    //      pluginModelBase = PluginRegistry.findModel(pluginId, pluginVersion, IMatchRules.EQUIVALENT, null);
    if (pluginModelBase == null && VersionUtil.isEmptyVersion(pluginVersion))
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
    IFeatureModel featureModel = getFeatureModel(featureId, featureVersion);
    return getFeatureModelLocation(featureModel);
  }

  /**
   * get Plugin location
   * @param plugin
   */
  private static String getPluginLocation(IPlugin plugin)
  {
    return getPluginModelLocation(plugin.getPluginModel());
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
    String oldLocation = null;
    if (featureImport.getType() == IFeatureImport.PLUGIN)
      oldLocation = getPluginLocation(featureImport.getId(), featureImport.getVersion());
    else if (featureImport.getType() == IFeatureImport.FEATURE && featureImport.getFeature() != null)
      oldLocation = getFeatureLocation(featureImport.getFeature());
    return oldLocation;
  }

  /**
   * Get Feature location
   * @param feature
   */
  private static String getFeatureLocation(IFeature feature)
  {
    return getFeatureModelLocation(feature.getModel());
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
    return getPluginModelLocation(fragment.getPluginModel());
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  final static Map<Object, Object> RESOURCE_CACHEMAP = new HashMap<>();

  /**
   * Get resource
   * @param pdeObject
   */
  public static IResource getResource(Object pdeObject)
  {
    Object key = getPDEKey(pdeObject);
    Object resource = RESOURCE_CACHEMAP.get(key);
    if (resource == null || !USE_CACHE)
    {
      resource = getResourceImpl(pdeObject);
      if (resource == null)
        resource = NULL;
      if (USE_CACHE)
        RESOURCE_CACHEMAP.put(key, resource);
    }
    //    else
    //      System.out.println("++++++++++++ " + key);

    return resource == NULL? null : (IResource) resource;
  }

  /**
   * Get resource
   * @param pdeObject
   */
  private static IResource getResourceImpl(Object pdeObject)
  {
    IResource resource = null;
    //    System.out.println(" " + pdeObject);
    if (pdeObject instanceof IModel)
      resource = getModelResource((IModel) pdeObject);

    else if (pdeObject instanceof IPlugin)
      resource = getPluginResource((IPlugin) pdeObject);

    else if (pdeObject instanceof IFeaturePlugin)
      resource = getFeaturePluginResource((IFeaturePlugin) pdeObject);

    else if (pdeObject instanceof IFeatureChild)
      resource = getFeatureChildResource((IFeatureChild) pdeObject);

    else if (pdeObject instanceof IFeatureImport)
      resource = getFeatureImportResource((IFeatureImport) pdeObject);

    else if (pdeObject instanceof IFeature)
      resource = getFeatureResource((IFeature) pdeObject);

    else if (pdeObject instanceof IProduct)
      resource = getProductResource((IProduct) pdeObject);

    else if (pdeObject instanceof IProductFeature)
      resource = getProductFeatureResource((IProductFeature) pdeObject);

    else if (pdeObject instanceof IProductPlugin)
      resource = getProductPluginResource((IProductPlugin) pdeObject);

    else if (pdeObject instanceof ILaunchConfiguration)
      resource = getLaunchConfigurationResource((ILaunchConfiguration) pdeObject);

    else if (pdeObject instanceof IProductModel)
      resource = getProductModelResource((IProductModel) pdeObject);

    else if (pdeObject instanceof IFragment)
      resource = getFragmentResource((IFragment) pdeObject);

    else if (pdeObject != null)
    {
      String msg = "Unsupported resource for " + pdeObject.getClass().getName();
      if (MESSAGE_ALREADY_PRINTED_SET.add(msg))
        PDEViewActivator.logError(msg, new Exception());
    }

    return resource;
  }

  /**
   * Get model resource
   * @param model
   */
  private static IResource getModelResource(IModel model)
  {
    return model == null? null : model.getUnderlyingResource();
  }

  /**
   * Get fragment resource
   * @param fragment
   */
  private static IResource getFragmentResource(IFragment fragment)
  {
    return getModelResource(fragment.getPluginModel());
  }

  /**
   * Get productModel resource
   * @param productModel
   */
  private static IResource getProductModelResource(IProductModel productModel)
  {
    return getModelResource(productModel);
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
    IFeatureModel featureModel = getFeatureModel(featureId, featureVersion);
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
    return getModelResource(plugin.getPluginModel());
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
   * Get IFeatureModel
   * @param featureId
   * @param featureVersion
   */
  private static IFeatureModel getFeatureModel(String featureId, String featureVersion)
  {
    FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
    IFeatureModel featureModel = manager.findFeatureModel(featureId, featureVersion);
    return featureModel;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  final static Object NULL = new Object();
  static boolean USE_CACHE = false;
  final static Map<Object, Object> SINGLETONSTATE_CACHEMAP = new HashMap<>();

  /**
   * @param useCache
   */
  public static void setUseCache(boolean useCache)
  {
    USE_CACHE = useCache;
    SINGLETONSTATE_CACHEMAP.clear();
    RESOURCE_CACHEMAP.clear();
  }

  /**
   * Open PDE object
   * @param pdeObject
   */
  public static Boolean getSingletonState(Object pdeObject)
  {
    Object key = getPDEKey(pdeObject);
    Object singletonState = SINGLETONSTATE_CACHEMAP.get(key);
    if (singletonState == null || !USE_CACHE)
    {
      singletonState = getSingletonStateImpl(pdeObject);
      if (singletonState == null)
        singletonState = NULL;
      if (USE_CACHE)
        SINGLETONSTATE_CACHEMAP.put(key, singletonState);
    }
    //    else
    //      System.out.println("++++++++++++ " + key);

    return singletonState == NULL? null : (Boolean) singletonState;
  }

  /**
   * @param pdeObject
   */
  private static Object getPDEKey(Object pdeObject)
  {
    //    return PDEPlugin.getDefault().getLabelProvider().getText(pdeObject);
    return pdeObject;
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

    else if (pdeObject instanceof IFeatureModel)
      return null;

    else if (pdeObject instanceof IFeatureChild)
      return null;

    else if (pdeObject instanceof IProductFeature)
      return null;

    else if (pdeObject != null)
    {
      String msg = "Unsupported singleton for " + pdeObject.getClass().getName();
      if (MESSAGE_ALREADY_PRINTED_SET.add(msg))
        PDEViewActivator.logError(msg, new Exception());
    }

    return null;
  }

  /**
   *
   * @param plugin
   */
  private static Boolean getSingletonState(IPlugin plugin)
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
  private static Boolean getSingletonState(IFeatureImport featureImport)
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
      if (pluginModelBase instanceof IFragmentModel)
      {
        IFragment fragment = ((IFragmentModel) pluginModelBase).getFragment();
        return getSingletonState(fragment);
      }
      if (pluginModelBase != null)
        PDEViewActivator.logError("Cannot getSingletonState for " + pluginModelBase.getClass().getName(), new Exception());
    }
    return null;
  }

  /**
   * @param featurePlugin
   */
  private static Boolean getSingletonState(IFeaturePlugin featurePlugin)
  {
    String pluginId = featurePlugin.getId();
    String pluginVersion = featurePlugin.getVersion();
    IPluginModelBase pluginModelBase = getPluginModelBase(pluginId, pluginVersion);
    if (pluginModelBase instanceof IPluginModel)
    {
      IPlugin plugin = ((IPluginModel) pluginModelBase).getPlugin();
      return getSingletonState(plugin);
    }
    if (pluginModelBase instanceof IFragmentModel)
    {
      IFragment fragment = ((IFragmentModel) pluginModelBase).getFragment();
      return getSingletonState(fragment);
    }
    if (pluginModelBase != null)
      PDEViewActivator.logError("Cannot getSingletonState for " + pluginModelBase.getClass().getName(), new Exception());
    return null;
  }

  /**
   * @param productPlugin
   */
  private static Boolean getSingletonState(IProductPlugin productPlugin)
  {
    String pluginId = productPlugin.getId();
    String pluginVersion = productPlugin.getVersion();
    IPluginModelBase pluginModelBase = getPluginModelBase(pluginId, pluginVersion);
    if (pluginModelBase instanceof IPluginModel)
    {
      IPlugin plugin = ((IPluginModel) pluginModelBase).getPlugin();
      return getSingletonState(plugin);
    }
    if (pluginModelBase instanceof IFragmentModel)
    {
      IFragment fragment = ((IFragmentModel) pluginModelBase).getFragment();
      return getSingletonState(fragment);
    }
    if (pluginModelBase != null)
      PDEViewActivator.logError("Cannot getSingletonState for " + pluginModelBase.getClass().getName(), new Exception());
    return null;
  }

  /**
   * @param fragment
   */
  private static Boolean getSingletonState(IFragment fragment)
  {
    IPluginModelBase pluginModelBase = fragment.getPluginModel();
    if (pluginModelBase instanceof ExternalPluginModelBase)
      return getSingletonState((ExternalPluginModelBase) pluginModelBase);
    if (pluginModelBase instanceof IBundlePluginModelBase)
    {
      IBundlePluginModelBase bundlePluginModel = (IBundlePluginModelBase) pluginModelBase;
      IBundle bundle = bundlePluginModel.getBundleModel().getBundle();
      IManifestHeader header = bundle.getManifestHeader(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME);
      if (header instanceof BundleSymbolicNameHeader)
        return ((BundleSymbolicNameHeader) header).isSingleton();
    }
    return null;
  }

  /**
   * @param externalPluginModelBase
   */
  private static Boolean getSingletonState(ExternalPluginModelBase externalPluginModelBase)
  {
    Object singletonState = null;

    String installLocation = externalPluginModelBase.getInstallLocation();
    if (installLocation != null)
    {
      singletonState = USE_CACHE? SINGLETONSTATE_CACHEMAP.get(installLocation) : null;
      if (singletonState == null)
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
                  singletonState = value.contains("singleton:=true");
              }
            }
            catch(Exception e)
            {
              PDEViewActivator.logError("Cannot treat " + installLocationFile, e);
            }
          }
          else if (installLocationFile.isDirectory())
          {
            Path manifestPath = Paths.get(installLocationFile.getPath(), "META-INF", "MANIFEST.MF");
            if (Files.exists(manifestPath))
            {
              try
              {
                singletonState = Files.lines(manifestPath).anyMatch(line -> line.startsWith("Bundle-SymbolicName:") && line.contains("singleton:=true"));
              }
              catch(Exception e)
              {
                PDEViewActivator.logError("Cannot treat " + manifestPath, e);
              }
            }
          }
        }

        if (singletonState == null)
          singletonState = NULL;
        if (USE_CACHE)
          SINGLETONSTATE_CACHEMAP.put(installLocation, singletonState);
      }
    }

    return singletonState == NULL? null : (Boolean) singletonState;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @param workspaceFeatureList
   */
  public static TreeParent getWorkspaceFeatureTreeParent(List<IFeatureModel> workspaceFeatureList)
  {
    TreeParent workspaceFeatureTreeParent = new TreeParent(Constants.WORKSPACE_NODE);
    workspaceFeatureTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);

    workspaceFeatureTreeParent.loadChildRunnable = () -> {
      workspaceFeatureList.stream().map(Util::getTreeParent).forEach(workspaceFeatureTreeParent::addChild);

      // sort
      workspaceFeatureTreeParent.sortChildren();
    };
    return workspaceFeatureTreeParent;
  }

  /**
   * @param externalFeatureList
   */
  public static TreeParent getTargetFeatureTreeParent(List<IFeatureModel> externalFeatureList)
  {
    TreeParent externalFeatureTreeParent = new TreeParent(Constants.TARGET_PLATFORM_NODE);
    externalFeatureTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);

    externalFeatureTreeParent.loadChildRunnable = () -> {
      externalFeatureList.stream().map(Util::getTreeParent).forEach(externalFeatureTreeParent::addChild);

      // sort
      externalFeatureTreeParent.sortChildren();
    };
    return externalFeatureTreeParent;
  }

  /**
   * @param productModel
   */
  public static TreeParent getTreeParent(IProductModel productModel)
  {
    TreeParent productTreeParent = new TreeParent(null, productModel);
    productTreeParent.foreground = Constants.PRODUCT_FOREGROUND;
    IResource underlyingResource = productModel.getUnderlyingResource();
    productTreeParent.name = underlyingResource.getName();
    IProduct product = productModel.getProduct();
    if (!VersionUtil.isEmptyVersion(product.getVersion()))
      productTreeParent.name += ' ' + PDELabelProvider.formatVersion(product.getVersion());
    productTreeParent.image = PDEViewActivator.getImage(Images.PRODUCT);

    productTreeParent.loadChildRunnable = () -> {
      List<TreeParent> elements = getElementsFromProduct(product);
      elements.forEach(productTreeParent::addChild);

      productTreeParent.sortChildren();
    };
    return productTreeParent;
  }

  /**
   * @param product
   */
  private static List<TreeParent> getElementsFromProduct(IProduct product)
  {
    List<TreeParent> elements = new ArrayList<>();

    if (product.useFeatures())
      loadFeatures(product, elements);
    else
      loadPlugins(product, elements);

    return elements;
  }

  /**
   * Load IProductFeatures
   * @param productFeatures
   * @param elements
   */
  private static void loadFeatures(IProduct product, List<TreeParent> elements)
  {
    IProductFeature[] productFeatures = product.getFeatures();
    if (productFeatures.length == 0)
      return;

    //
    TreeParent featuresTreeParent = new TreeParent(Constants.FEATURES_NODE);
    featuresTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_FEATURE_MF_OBJ);
    elements.add(featuresTreeParent);

    featuresTreeParent.loadChildRunnable = () -> {
      Stream.of(productFeatures).map(Util::getTreeParent).forEach(featuresTreeParent::addChild);

      // sort
      featuresTreeParent.sortChildren();
    };
  }

  /**
   * @param productFeature
   * @return
   */
  private static TreeParent getTreeParent(IProductFeature productFeature)
  {
    TreeParent featureTreeParent = new TreeParent(null, productFeature);
    featureTreeParent.foreground = Constants.FEATURE_FOREGROUND;

    featureTreeParent.loadChildRunnable = () -> {
      IFeatureModel featureModel = getFeatureModel(productFeature.getId(), productFeature.getVersion());
      if (featureModel != null)
      {
        IFeature feature = featureModel.getFeature();
        if (feature != null)
        {
          List<TreeParent> childElements = getElementsFromFeature(feature);
          childElements.forEach(featureTreeParent::addChild);
        }
      }
    };
    return featureTreeParent;
  }

  /**
   * Load IProductPlugins
   * @param productPlugins
   * @param elements
   */
  private static void loadPlugins(IProduct product, List<TreeParent> elements)
  {
    IProductPlugin[] productPlugins = product.getPlugins();
    if (productPlugins.length == 0)
      return;

    //
    TreeParent pluginsTreeParent = new TreeParent(Constants.PLUGINS_NODE);
    pluginsTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGIN_OBJ);
    elements.add(pluginsTreeParent);

    pluginsTreeParent.loadChildRunnable = () -> {
      Stream.of(productPlugins).map(Util::getTreeObject).forEach(pluginsTreeParent::addChild);

      // sort
      pluginsTreeParent.sortChildren();
    };
  }

  /**
   * @param productPlugin
   */
  private static TreeObject getTreeObject(IProductPlugin productPlugin)
  {
    TreeObject productPluginTreeObject = new TreeObject(null, productPlugin);
    productPluginTreeObject.foreground = Constants.PLUGIN_FOREGROUND;
    return productPluginTreeObject;
  }

  /**
   * @param feature
   */
  private static TreeParent getTreeParent(IFeature feature)
  {
    TreeParent featureTreeParent = new TreeParent(null, feature);
    featureTreeParent.foreground = Constants.FEATURE_FOREGROUND;

    featureTreeParent.loadChildRunnable = () -> {
      List<TreeParent> elements = getElementsFromFeature(feature);
      elements.forEach(featureTreeParent::addChild);
    };
    return featureTreeParent;
  }

  /**
   * @param includedFeature
   */
  private static TreeParent getTreeParent(IFeatureChild includedFeature)
  {
    TreeParent featureChildTreeParent = new TreeParent(null, includedFeature);
    featureChildTreeParent.foreground = Constants.FEATURE_FOREGROUND;

    //
    featureChildTreeParent.loadChildRunnable = () -> {
      FeatureChild featureChildImpl = (FeatureChild) includedFeature;
      IFeature referencedFeature = featureChildImpl.getReferencedFeature();
      if (referencedFeature != null)
      {
        List<TreeParent> featureElements = getElementsFromFeature(referencedFeature);
        featureElements.forEach(featureChildTreeParent::addChild);
      }
    };
    return featureChildTreeParent;
  }

  /**
   * @param featureImport
   */
  private static TreeParent getTreeParent(IFeatureImport featureImport)
  {
    TreeParent featureChildTreeParent = new TreeParent(null, featureImport);
    featureChildTreeParent.foreground = Constants.FEATURE_FOREGROUND;

    //
    if (featureImport.getFeature() != null)
    {
      featureChildTreeParent.loadChildRunnable = () -> {
        List<TreeParent> featureElements = getElementsFromFeature(featureImport.getFeature());
        featureElements.forEach(featureChildTreeParent::addChild);
      };
    }
    return featureChildTreeParent;
  }

  /**
   * @param pluginBase
   */
  private static TreeObject getTreeObject(IPluginBase pluginBase)
  {
    TreeObject treeObject = new TreeObject(null, pluginBase);
    treeObject.foreground = Constants.PLUGIN_FOREGROUND;

    return treeObject;
  }

  /**
   * @param feature
   */
  private static List<TreeParent> getElementsFromFeature(IFeature feature)
  {
    List<TreeParent> elements = new ArrayList<>();

    // included plugins
    loadIncludedPlugins(feature, elements);

    // included features
    loadIncludedFeatures(feature, elements);

    // required features/plugins
    loadRequiredPlugins(feature, elements);

    return elements;
  }

  /**
   * Load Included Plugins
   * @param feature
   * @param elements
   */
  private static void loadIncludedPlugins(IFeature feature, List<TreeParent> elements)
  {
    IFeaturePlugin[] includedPlugins = feature.getPlugins();
    if (includedPlugins != null && includedPlugins.length != 0)
    {
      TreeParent includedPluginsTreeParent = new TreeParent(Constants.INCLUDED_PLUGINS_NODE);
      includedPluginsTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGINS_FRAGMENTS);
      elements.add(includedPluginsTreeParent);

      includedPluginsTreeParent.loadChildRunnable = () -> {
        IFeaturePlugin[] reloadedIncludedPlugins = feature.getPlugins();
        Stream.of(reloadedIncludedPlugins).map(Util::getTreeObject).forEach(includedPluginsTreeParent::addChild);

        // sort
        includedPluginsTreeParent.sortChildren();
      };
    }
  }

  /**
   * @param featurePlugin
   */
  private static TreeObject getTreeObject(IFeaturePlugin featurePlugin)
  {
    TreeObject childTreeObject = new TreeObject(null, featurePlugin);
    childTreeObject.foreground = Constants.PLUGIN_FOREGROUND;
    return childTreeObject;
  }

  /**
   * Load Included Features
   * @param feature
   * @param elements
   */
  private static void loadIncludedFeatures(IFeature feature, List<TreeParent> elements)
  {
    IFeatureChild[] includedFeatures = feature.getIncludedFeatures();
    if (includedFeatures != null && includedFeatures.length != 0)
    {
      //
      TreeParent includedFeaturesTreeParent = new TreeParent(Constants.INCLUDED_FEATURES_NODE);
      includedFeaturesTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_FEATURE_OBJ);
      elements.add(includedFeaturesTreeParent);

      includedFeaturesTreeParent.loadChildRunnable = () -> {
        IFeatureChild[] reloadedIncludedFeatures = feature.getIncludedFeatures();
        Stream.of(reloadedIncludedFeatures).map(Util::getTreeParent).forEach(includedFeaturesTreeParent::addChild);

        // sort
        includedFeaturesTreeParent.sortChildren();
      };
    }
  }

  /**
   * Load Required Plugins
   * @param feature
   * @param elements
   */
  private static void loadRequiredPlugins(IFeature feature, List<TreeParent> elements)
  {
    IFeatureImport[] featureImports = feature.getImports();
    if (featureImports != null && featureImports.length != 0)
    {
      //
      TreeParent requiredFeaturesTreeParent = new TreeParent(Constants.REQUIRED_PLUGINS_NODE);
      requiredFeaturesTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_REQ_PLUGINS_OBJ);
      elements.add(requiredFeaturesTreeParent);

      requiredFeaturesTreeParent.loadChildRunnable = () -> {
        IFeatureImport[] reloadedFeatureImports = feature.getImports();

        for(IFeatureImport featureImport : reloadedFeatureImports)
        {
          if (featureImport.getType() == IFeatureImport.FEATURE)
          {
            TreeParent featureChildTreeParent = getTreeParent(featureImport);
            requiredFeaturesTreeParent.addChild(featureChildTreeParent);
          }
          else if (featureImport.getType() == IFeatureImport.PLUGIN)
          {
            TreeObject childTreeObject = new TreeObject(null, featureImport);
            childTreeObject.foreground = Constants.PLUGIN_FOREGROUND;
            requiredFeaturesTreeParent.addChild(childTreeObject);
          }
        }

        // sort
        requiredFeaturesTreeParent.sortChildren();
      };
    }
  }

  /**
   * @param featureModel
   */
  private static TreeParent getTreeParent(IFeatureModel featureModel)
  {
    TreeParent treeParent = new TreeParent(null, featureModel);
    treeParent.foreground = Constants.FEATURE_FOREGROUND;

    treeParent.loadChildRunnable = () -> {
      IFeature feature = featureModel.getFeature();
      List<TreeParent> childElements = getElementsFromFeature(feature);
      childElements.forEach(treeParent::addChild);
    };
    return treeParent;
  }

  /**
   * @param launchConfiguration
   */
  public static TreeParent getLaunchConfigurationTreeParent(ILaunchConfiguration launchConfiguration)
  {
    TreeParent launchConfigurationTreeParent = new TreeParent(null, launchConfiguration);
    launchConfigurationTreeParent.image = PDEViewActivator.getImage(Images.LAUNCH_CONFIGURATION);
    launchConfigurationTreeParent.foreground = Constants.LAUNCH_CONFIGURATION_FOREGROUND;

    launchConfigurationTreeParent.loadChildRunnable = () -> {
      List<TreeParent> elements = getElementsFromLaunchConfiguration(launchConfiguration);
      elements.forEach(launchConfigurationTreeParent::addChild);

      // sort
      launchConfigurationTreeParent.sortChildren();
    };
    return launchConfigurationTreeParent;
  }

  /**
   * @param launchConfiguration
   */
  private static List<TreeParent> getElementsFromLaunchConfiguration(ILaunchConfiguration launchConfiguration)
  {
    List<TreeParent> elements = new ArrayList<>();

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
      String msg = "Cannot load launchConfiguration " + launchConfiguration;
      if (MESSAGE_ALREADY_PRINTED_SET.add(msg))
        PDEViewActivator.logError(msg, e);
    }

    return elements;
  }

  /**
   * Load plugins from launchConfiguration
   * @param launchConfiguration
   * @param elements
   */
  private static void loadPluginsFromLaunchConfiguration(ILaunchConfiguration launchConfiguration, List<TreeParent> elements)
  {
    loadElements(launchConfiguration, Constants.WORKSPACE_NODE, IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS, elements);

    loadElements(launchConfiguration, Constants.TARGET_PLATFORM_NODE, IPDELauncherConstants.SELECTED_TARGET_PLUGINS, elements);
  }

  /**
   * Load elements from launchConfiguration
   * @param launchConfiguration
   * @param name
   * @param attributeKey
   * @param elements
   */
  private static void loadElements(ILaunchConfiguration launchConfiguration, String name, String attributeKey, List<TreeParent> elements)
  {
    List<IPluginBase> pluginBases = loadPlugins(launchConfiguration, attributeKey);
    if (!pluginBases.isEmpty())
    {
      TreeParent treeParent = new TreeParent(name, null);
      treeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);
      elements.add(treeParent);

      //
      treeParent.loadChildRunnable = () -> {
        pluginBases.stream().map(Util::getTreeObject).forEach(treeParent::addChild);

        // sort
        treeParent.sortChildren();
      };
    }
  }

  /**
   * Load plugins from launchConfiguration
   * @param launchConfiguration
   * @param attributeKey
   */
  private static List<IPluginBase> loadPlugins(ILaunchConfiguration launchConfiguration, String attributeKey)
  {
    List<IPluginBase> pluginBases = new ArrayList<>();
    try
    {
      String selected_target_plugins = launchConfiguration.getAttribute(attributeKey, "");
      for(String str : selected_target_plugins.split(","))
      {
        int index = str.indexOf(':');
        if (index >= 0)
          str = str.substring(0, index);
        index = str.indexOf('@');
        if (index >= 0)
          str = str.substring(0, index);
        index = str.indexOf('*');
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
      PDEViewActivator.logError(e.toString(), e);
    }

    return pluginBases;
  }

  /**
   * Load features from launchConfiguration
   * @param launchConfiguration
   * @param elements
   */
  private static void loadFeaturesFromLaunchConfiguration(ILaunchConfiguration launchConfiguration, List<TreeParent> elements)
  {
    try
    {
      List<IFeatureModel> featureModels = new ArrayList<>();
      FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
      Set<String> selected_features = launchConfiguration.getAttribute(IPDELauncherConstants.SELECTED_FEATURES, Collections.emptySet());
      for(String str : selected_features)
      {
        if (str.isEmpty())
          continue;
        str = str.substring(0, str.indexOf(':'));
        //        str = str.substring(0, str.indexOf('@'));
        int index = str.indexOf('*');
        String pluginId = str;

        IFeatureModel featureModel = null;
        if (index >= 0)
        {
          pluginId = str.substring(0, index);
          String pluginVersion = str.substring(index + 1);
          featureModel = manager.findFeatureModel(pluginId, pluginVersion);
        }
        if (featureModel == null)
          featureModel = manager.findFeatureModel(pluginId);

        if (featureModel != null)
          featureModels.add(featureModel);
      }

      featureModels.stream().map(Util::getTreeParent).forEach(elements::add);

      // Additional plugins
      Set<String> additional_plugins = launchConfiguration.getAttribute(IPDELauncherConstants.ADDITIONAL_PLUGINS, Collections.emptySet());
      if (!additional_plugins.isEmpty())
      {
        List<IPluginBase> pluginBases = new ArrayList<>();
        for(String additional_plugin : additional_plugins)
        {
          if (additional_plugin.isEmpty())
            continue;
          if (additional_plugin.endsWith("default:false"))
            continue;

          //
          String pluginId = additional_plugin;
          String pluginVersion = null;

          int index = additional_plugin.indexOf(':');
          if (index >= 0)
          {
            pluginId = additional_plugin.substring(0, index);
            pluginVersion = additional_plugin.substring(index + 1);
            if (pluginVersion.endsWith("default:true"))
              pluginVersion = pluginVersion.substring(0, pluginVersion.length() - "default:true".length() - 1);
          }

          IPluginModelBase model = null;
          if (pluginVersion != null)
            model = PluginRegistry.findModel(pluginId, pluginVersion, IMatchRules.PERFECT, null);
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

        if (!pluginBases.isEmpty())
        {
          TreeParent additionalPluginsTreeParent = new TreeParent(Constants.ADDITIONAL_PLUGIN_NODE);
          additionalPluginsTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);
          elements.add(additionalPluginsTreeParent);

          //
          pluginBases.stream().map(Util::getTreeObject).forEach(additionalPluginsTreeParent::addChild);

          // sort
          additionalPluginsTreeParent.sortChildren();
        }
      }
    }
    catch(CoreException e)
    {
      PDEViewActivator.logError(e.toString(), e);
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Open PDE object
   * @param pdeObject
   */
  public static String getId(Object pdeObject)
  {
    if (pdeObject instanceof IPlugin)
      return ((IPlugin) pdeObject).getId();

    else if (pdeObject instanceof IFeaturePlugin)
      return ((IFeaturePlugin) pdeObject).getId();

    else if (pdeObject instanceof IFeatureChild)
      return ((IFeatureChild) pdeObject).getId();

    else if (pdeObject instanceof IFeatureImport)
      return ((IFeatureImport) pdeObject).getId();

    else if (pdeObject instanceof IFeature)
      return ((IFeature) pdeObject).getId();

    else if (pdeObject instanceof IProduct)
      return ((IProduct) pdeObject).getId();

    else if (pdeObject instanceof IProductFeature)
      return ((IProductFeature) pdeObject).getId();

    else if (pdeObject instanceof IProductPlugin)
      return ((IProductPlugin) pdeObject).getId();

    else if (pdeObject instanceof IFragment)
      return ((IFragment) pdeObject).getId();

    else if (pdeObject instanceof IFeatureModel)
      return ((IFeatureModel) pdeObject).getFeature().getId();

    else if (pdeObject instanceof ILaunchConfiguration)
      return ((ILaunchConfiguration) pdeObject).getName();

    else if (pdeObject != null)
    {
      String msg = "Unsupported id for " + pdeObject.getClass().getName();
      if (MESSAGE_ALREADY_PRINTED_SET.add(msg))
        PDEViewActivator.logError(msg, new Exception());
    }

    return null;
  }

  static enum TYPE
  {
    PLUGIN, FEATURE, PRODUCT, LAUNCH_CONFIGURATION
  }

  /**
   * Open PDE object
   * @param pdeObject
   */
  public static TYPE getType(Object pdeObject)
  {
    if (pdeObject instanceof IPlugin)
      return TYPE.PLUGIN;

    else if (pdeObject instanceof IFragment)
      return TYPE.PLUGIN;

    else if (pdeObject instanceof IFeaturePlugin)
      return TYPE.PLUGIN;

    else if (pdeObject instanceof IFeatureChild)
      return TYPE.FEATURE;

    else if (pdeObject instanceof IFeatureImport)
      return ((IFeatureImport) pdeObject).getType() == IFeatureImport.PLUGIN? TYPE.PLUGIN : TYPE.FEATURE;

    else if (pdeObject instanceof IFeature)
      return TYPE.FEATURE;

    else if (pdeObject instanceof IProduct)
      return TYPE.PRODUCT;

    else if (pdeObject instanceof IProductFeature)
      return TYPE.FEATURE;

    else if (pdeObject instanceof IProductPlugin)
      return TYPE.PLUGIN;

    else if (pdeObject instanceof IFeatureModel)
      return TYPE.FEATURE;

    else if (pdeObject instanceof ILaunchConfiguration)
      return TYPE.LAUNCH_CONFIGURATION;

    else if (pdeObject != null)
    {
      String msg = "Unsupported type for " + pdeObject.getClass().getName();
      if (MESSAGE_ALREADY_PRINTED_SET.add(msg))
        PDEViewActivator.logError(msg, new Exception());
    }

    return null;
  }

  //  /////////////////////////////////////////////////////////////////////////////////////////////////
  //
  //  /**
  //   * @param pdeObject1
  //   * @param pdeObject2
  //   */
  //  public static boolean equals(Object pdeObject1, Object pdeObject2)
  //  {
  //    String id1 = getId(pdeObject1);
  //    String id2 = getId(pdeObject2);
  //    if (!Objects.equals(id1, id2))
  //      return false;
  //
  //    TYPE type1 = getType(pdeObject1);
  //    TYPE type2 = getType(pdeObject2);
  //    if (!Objects.equals(type1, type2))
  //      return false;
  //
  //    //    System.err.println(pdeObject1.getClass() + " " + pdeObject2.getClass());
  //    return true;
  //  }

}
