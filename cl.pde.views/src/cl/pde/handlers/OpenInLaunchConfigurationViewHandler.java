package cl.pde.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import cl.pde.Activator;
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
      for(Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();)
      {
        Object element = it.next();
        if (element instanceof IFile)
        {
          IFile file = (IFile) element;
          if (file.getName().endsWith(".launch"))
            openInLaunchConfigurationView(event, file);
        }
      }
    }

    return null;
  }

  /**
   * @param event
   * @param launchConfigurationFile
   */
  private void openInLaunchConfigurationView(ExecutionEvent event, IFile launchConfigurationFile)
  {
    IWorkbenchPage workbenchPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
    try
    {
      workbenchPage.showView(LaunchConfigurationView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
    }
    catch(PartInitException e)
    {
      String message = "Cannot open Launch configuration file : " + launchConfigurationFile;
      Activator.logError(message, e);
      Shell shell = HandlerUtil.getActiveShell(event);
      MessageDialog.openError(shell, "Error", message);
    }
  }
}
