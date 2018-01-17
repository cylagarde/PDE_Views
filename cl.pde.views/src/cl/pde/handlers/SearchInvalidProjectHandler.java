package cl.pde.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.undo.CreateProjectOperation;

import cl.pde.Activator;
import cl.pde.Images;
import cl.pde.dialog.SearchElementTreeSelectionDialog;

/**
 * The class <b>SearchInvalidProjectHandler</b> allows to.<br>
 */
public class SearchInvalidProjectHandler extends AbstractHandler
{
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    Shell shell = HandlerUtil.getActiveShell(event);

    InvalidProjectLabelProvider labelProvider = new InvalidProjectLabelProvider();
    Comparator<Object> invalidProjectComparator = Comparator.comparing(labelProvider::getText, String.CASE_INSENSITIVE_ORDER);

    //
    Set<Object> invalidProjectSet = new TreeSet<>(invalidProjectComparator);
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    Set<Object> alreadyTreatedCacheSet = new HashSet<>();

    //
    Predicate<IResource> filePredicate = resource -> {
      if (!alreadyTreatedCacheSet.add(resource.getLocation()))
        return false;

      if (resource instanceof IFile && resource.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME))
      {
        IFile file = (IFile) resource;
        //        System.out.println(resource);

        IContainer folder = file.getParent();
        try
        {
          IPath projectDescriptionPath = folder.getLocation().append(IProjectDescription.DESCRIPTION_FILE_NAME);
          IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().loadProjectDescription(projectDescriptionPath);
          projectDescription.setLocation(folder.getLocation());

          String projectName = projectDescription.getName();
          IProject project = root.getProject(projectName);
          if (!project.exists())
            invalidProjectSet.add(projectDescription);
        }
        catch(CoreException e)
        {
          Activator.logError("Error loading project description "+file, e);
        }
      }

      return true;
    };

    // search
    MultiStatus errorStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, "Some errors were found when processing", null);
    IProject[] projects = root.getProjects();
    for(IProject workspaceProject : projects)
    {
      if (!workspaceProject.isOpen())
      {
        invalidProjectSet.add(workspaceProject);
        alreadyTreatedCacheSet.add(workspaceProject.getLocation());
        continue;
      }

      try
      {
        processContainer(workspaceProject, filePredicate);
      }
      catch(CoreException e)
      {
        errorStatus.add(e.getStatus());
      }
    }

    if (errorStatus.getChildren().length != 0)
      MessageDialog.openError(shell, "Error", "Some errors were found when processing: " + errorStatus.getMessage());

    //
    if (!invalidProjectSet.isEmpty())
    {
      //      invalidProjectSet.forEach(o -> Activator.logInfo(labelProvider.getText(o)));

//      ListSelectionDialog listSelectionDialog = new ListSelectionDialog(shell, invalidProjectSet, ArrayContentProvider.getInstance(), labelProvider, "Select the projects to be opened/imported:");
//      listSelectionDialog.open();

      InvalidProjectTreeContentProvider invalidProjectTreeContentProvider = new InvalidProjectTreeContentProvider();
      PatternFilter patternFilter = new PatternFilter();
      patternFilter.setIncludeLeadingWildcard(true);
      SearchElementTreeSelectionDialog searchElementTreeSelectionDialog = new SearchElementTreeSelectionDialog(shell, labelProvider, invalidProjectTreeContentProvider, patternFilter);
      searchElementTreeSelectionDialog.setInput(invalidProjectSet);
      searchElementTreeSelectionDialog.setMessage("Select the projects to be opened/imported:");
      searchElementTreeSelectionDialog.setTitle("Open Project");
      if (searchElementTreeSelectionDialog.open() == IDialogConstants.OK_ID)
      {
        Object[] result = searchElementTreeSelectionDialog.getResult();

        IWorkspaceRunnable openProjectRunnable = createRunnable(result);
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
        }
        catch(Exception e)
        {
          MessageDialog.openError(shell, "Error", "Cannot open projects " + e);
        }
      }
    }

    return null;
  }

  /**
   *
   * @param invalidProjects
   */
  private static IWorkspaceRunnable createRunnable(final Object[] invalidProjects)
  {
    return monitor -> {
      monitor.beginTask("", invalidProjects.length); //$NON-NLS-1$
      MultiStatus errorStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, "Cannot open projects", null);
      for(int i = 0; i < invalidProjects.length; i++)
      {
        if (invalidProjects[i] instanceof IProject)
        {
          IProject project = (IProject) invalidProjects[i];
          try
          {
            project.open(SubMonitor.convert(monitor, 1));
          }
          catch(CoreException e)
          {
            errorStatus.add(e.getStatus());
          }
        }
        else if (invalidProjects[i] instanceof IProjectDescription)
        {
          IProjectDescription projectDescription = (IProjectDescription) invalidProjects[i];
          CreateProjectOperation operation = new CreateProjectOperation(projectDescription, projectDescription.getName());
          try
          {
            IStatus status = OperationHistoryFactory.getOperationHistory().execute(operation, SubMonitor.convert(monitor, 1), null);
            if (!status.isOK())
              errorStatus.add(status);
          }
          catch(ExecutionException e)
          {
          }
        }
      }
      monitor.done();
      if (errorStatus.getChildren().length != 0)
        throw new CoreException(errorStatus);
    };
  }

  /**
   * Process container
   * @param container
   * @param fileConsumer
   * @throws CoreException
   */
  public static void processContainer(IContainer container, Predicate<IResource> filePredicate) throws CoreException
  {
    if (filePredicate.test(container))
    {
      IResource[] members = container.members();
      for(IResource member : members)
      {
        if (member instanceof IContainer)
          processContainer((IContainer) member, filePredicate);
        else if (member instanceof IFile)
        {
          if (!filePredicate.test(member))
            break;
        }
      }
    }
  }

  /**
   */
  static class InvalidProjectLabelProvider extends LabelProvider
  {
    public String getId(Object element)
    {
      if (element instanceof IProject)
      {
        IProject project = (IProject) element;
        return project.getName();
      }
      else if (element instanceof IProjectDescription)
      {
        IProjectDescription projectDescription = (IProjectDescription) element;
        return projectDescription.getName();
      }
      return null;
    }

    @Override
    public String getText(Object element)
    {
      String id = getId(element);
      if (id != null)
        return id;
      return super.getText(element);
    }

    @Override
    public Image getImage(Object element)
    {
      if (element instanceof IProject)
      {
        return Activator.getImage(Images.INVALID_PROJECT);
      }
      else if (element instanceof IProjectDescription)
      {
        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
      }
      return super.getImage(element);
    }
  }

  /**
   * The class <b>InvalidProjectTreeContentProvider</b> allows to.<br>
   */
  static class InvalidProjectTreeContentProvider implements ITreeContentProvider
  {
    @Override
    public void dispose()
    {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    }

    @Override
    public Object[] getElements(Object inputElement)
    {
      if (inputElement instanceof Collection)
        return ((Collection) inputElement).toArray();
      return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement)
    {
      return new Object[0];
    }

    @Override
    public Object getParent(Object element)
    {
      return null;
    }

    @Override
    public boolean hasChildren(Object element)
    {
      return false;
    }
  }
}
