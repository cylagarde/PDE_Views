package cl.pde.views.actions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;

import cl.pde.Images;
import cl.pde.PDEViewActivator;
import cl.pde.views.Util;
import cl.pde.views.launchconfiguration.LaunchConfigurationView;

/**
 * The class <b>GetAllLaunchConfigurationsAction</b> allows to search and display all launch configurations from workspace and target platform.<br>
 */
public class GetAllLaunchConfigurationsAction extends Action
{
  LaunchConfigurationView launchConfigurationView;

  /**
   * Constructor
   * @param launchConfigurationView
   */
  public GetAllLaunchConfigurationsAction(LaunchConfigurationView launchConfigurationView)
  {
    this.launchConfigurationView = launchConfigurationView;
    setText("Refresh all launch configurations in workspace");
    setToolTipText(getText());
    setImageDescriptor(PDEViewActivator.getImageDescriptor(Images.GET_ALL_LAUNCH_CONFIGURATIONS));
  }

  @Override
  public void run()
  {
    try
    {
      //    Set<ILaunchConfiguration> launchConfigurationSet = new HashSet<>();
      //    Set<Object> alreadyTreatedCacheSet = new HashSet<>();
      //    //
      //    Predicate<IResource> filePredicate = resource -> {
      //      if (!alreadyTreatedCacheSet.add(resource.getLocation()))
      //        return false;
      //
      //      if (resource instanceof IFile && resource.getName().endsWith(".launch"))
      //      {
      //        IFile launchConfigurationFile = (IFile) resource;
      //
      //        // load launch configuration
      //        ILaunchConfiguration launchConfiguration = launchManager.getLaunchConfiguration(launchConfigurationFile);
      //        launchConfigurationSet.add(launchConfiguration);
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
      //      MessageDialog.openError(launchConfigurationView.getTreeViewer().getTree().getShell(), "Error", "Some errors were found when processing: " + errorStatus.getMessage());

      Object[] eclipseApplications = Util.getAllEclipseApplications().filter(distinctByKey(ILaunchConfiguration::getLocation)).toArray();

      //
      TreeViewer launchConfigurationViewer = launchConfigurationView.getTreeViewer();
      launchConfigurationViewer.getControl().setRedraw(false);
      try
      {
        launchConfigurationView.setInput(eclipseApplications);
      }
      finally
      {
        launchConfigurationViewer.getControl().setRedraw(true);
      }
    }
    catch(Exception e)
    {
      MessageDialog.openError(launchConfigurationView.getTreeViewer().getTree().getShell(), "Error", "Exception: " + e.getMessage());
    }
  }

  /**
   * Create predicate
   * @param keyExtractor
   */
  public static <E> Predicate<E> distinctByKey(Function<? super E, ?> keyExtractor)
  {
    Map<Object, Boolean> cacheMap = new ConcurrentHashMap<>();
    return e -> {
      Object key = keyExtractor.apply(e);
      if (key == null)
        return true;
      return cacheMap.putIfAbsent(key, Boolean.TRUE) == null;
    };
  }
}
