package cl.pde.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import cl.pde.PDEViewActivator;
import cl.pde.views.launchconfiguration.LaunchConfigurationView;

/**
 * The class <b>OpenInLaunchConfigurationViewHandler</b> allows to.<br>
 */
public class OpenInLaunchConfigurationViewHandler extends AbstractHandler
{
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    ISelection selection = HandlerUtil.getCurrentSelection(event);
    if (selection instanceof IStructuredSelection)
    {
      ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
      Collection<ILaunchConfiguration> launchConfigurations = new ArrayList<>();

      for(Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();)
      {
        Object element = it.next();
        if (element instanceof IFile)
        {
          IFile file = (IFile) element;
          if (!file.getName().endsWith(".launch"))
            continue;

          // load launch configuration
          ILaunchConfiguration launchConfiguration = launchManager.getLaunchConfiguration(file);

          launchConfigurations.add(launchConfiguration);
        }
      }

      if (!launchConfigurations.isEmpty())
        openInLaunchConfigurationView(event, launchConfigurations);
    }

    return null;
  }

  /**
   * Open in LaunchConfigurationView
   * @param event
   * @param launchConfigurations
   */
  private void openInLaunchConfigurationView(ExecutionEvent event, Collection<ILaunchConfiguration> launchConfigurations)
  {
    IWorkbenchPage workbenchPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

    try
    {
      // get LaunchConfigurationView
      IViewPart showView = workbenchPage.showView(LaunchConfigurationView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
      LaunchConfigurationView launchConfigurationView = (LaunchConfigurationView) showView;
      TreeViewer launchConfigurationViewer = launchConfigurationView.getTreeViewer();

      launchConfigurationViewer.getControl().setRedraw(false);
      try
      {
        launchConfigurationView.setInput(launchConfigurations);

        //
        if (launchConfigurations.size() == 1)
          launchConfigurationViewer.expandToLevel(4);
      }
      finally
      {
        launchConfigurationViewer.getControl().setRedraw(true);
      }
    }
    catch(PartInitException e)
    {
      String message = "Cannot open Launch configuration view : " + e;
      PDEViewActivator.logError(message, e);
      Shell shell = HandlerUtil.getActiveShell(event);
      MessageDialog.openError(shell, "Error", message);
    }
  }
}
