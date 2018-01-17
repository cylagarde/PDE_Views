package cl.pde.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.internal.core.plugin.ImportObject;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.undo.CreateProjectOperation;

import cl.pde.Activator;
import cl.pde.handlers.SearchInvalidProjectHandler.InvalidProjectLabelProvider;

/**
 * The class <b>TryToOpenInvalidProjectHandler</b> allows to.<br>
 */
public class TryToOpenInvalidProjectHandler extends AbstractHandler
{
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    Shell shell = HandlerUtil.getActiveShell(event);
    InvalidProjectLabelProvider labelProvider = new InvalidProjectLabelProvider();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    Object[] invalidProject = new Object[1];
    Object[] validProject = new Object[1];

    String activePartId = HandlerUtil.getActivePartId(event);
    if (IPDEUIConstants.MANIFEST_EDITOR_ID.equals(activePartId))
    {
      IStructuredSelection currentSelection = HandlerUtil.getCurrentStructuredSelection(event);
      if (currentSelection.isEmpty())
        return null;
      Object firstElement = currentSelection.getFirstElement();
      if (! (firstElement instanceof ImportObject))
        return null;

      ImportObject importObject = (ImportObject) firstElement;
      String pluginId = importObject.getId();

      IPluginImport iimport = importObject.getImport();
      if (importObject.isResolved() || iimport.isOptional())
        return null;

      Predicate<IResource> filePredicate = resource -> {
        if (invalidProject[0] != null)
          return false;

        if (resource instanceof IFile && resource.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME))
        {
          IFile file = (IFile) resource;
          //        System.out.println(resource);

          IContainer folder = file.getParent();
          try
          {
            IProjectDescription desc = ResourcesPlugin.getWorkspace().loadProjectDescription(folder.getLocation().append(IProjectDescription.DESCRIPTION_FILE_NAME));
            desc.setLocation(folder.getLocation());

            if (pluginId.equals(labelProvider.getId(desc)))
            {
              String projectName = desc.getName();
              IProject project = root.getProject(projectName);
              if (! project.exists())
              {
                invalidProject[0] = desc;
                return false;
              }
              else
                validProject[0] = project;
            }
          }
          catch(Exception e)
          {
            Activator.logError("Error loading project description "+file, e);
          }
        }

        return true;
      };

      // search
      MultiStatus errorStatus = null;
      IProject[] projects = root.getProjects();
      for(IProject workspaceProject : projects)
      {
        if (invalidProject[0] != null)
          break;
        if (!workspaceProject.isOpen())
        {
          if (pluginId.equals(labelProvider.getId(workspaceProject)))
          {
            invalidProject[0] = workspaceProject;
            break;
          }
          continue;
        }

        try
        {
          SearchInvalidProjectHandler.processContainer(workspaceProject, filePredicate);
        }
        catch(CoreException e)
        {
          if (errorStatus == null)
            errorStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, "Cannot process project " + workspaceProject, e);
          errorStatus.add(e.getStatus());
        }
      }

      if (errorStatus != null)
        MessageDialog.openError(shell, "Error", "Some errors were found when processing: "+errorStatus.getMessage());
    }

    if (invalidProject[0] != null)
    {
      boolean result = MessageDialog.openConfirm(shell, "Confirm", "Project found.\nOpen it?");
      //      invalidProjectSet.forEach(o -> Activator.logInfo(labelProvider.getText(o)));
      if (result)
      {
        IWorkspaceRunnable openProjectRunnable= createRunnable(invalidProject[0]);
        try
        {
          PlatformUI.getWorkbench().getProgressService().run(true, true, monitor -> {
            try
            {
              openProjectRunnable.run(monitor);
            }
            catch(CoreException ce)
            {
              throw new InvocationTargetException(ce);
            }
          });
        } catch (Exception e) {
          MessageDialog.openError(shell, "Error", "Cannot open projects "+e);
        }
      }
    }
    else if (validProject[0] == null)
      MessageDialog.openWarning(shell, "Warning", "Project not found");

    return null;
  }

  /**
   *
   * @param invalidProjects
   */
  private IWorkspaceRunnable createRunnable(final Object invalidProject)
  {
    return monitor -> {
      monitor.beginTask("", 1); //$NON-NLS-1$
      MultiStatus errorStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, "Cannot open projects", null);
      if (invalidProject instanceof IProject)
      {
        IProject project = (IProject) invalidProject;
        try
        {
          project.open(SubMonitor.convert(monitor, 1));
        }
        catch(CoreException e)
        {
          errorStatus.add(e.getStatus());
        }
      }
      else if (invalidProject instanceof IProjectDescription)
      {
        IProjectDescription projectDescription = (IProjectDescription) invalidProject;
        CreateProjectOperation operation = new CreateProjectOperation(projectDescription, projectDescription.getName());
        try
        {
          IStatus status = OperationHistoryFactory.getOperationHistory().execute(operation, SubMonitor.convert(monitor, 1), null);
          if (! status.isOK())
            errorStatus.add(status);
        }
        catch(ExecutionException e)
        {
        }
      }
      monitor.done();
      if (errorStatus.getChildren().length != 0)
        throw new CoreException(errorStatus);
    };
  }
}
