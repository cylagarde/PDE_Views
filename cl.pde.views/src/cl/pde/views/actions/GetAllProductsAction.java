package cl.pde.views.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;

import cl.pde.Activator;
import cl.pde.Images;
import cl.pde.views.Util;
import cl.pde.views.product.ProductView;

/**
 * The class <b>GetAllProductsAction</b> allows to.<br>
 */
public class GetAllProductsAction extends Action
{
  ProductView productView;

  /**
   * Constructor
   * @param productView
   */
  public GetAllProductsAction(ProductView productView)
  {
    this.productView = productView;
    setText("Get all products in workspace");
    setToolTipText("Get all products in workspace");
    setImageDescriptor(Activator.getImageDescriptor(Images.GET_ALL_PRODUCTS));
  }

  @Override
  public void run()
  {
    Set<IProductModel> productModelSet = new HashSet<>();
    Set<Object> alreadyTreatedCacheSet = new HashSet<>();

    //
    Predicate<IResource> filePredicate = resource -> {
      if (!alreadyTreatedCacheSet.add(resource.getLocation()))
        return false;

      if (resource instanceof IFile && resource.getName().endsWith(".product"))
      {
        IFile productFile = (IFile) resource;

        // load product
        WorkspaceProductModel workspaceProductModel = new WorkspaceProductModel(productFile, false);
        try
        {
          workspaceProductModel.load();

          // check if product loaded
          if (workspaceProductModel.isLoaded())
            productModelSet.add(workspaceProductModel);
        }
        catch(CoreException e)
        {
          Activator.logError("Cannot load product " + productFile, e);
        }
      }

      return true;
    };

    // search
    MultiStatus errorStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, "Some errors were found when processing", null);

    NullProgressMonitor nullMonitor = new NullProgressMonitor();

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject[] projects = root.getProjects();
    for(IProject workspaceProject : projects)
    {
      if (!workspaceProject.isOpen())
      {
        alreadyTreatedCacheSet.add(workspaceProject.getLocation());
        continue;
      }

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
      MessageDialog.openError(productView.getProductViewer().getTree().getShell(), "Error", "Some errors were found when processing: " + errorStatus.getMessage());

    //
    TreeViewer productViewer = productView.getProductViewer();
    productViewer.getControl().setRedraw(false);
    try
    {
      productView.setInput(productModelSet);
    }
    finally
    {
      productViewer.getControl().setRedraw(true);
    }
  }
}
