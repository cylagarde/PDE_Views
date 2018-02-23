package cl.pde.views.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
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
    setText("Get all launch configurations in workspace");
    setToolTipText(getText());
    setImageDescriptor(PDEViewActivator.getImageDescriptor(Images.GET_ALL_LAUNCH_CONFIGURATIONS));
  }

  @Override
  public void run()
  {
    Set<ILaunchConfiguration> launchConfigurationSet = new HashSet<>();
    Set<Object> alreadyTreatedCacheSet = new HashSet<>();
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();

    //
    Predicate<IResource> filePredicate = resource -> {
      if (!alreadyTreatedCacheSet.add(resource.getLocation()))
        return false;

      if (resource instanceof IFile && resource.getName().endsWith(".launch"))
      {
        IFile launchConfigurationFile = (IFile) resource;

        // load launch configuration
        ILaunchConfiguration launchConfiguration = launchManager.getLaunchConfiguration(launchConfigurationFile);
        launchConfigurationSet.add(launchConfiguration);
      }

      return true;
    };

    // search
    MultiStatus errorStatus = new MultiStatus(PDEViewActivator.PLUGIN_ID, IStatus.ERROR, "Some errors were found when processing", null);

    NullProgressMonitor nullMonitor = new NullProgressMonitor();

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject[] projects = root.getProjects();
    for(IProject workspaceProject : projects)
    {
      if (!workspaceProject.isOpen())
      {
        alreadyTreatedCacheSet.add(workspaceProject.getLocation());
        continue;
      }

      try
      {
        Util.traverseContainer(workspaceProject, filePredicate, nullMonitor);
      }
      catch(CoreException e)
      {
        errorStatus.add(e.getStatus());
      }
    }

    if (errorStatus.getChildren().length != 0)
      MessageDialog.openError(launchConfigurationView.getTreeViewer().getTree().getShell(), "Error", "Some errors were found when processing: " + errorStatus.getMessage());

    //
    TreeViewer launchConfigurationViewer = launchConfigurationView.getTreeViewer();
    launchConfigurationViewer.getControl().setRedraw(false);
    try
    {
      launchConfigurationView.setInput(launchConfigurationSet);
    }
    finally
    {
      launchConfigurationViewer.getControl().setRedraw(true);
    }
  }
}
