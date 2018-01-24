package cl.pde.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.undo.CreateProjectOperation;

import cl.pde.Images;
import cl.pde.PDEViewActivator;
import cl.pde.dialog.CheckedFilteredTreeSelectionDialog;
import cl.pde.views.Util;

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
    Set<InvalidProject> invalidProjectSet = new TreeSet<>(invalidProjectComparator);
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    Set<Object> alreadyTreatedCacheSet = new HashSet<>();

    IProject[] projects = root.getProjects();
    Set<IPath> locationSet = new HashSet<>();
    for(IProject workspaceProject : projects)
    {
      if (workspaceProject.isOpen())
        locationSet.add(workspaceProject.getLocation());
    }

    //
    Predicate<IResource> filePredicate = resource -> {
      if (!alreadyTreatedCacheSet.add(resource.getLocation()))
        return false;

      if (resource instanceof IProject)
      {
        IProject project = (IProject) resource;
        if (!project.isOpen())
        {
          InvalidProject invalidProject = createInvalidProject(project, locationSet);
          invalidProjectSet.add(invalidProject);

          return false;
        }
      }
      else if (resource instanceof IFile && resource.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME))
      {
        IFile file = (IFile) resource;
        IContainer folder = file.getParent();
        try
        {
          IPath projectDescriptionPath = folder.getLocation().append(IProjectDescription.DESCRIPTION_FILE_NAME);
          IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().loadProjectDescription(projectDescriptionPath);
          projectDescription.setLocation(folder.getLocation());

          String projectName = projectDescription.getName();
          IProject project = root.getProject(projectName);
          if (!project.exists())
          {
            InvalidProject invalidProject = new InvalidProject();
            invalidProject.projectInfo = projectDescription;
            invalidProject.name = folder.getName();
            invalidProject.image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

            if (!projectDescription.getName().equals(invalidProject.name))
              invalidProject.name += " (" + projectDescription.getName() + ")";

            String location = folder.toString().substring(2);
            if (!location.equals(folder.getName()))
              invalidProject.relative = location;

            invalidProject.location = folder.getLocation().toOSString();

            invalidProjectSet.add(invalidProject);
          }
          else if (!project.isOpen())
          {
            InvalidProject invalidProject = createInvalidProject(project, locationSet);
            invalidProjectSet.add(invalidProject);

            return false;
          }
        }
        catch(CoreException e)
        {
          PDEViewActivator.logError("Error loading project description " + file, e);
        }
      }

      return true;
    };

    NullProgressMonitor nullMonitor = new NullProgressMonitor();

    // search
    MultiStatus errorStatus = new MultiStatus(PDEViewActivator.PLUGIN_ID, IStatus.ERROR, "Some errors were found when processing", null);
    for(IProject workspaceProject : projects)
    {
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
      MessageDialog.openError(shell, "Error", "Some errors were found when processing: " + errorStatus.getMessage());

    //
    if (!invalidProjectSet.isEmpty())
    {
      InvalidProjectTreeContentProvider invalidProjectTreeContentProvider = new InvalidProjectTreeContentProvider();
      PatternFilter patternFilter = new PatternFilter();
      patternFilter.setIncludeLeadingWildcard(true);

      //
      CheckedFilteredTreeSelectionDialog searchElementTreeSelectionDialog = new CheckedFilteredTreeSelectionDialog(shell, labelProvider, invalidProjectTreeContentProvider, SWT.FULL_SELECTION, patternFilter);
      searchElementTreeSelectionDialog.setInput(invalidProjectSet);
      searchElementTreeSelectionDialog.setMessage("Select the projects to be opened/imported:");
      searchElementTreeSelectionDialog.setTitle("Open Project");
      searchElementTreeSelectionDialog.create();

      CheckboxTreeViewer treeViewer = searchElementTreeSelectionDialog.getTreeViewer();
      treeViewer.getTree().setHeaderVisible(true);
      treeViewer.getTree().setLinesVisible(true);

      //
      TreeViewerColumn nameViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
      nameViewerColumn.getColumn().setText("Name");
      nameViewerColumn.setLabelProvider(new ColumnLabelProvider()
      {
        @Override
        public String getText(Object element)
        {
          return labelProvider.getText(element);
        }

        @Override
        public Image getImage(Object element)
        {
          return labelProvider.getImage(element);
        }
      });

      //
      TreeViewerColumn relativePathViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
      relativePathViewerColumn.getColumn().setText("Path");
      relativePathViewerColumn.setLabelProvider(new ColumnLabelProvider()
      {
        @Override
        public String getText(Object element)
        {
          if (element instanceof InvalidProject)
          {
            InvalidProject invalidProject = (InvalidProject) element;
            return invalidProject.relative;
          }
          return null;
        }
      });

      //
      TreeViewerColumn locationViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
      locationViewerColumn.getColumn().setText("Location");
      locationViewerColumn.setLabelProvider(new ColumnLabelProvider()
      {
        @Override
        public String getText(Object element)
        {
          if (element instanceof InvalidProject)
          {
            InvalidProject invalidProject = (InvalidProject) element;
            return invalidProject.location;
          }
          return null;
        }
      });

      // pack columns
      treeViewer.refresh();
      for(TreeColumn treeColumn : treeViewer.getTree().getColumns())
        treeColumn.pack();

      searchElementTreeSelectionDialog.getShell().setSize(640, 480);

      // open
      if (searchElementTreeSelectionDialog.open() == IDialogConstants.OK_ID)
      {
        Object[] result = searchElementTreeSelectionDialog.getResult();
        InvalidProject[] invalidProjects = Stream.of(result).filter(InvalidProject.class::isInstance).map(InvalidProject.class::cast).toArray(InvalidProject[]::new);

        IWorkspaceRunnable openProjectRunnable = createRunnable(invalidProjects);
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
   * Create InvalidProject
   * @param locationSet
   * @param project
   */
  private InvalidProject createInvalidProject(IProject project, Set<IPath> locationSet)
  {
    InvalidProject invalidProject = new InvalidProject();
    invalidProject.projectInfo = project;
    invalidProject.name = project.getName();
    invalidProject.image = PDEViewActivator.getImage(Images.INVALID_PROJECT);

    String pluginId = Util.getPluginId(project);
    if (pluginId != null && !invalidProject.name.equals(pluginId))
      invalidProject.name += " (" + pluginId + ")";

    String location = project.getLocation().toOSString();
    locationSet.stream().filter(loc -> location.startsWith(loc.toOSString())).max(Comparator.comparing(IPath::toOSString, Comparator.comparing(String::length))).map(loc -> loc.lastSegment() + '/' + location.substring(loc.toOSString().length() + 1).replace('\\', '/')).ifPresent(relative -> invalidProject.relative = relative);

    invalidProject.location = project.getLocation().toOSString();
    return invalidProject;
  }

  /**
   *
   * @param invalidProjects
   */
  private static IWorkspaceRunnable createRunnable(final InvalidProject[] invalidProjects)
  {
    return monitor -> {
      monitor.beginTask("", invalidProjects.length); //$NON-NLS-1$
      MultiStatus errorStatus = new MultiStatus(PDEViewActivator.PLUGIN_ID, IStatus.ERROR, "Cannot open projects", null);
      for(int i = 0; i < invalidProjects.length; i++)
        invalidProjects[i].tryToOpen(monitor);
      monitor.done();
      if (errorStatus.getChildren().length != 0)
        throw new CoreException(errorStatus);
    };
  }

  /**
   */
  static class InvalidProjectLabelProvider extends LabelProvider
  {
    @Override
    public String getText(Object element)
    {
      if (element instanceof InvalidProject)
      {
        InvalidProject invalidProject = (InvalidProject) element;
        return invalidProject.name;
      }
      return super.getText(element);
    }

    @Override
    public Image getImage(Object element)
    {
      if (element instanceof InvalidProject)
      {
        InvalidProject invalidProject = (InvalidProject) element;
        return invalidProject.image;
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
        return ((Collection<?>) inputElement).toArray();
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

  /**
   *
   */
  class InvalidProject
  {
    Object projectInfo;
    String name;
    Image image;
    String relative;
    String location;

    /**
     *
     * @param monitor
     */
    public IStatus tryToOpen(IProgressMonitor monitor)
    {
      if (projectInfo instanceof IProject)
      {
        IProject project = (IProject) projectInfo;
        try
        {
          project.open(SubMonitor.convert(monitor, 1));
        }
        catch(CoreException e)
        {
          return e.getStatus();
        }
      }
      else if (projectInfo instanceof IProjectDescription)
      {
        IProjectDescription projectDescription = (IProjectDescription) projectInfo;
        CreateProjectOperation operation = new CreateProjectOperation(projectDescription, projectDescription.getName());
        try
        {
          return OperationHistoryFactory.getOperationHistory().execute(operation, SubMonitor.convert(monitor, 1), null);
        }
        catch(ExecutionException e)
        {
        }
      }

      return Status.OK_STATUS;
    }

    @Override
    public String toString()
    {
      return "InvalidProject[name=" + name + "  " + projectInfo + "]";
    }
  }
}
