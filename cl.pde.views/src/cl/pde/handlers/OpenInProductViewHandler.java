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

/**
 * The class <b>OpenInProductViewHandler</b> allows to.<br>
 */
public class OpenInProductViewHandler extends AbstractHandler
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
          if (file.getName().endsWith(".product"))
            openInProductView(event, file);
        }
      }
    }

    return null;
  }

  /**
   * @param event
   * @param productFile
   */
  private void openInProductView(ExecutionEvent event, IFile productFile)
  {
    IWorkbenchPage workbenchPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
    try
    {
      workbenchPage.showView(Activator.PRODUCT_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
    }
    catch(PartInitException e)
    {
      String message = "Cannot open product file : " + productFile;
      Activator.logError(message, e);
      Shell shell = HandlerUtil.getActiveShell(event);
      MessageDialog.openError(shell, "Error", message);
    }
  }
}
