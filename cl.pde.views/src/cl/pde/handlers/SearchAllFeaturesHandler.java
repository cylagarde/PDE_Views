package cl.pde.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import cl.pde.Activator;
import cl.pde.views.Util;
import cl.pde.views.search_pde.SearchPDEView;

/**
 * The class <b>SearchAllFeaturesViewHandler</b> allows to.<br>
 */
public class SearchAllFeaturesHandler extends AbstractHandler
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
        IProject project = null;
        if (element instanceof IProject)
        {
          project = (IProject) element;
        }
        else if (element instanceof IAdaptable)
        {
          project = ((IAdaptable) element).getAdapter(IProject.class);
        }
        if (project != null)
        {
          String pluginId = Util.getPluginId(project);
          openInSearchPDEView(event, pluginId);
        }
      }
    }

    return null;
  }

  /**
   * @param event
   * @param productFile
   */
  private void openInSearchPDEView(ExecutionEvent event, String pluginId)
  {
    IWorkbenchPage workbenchPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
    try
    {
      workbenchPage.showView(SearchPDEView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
    }
    catch(PartInitException e)
    {
      String message = "Cannot open pluginId : " + pluginId;
      Activator.logError(message, e);
      Shell shell = HandlerUtil.getActiveShell(event);
      MessageDialog.openError(shell, "Error", message);
    }
  }
}
