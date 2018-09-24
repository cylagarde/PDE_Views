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
import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import cl.pde.PDEViewActivator;
import cl.pde.views.Constants;
import cl.pde.views.feature.FeatureView;

/**
 * The class <b>OpenInFeatureViewHandler</b> allows to.<br>
 */
public class OpenInFeatureViewHandler extends AbstractHandler implements Constants
{
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    ISelection selection = HandlerUtil.getCurrentSelection(event);
    if (selection instanceof IStructuredSelection)
    {
      Collection<WorkspaceFeatureModel> workspaceFeatureModels = new ArrayList<>();

      for(Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();)
      {
        Object element = it.next();
        if (element instanceof IFile)
        {
          IFile file = (IFile) element;
          if (!"feature.xml".equals(file.getName()))
            continue;

          try
          {
            String contentId = Optional.ofNullable(file.getContentDescription()).map(IContentDescription::getContentType).map(IContentType::getId).orElse("");
            if (!FEATURE_CONTENT_TYPE.equals(contentId))
              continue;
          }
          catch(CoreException e)
          {
            PDEViewActivator.logError("Cannot getContentType " + file, e);
            continue;
          }

          // load feature
          WorkspaceFeatureModel workspaceFeatureModel = new WorkspaceFeatureModel(file);
          workspaceFeatureModel.load();

          // check if feature loaded
          if (!workspaceFeatureModel.isLoaded())
          {
            PDEViewActivator.logError("Cannot load feature " + file);
            continue;
          }

          workspaceFeatureModels.add(workspaceFeatureModel);
        }
      }

      if (!workspaceFeatureModels.isEmpty())
        openInFeatureView(event, workspaceFeatureModels);
    }

    return null;
  }

  /**
   * Open in FeatureView
   * @param event
   * @param workspaceFeatureModels
   */
  private void openInFeatureView(ExecutionEvent event, Collection<WorkspaceFeatureModel> workspaceFeatureModels)
  {
    IWorkbenchPage workbenchPage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

    try
    {
      // get FeatureView
      IViewPart showView = workbenchPage.showView(FeatureView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
      FeatureView featureView = (FeatureView) showView;
      TreeViewer featureViewer = featureView.getTreeViewer();

      featureViewer.getControl().setRedraw(false);
      try
      {
        featureView.setInput(workspaceFeatureModels);

        //
        if (workspaceFeatureModels.size() == 1)
          featureViewer.expandToLevel(4);
      }
      finally
      {
        featureViewer.getControl().setRedraw(true);
      }
    }
    catch(PartInitException e)
    {
      String message = "Cannot open feature view : " + e;
      PDEViewActivator.logError(message, e);
      Shell shell = HandlerUtil.getActiveShell(event);
      MessageDialog.openError(shell, "Error", message);
    }
  }
}
