package cl.pde.views;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
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
import org.eclipse.pde.internal.ui.PDEUIMessages;
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

import cl.pde.Activator;
import cl.pde.Images;

/**
 * The class <b>Util</b> allows to.<br>
 */
public class Util
{
  public static final Comparator<Object> PDE_LABEL_COMPARATOR = Comparator.comparing(PDEPlugin.getDefault().getLabelProvider()::getText, String.CASE_INSENSITIVE_ORDER);

  /**
   * Traverse tree
   * @param tree
   * @param predicate
   * @param monitor
   */
  public static int traverseTree(Tree tree, Predicate<Object> predicate, IProgressMonitor monitor)
  {
    int count = 1;
    TreeItem[] items = tree.getItems();
    //    monitor.beginTask("", elements.length);
    SubMonitor subMonitor = SubMonitor.convert(monitor, items.length);
    for(TreeItem item : items)
    {
      count += traverseItem(item, predicate, subMonitor.split(1));
      if (subMonitor.isCanceled())
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
  public static int traverseItem(TreeItem item, Predicate<Object> predicate, IProgressMonitor monitor)
  {
    int count = 1;
    if (predicate.test(item.getData()))
    {
      TreeItem[] items = item.getItems();
      SubMonitor subMonitor = SubMonitor.convert(monitor, items.length);
      for(TreeItem child : items)
      {
        count += traverseItem(child, predicate, subMonitor.split(1));
        if (subMonitor.isCanceled())
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
    //    monitor.beginTask("", elements.length);
    SubMonitor subMonitor = SubMonitor.convert(monitor, elements.length);
    for(Object element : elements)
    {
      count += traverseElement(treeContentProvider, element, predicate, subMonitor.split(1));
      if (subMonitor.isCanceled())
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
      SubMonitor subMonitor = SubMonitor.convert(monitor, children.length);
      for(Object child : children)
      {
        count += traverseElement(treeContentProvider, child, predicate, subMonitor.split(1));
        if (subMonitor.isCanceled())
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
      SubMonitor subMonitor = SubMonitor.convert(monitor, members.length);
      for(IResource member : members)
      {
        if (member instanceof IContainer)
          traverseContainer((IContainer) member, filePredicate, subMonitor.split(1));
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

    else if (pdeObject != null)
      Activator.logError("Cannot open data " + pdeObject.getClass().getName(), new Exception());
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
        Activator.logError("Cannot open product " + productModelResource, e);
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
    {
      String message = "Cannot found plugin with id=" + pluginId + " and version=" + pluginVersion;
      Activator.logError(message);
      Shell shell = Display.getDefault().getActiveShell();
      MessageDialog.openError(shell, "Error", message);
    }
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

    if (pdeObject instanceof IProductFeature)
      return getProductFeatureLocation((IProductFeature) pdeObject);

    if (pdeObject instanceof IProductPlugin)
      return getProductPluginLocation((IProductPlugin) pdeObject);

    if (pdeObject instanceof IFragment)
      return getFragmentLocation((IFragment) pdeObject);

    if (pdeObject instanceof IFeatureModel)
      return getFeatureModelLocation((IFeatureModel) pdeObject);

    if (pdeObject instanceof IProductModel)
      return getProductModelLocation((IProductModel) pdeObject);

    return null;
  }

  /**
   * Get IProductModel location
   * @param productModel
   */
  private static String getProductModelLocation(IProductModel productModel)
  {
    return productModel.getInstallLocation();
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
    //    System.out.println(" " + pdeObject);
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

    if (pdeObject instanceof IProductModel)
      return getProductModelResource((IProductModel) pdeObject);

    return null;
  }

  /**
   * Get productModel resource
   * @param productModel
   */
  private static IResource getProductModelResource(IProductModel productModel)
  {
    return productModel.getUnderlyingResource();
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

  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @param workspaceFeatureList
   */
  public static TreeParent getWorkspaceFeatureTreeParent(List<IFeatureModel> workspaceFeatureList)
  {
    TreeParent workspaceFeatureTreeParent = new TreeParent(Constants.WORKSPACE_FEATURE);
    workspaceFeatureTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);

    workspaceFeatureTreeParent.loadChildRunnable = () -> {
      workspaceFeatureList.stream().map(Util::getTreeParent).forEach(workspaceFeatureTreeParent::addChild);
    };
    return workspaceFeatureTreeParent;
  }

  /**
   * @param externalFeatureList
   */
  public static TreeParent getTargetFeatureTreeParent(List<IFeatureModel> externalFeatureList)
  {
    TreeParent externalFeatureTreeParent = new TreeParent(Constants.TARGET_FEATURE);
    externalFeatureTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);

    externalFeatureTreeParent.loadChildRunnable = () -> {
      externalFeatureList.stream().map(Util::getTreeParent).forEach(externalFeatureTreeParent::addChild);
    };
    return externalFeatureTreeParent;
  }

  /**
   * @param productModel
   */
  public static TreeParent getProductModelTreeParent(IProductModel productModel)
  {
    TreeParent productTreeParent = new TreeParent(null, productModel);
    productTreeParent.foreground = Constants.PRODUCT_FOREGROUND;
    IResource underlyingResource = productModel.getUnderlyingResource();
    productTreeParent.name = underlyingResource.getName();
    IProduct product = productModel.getProduct();
    if (!VersionUtil.isEmptyVersion(product.getVersion()))
      productTreeParent.name += ' ' + PDELabelProvider.formatVersion(product.getVersion());
    productTreeParent.image = Activator.getImage(Images.PRODUCT);

    productTreeParent.loadChildRunnable = () -> {
      List<TreeParent> elements = getElementsFromProduct(product);
      elements.forEach(productTreeParent::addChild);
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
    TreeParent featuresTreeParent = new TreeParent("Features");
    featuresTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_FEATURE_MF_OBJ);
    elements.add(featuresTreeParent);

    featuresTreeParent.loadChildRunnable = () -> {
      // sort
      Arrays.sort(productFeatures, Util.PDE_LABEL_COMPARATOR);

      for(IProductFeature productFeature : productFeatures)
      {
        TreeParent featureTreeParent = new TreeParent(null, productFeature);
        featureTreeParent.foreground = Constants.FEATURE_FOREGROUND;
        featuresTreeParent.addChild(featureTreeParent);

        featureTreeParent.loadChildRunnable = () -> {
          FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
          IFeatureModel featureModel = manager.findFeatureModel(productFeature.getId(), productFeature.getVersion());
          if (featureModel != null)
          {
            IFeature feature = featureModel.getFeature();
            if (feature != null)
            {
              List<TreeParent> childElements = Util.getElementsFromFeature(feature);
              childElements.forEach(featureTreeParent::addChild);
            }
          }
        };
      }
    };
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
    TreeParent pluginsTreeParent = new TreeParent("Plugins");
    pluginsTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGIN_OBJ);
    elements.add(pluginsTreeParent);

    pluginsTreeParent.loadChildRunnable = () -> {

      // sort
      Arrays.sort(productPlugins, Util.PDE_LABEL_COMPARATOR);

      for(IProductPlugin productPlugin : productPlugins)
      {
        TreeObject productPluginTreeObject = new TreeObject(null, productPlugin);
        productPluginTreeObject.foreground = Constants.PLUGIN_FOREGROUND;
        pluginsTreeParent.addChild(productPluginTreeObject);
      }
    };
  }

  /**
   * @param feature
   */
  public static TreeParent getTreeParent(IFeature feature)
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
  public static TreeParent getTreeParent(IFeatureChild includedFeature)
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
  public static TreeParent getTreeParent(IFeatureImport featureImport)
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
  public static TreeObject getTreeObject(IPluginBase pluginBase)
  {
    TreeObject treeObject = new TreeObject(null, pluginBase);
    treeObject.foreground = Constants.PLUGIN_FOREGROUND;

    return treeObject;
  }

  /**
   * @param feature
   */
  public static List<TreeParent> getElementsFromFeature(IFeature feature)
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
      //
      TreeParent includedPluginsTreeParent = new TreeParent(PDEUIMessages.FeatureEditor_ReferencePage_title);
      includedPluginsTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGINS_FRAGMENTS);
      elements.add(includedPluginsTreeParent);

      includedPluginsTreeParent.loadChildRunnable = () -> {
        // sort
        Arrays.sort(includedPlugins, Util.PDE_LABEL_COMPARATOR);
        for(IFeaturePlugin featurePlugin : includedPlugins)
        {
          TreeObject childTreeObject = new TreeObject(null, featurePlugin);
          childTreeObject.foreground = Constants.PLUGIN_FOREGROUND;
          includedPluginsTreeParent.addChild(childTreeObject);
        }
      };
    }
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
      TreeParent includedFeaturesTreeParent = new TreeParent(PDEUIMessages.FeatureEditor_IncludesPage_title);
      includedFeaturesTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_FEATURE_OBJ);
      elements.add(includedFeaturesTreeParent);

      includedFeaturesTreeParent.loadChildRunnable = () -> {
        // sort
        Arrays.sort(includedFeatures, Util.PDE_LABEL_COMPARATOR);
        for(IFeatureChild includedFeature : includedFeatures)
        {
          TreeParent featureChildTreeParent = getTreeParent(includedFeature);
          includedFeaturesTreeParent.addChild(featureChildTreeParent);
        }
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
      TreeParent requiredFeaturesTreeParent = new TreeParent(PDEUIMessages.FeatureEditor_DependenciesPage_title);
      requiredFeaturesTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_REQ_PLUGINS_OBJ);
      elements.add(requiredFeaturesTreeParent);

      requiredFeaturesTreeParent.loadChildRunnable = () -> {
        // sort
        Arrays.sort(featureImports, Util.PDE_LABEL_COMPARATOR);
        for(IFeatureImport featureImport : featureImports)
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
      };
    }
  }

