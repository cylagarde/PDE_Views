package cl.pde.views.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.core.plugin.IMatchRules;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import cl.pde.Images;
import cl.pde.PDEViewActivator;
import cl.pde.views.Util;
import cl.pde.views.plugin.PluginView;

/**
 * The class <b>GetAllProductsAction</b> allows to.<br>
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
    setToolTipText("Get all plugins");
    setImageDescriptor(PDEViewActivator.getImageDescriptor(Images.GET_ALL_PLUGINS));
  }

  @Override
  public void run()
  {
    Set<IPluginModelBase> pluginSet = new HashSet<>();

    Bundle[] bundles = FrameworkUtil.getBundle(getClass()).getBundleContext().getBundles();
    for(Bundle bundle : bundles)
    {
      String symbolicName = bundle.getSymbolicName();
      String version = bundle.getVersion().toString();
      int matchRule = IMatchRules.PERFECT;
      IPluginModelBase pluginModelBase = Util.getPluginModelBase(symbolicName, version, matchRule);
      if (pluginModelBase != null)
        pluginSet.add(pluginModelBase);
      else
        System.err.println("Cannot getPluginModelBase " + symbolicName + " " + version);
    }
    System.out.println(bundles.length);

    //    Set<Object> alreadyTreatedCacheSet = new HashSet<>();
    //
    //    //
    //    Predicate<IResource> filePredicate = resource -> {
    //      if (!alreadyTreatedCacheSet.add(resource.getLocation()))
    //        return false;
    //
    //      if (resource instanceof IFile && resource.getName().endsWith(".product"))
    //      {
    //        IFile productFile = (IFile) resource;
    //
    //        // load product
    //        WorkspaceProductModel workspaceProductModel = new WorkspaceProductModel(productFile, false);
    //        try
    //        {
    //          workspaceProductModel.load();
    //
    //          // check if product loaded
    //          if (workspaceProductModel.isLoaded())
    //            productModelSet.add(workspaceProductModel);
    //        }
    //        catch(CoreException e)
    //        {
    //          PDEViewActivator.logError("Cannot load product " + productFile, e);
    //        }
    //      }
    //
    //      return true;
    //    };
    //
    //    // search
    //    MultiStatus errorStatus = new MultiStatus(PDEViewActivator.PLUGIN_ID, IStatus.ERROR, "Some errors were found when processing", null);
    //
    //    NullProgressMonitor nullMonitor = new NullProgressMonitor();
    //
    //    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    //    IProject[] projects = root.getProjects();
    //    for(IProject workspaceProject : projects)
    //    {
    //      if (!workspaceProject.isOpen())
    //      {
    //        alreadyTreatedCacheSet.add(workspaceProject.getLocation());
    //        continue;
    //      }
    //
    //      try
    //      {
    //        Util.traverseContainer(workspaceProject, filePredicate, nullMonitor);
    //      }
    //      catch(CoreException e)
    //      {
    //        errorStatus.add(e.getStatus());
    //      }
    //    }
    //
    //    if (errorStatus.getChildren().length != 0)
    //      MessageDialog.openError(pluginView.getTreeViewer().getTree().getShell(), "Error", "Some errors were found when processing: " + errorStatus.getMessage());
    //
    //
    TreeViewer pluginViewer = pluginView.getTreeViewer();
    pluginViewer.getControl().setRedraw(false);
    try
    {
      pluginView.setInput(pluginSet);
    }
    finally
    {
      pluginViewer.getControl().setRedraw(true);
    }
  }
}
