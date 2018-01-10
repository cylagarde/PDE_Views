package cl.pde.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class SearchAllFeaturesViewHandler extends AbstractHandler
{
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    System.out.println("TODO " + event.getParameter("cl.pde.views.commandParameter.feature.file"));
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
          //          try
          //          {
          //            //            toggleNature(project);
          //          }
          //          catch(CoreException e)
          //          {
          //            // TODO log something
          //            throw new ExecutionException("Failed to toggle nature", e);
          //          }
        }
      }
    }

    return null;
  }
}