  /**
   * @param featureModel
   */
  public static TreeParent getTreeParent(IFeatureModel featureModel)
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
    launchConfigurationTreeParent.image = Activator.getImage(Images.LAUNCH_CONFIGURATION);
    launchConfigurationTreeParent.foreground = Constants.LAUNCH_CONFIGURATION_FOREGROUND;

    launchConfigurationTreeParent.loadChildRunnable = () -> {
      List<TreeParent> elements = getElementsFromLaunchConfiguration(launchConfiguration);
      elements.forEach(launchConfigurationTreeParent::addChild);
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
  private static void loadPluginsFromLaunchConfiguration(ILaunchConfiguration launchConfiguration, List<TreeParent> elements)
  {
    TreeParent workspacePlugins = createTreeParent(launchConfiguration, Constants.WORKSPACE_FEATURE, IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS);
    elements.add(workspacePlugins);

    TreeParent externalPlugins = createTreeParent(launchConfiguration, Constants.TARGET_FEATURE, IPDELauncherConstants.SELECTED_TARGET_PLUGINS);
    elements.add(externalPlugins);
  }

  /**
   * Create TreeParent from launchConfiguration
   * @param launchConfiguration
   * @param name
   * @param attributeKey
   */
  private static TreeParent createTreeParent(ILaunchConfiguration launchConfiguration, String name, String attributeKey)
  {
    TreeParent treeParent = new TreeParent(name, null);
    treeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);

    //
    treeParent.loadChildRunnable = () -> {
      List<IPluginBase> pluginBases = loadPlugins(launchConfiguration, attributeKey);

      // sort
      Collections.sort(pluginBases, Util.PDE_LABEL_COMPARATOR);

      //
      pluginBases.stream().map(Util::getTreeObject).forEach(treeParent::addChild);
    };

    return treeParent;
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

      // sort
      Collections.sort(featureModels, Util.PDE_LABEL_COMPARATOR);
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
          TreeParent additionalPluginsTreeParent = new TreeParent(Constants.ADDITIONAL_PLUGIN);
          additionalPluginsTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);
          elements.add(additionalPluginsTreeParent);

          // sort
          Collections.sort(pluginBases, Util.PDE_LABEL_COMPARATOR);

          //
          pluginBases.stream().map(Util::getTreeObject).forEach(additionalPluginsTreeParent::addChild);
        }
      }
    }
    catch(CoreException e)
    {
      e.printStackTrace();
      Activator.logError(e.toString(), e);
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

    else if (pdeObject != null)
      Activator.logError("Cannot get id " + pdeObject.getClass().getName(), new Exception());
    return null;
  }

}
