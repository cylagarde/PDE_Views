package cl.pde.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import cl.pde.PDEViewActivator;
import cl.pde.views.Constants;
import cl.pde.views.product.ProductView;

/**
 * The class <b>OpenInProductViewHandler</b> allows to.<br>
 */
public class OpenInProductViewHandler extends AbstractHandler implements Constants
{
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    ISelection selection = HandlerUtil.getCurrentSelection(event);
    if (selection instanceof IStructuredSelection)
    {
      Collection<WorkspaceProductModel> workspaceProductModels = new ArrayList<>();

      for(Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();)
      {
        Object element = it.next();
        if (element instanceof IFile)
        {
          IFile file = (IFile) element;
          if (!file.getName().endsWith(".product"))
            continue;

          try
          {
            String contentId = Optional.ofNullable(file.getContentDescription()).map(IContentDescription::getContentType).map(IContentType::getId).orElse("");
            if (!PRODUCT_CONTENT_TYPE.equals(contentId))
              continue;

            // load product
            WorkspaceProductModel workspaceProductModel = new WorkspaceProductModel(file, false);
            workspaceProductModel.load();

            // check if product loaded
            if (!workspaceProductModel.isLoaded())
            {
              PDEViewActivator.logError("Cannot load product " + file);
              continue;
            }

            workspaceProductModels.add(workspaceProductModel);
          }
          catch(CoreException e)
          {
            PDEViewActivator.logError("Cannot getContentType " + file, e);
            continue;
          }
        }
      }

      if (!workspaceProductModels.isEmpty())
        openInProductView(event, workspaceProductModels);
    }

    return null;
  }

  /**
   * Open in ProductView
   * @param event
   * @param workspaceProductModels
   */
  private void openInProductView(ExecutionEvent event, Collection<WorkspaceProductModel> workspaceProductModels)
  {
    IWorkbenchPage workbenchPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

    try
    {
      // get ProductView
      IViewPart showView = workbenchPage.showView(ProductView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
      ProductView productView = (ProductView) showView;
      TreeViewer productViewer = productView.getTreeViewer();

      productViewer.getControl().setRedraw(false);
      try
      {
        productView.setInput(workspaceProductModels);

        //
        if (workspaceProductModels.size() == 1)
          productViewer.expandToLevel(5);
      }
      finally
      {
        productViewer.getControl().setRedraw(true);
      }
    }
    catch(PartInitException e)
    {
      String message = "Cannot open product view : " + e;
      PDEViewActivator.logError(message, e);
      Shell shell = HandlerUtil.getActiveShell(event);
      MessageDialog.openError(shell, "Error", message);
    }
  }
}
