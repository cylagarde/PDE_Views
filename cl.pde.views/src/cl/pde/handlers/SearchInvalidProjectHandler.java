package cl.pde.handlers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The class <b>SearchInvalidProjectHandler</b> allows to.<br>
 */
public class SearchInvalidProjectHandler extends AbstractHandler
{
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    Set<IProject> invalidProjectSet = new HashSet<>();

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject[] projects = root.getProjects();
    for(IProject workspaceProject : projects)
    {
      if (!workspaceProject.isOpen())
      {
        invalidProjectSet.add(workspaceProject);
        continue;
      }

      Predicate<IResource> filePredicate = resource -> {
        if (resource instanceof IFile && resource.getName().equals(".project"))
        {
          IFile file = (IFile) resource;
//          System.out.println(resource);

          try (InputStream inputStream = file.getContents(true))
          {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
            .map(String::trim)
            .filter(line -> line.startsWith("<name>"))
            .filter(line -> line.endsWith("</name>"))
            .findFirst()
            .map(line -> line.substring("<name>".length()))
            .map(line -> line.substring(0, line.length()-"</name>".length()))
            .map(name -> root.getProject(name))
            .filter(project -> !project.exists())
            .ifPresent(invalidProjectSet::add);
            ;
          }
          catch(Exception e)
          {

          }
        }

        return true;
      };
      try
      {
        processContainer(workspaceProject, filePredicate);
      }
      catch(CoreException e)
      {
        e.printStackTrace();
      }
    }

    if (!invalidProjectSet.isEmpty())
    {
      Shell shell = HandlerUtil.getActiveShell(event);
      ListSelectionDialog dlg = new ListSelectionDialog(shell, invalidProjectSet, ArrayContentProvider.getInstance(), new WorkbenchLabelProvider(), "Select the projects to open/import");
      dlg.setTitle("Project not open or not imported");
      if (dlg.open() == IDialogConstants.OK_ID)
      {
        Object[] result = dlg.getResult();
        IProject project = (IProject) result[0];
        try
        {
          NullProgressMonitor monitor = new NullProgressMonitor();
          if (!project.exists())
            project.create(monitor);
          project.open(monitor);
        }
        catch(CoreException e)
        {
          e.printStackTrace();
          MessageDialog.openError(shell, "Error", "Cannot open project "+project);
        }
      }
    }


    return null;
  }

  /**
  *
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
          filePredicate.test(member);
      }
    }
  }
}
