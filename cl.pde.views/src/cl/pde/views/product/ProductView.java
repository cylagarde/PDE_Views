package cl.pde.views.product;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;

import cl.pde.PDEViewActivator;
import cl.pde.views.AbstractCheckboxFilteredTree;
import cl.pde.views.AbstractPDEView;
import cl.pde.views.Constants;
import cl.pde.views.NotTreeParentPatternFilter;
import cl.pde.views.actions.GetAllProductsAction;

/**
 * The class <b>ProductView</b> allows to.<br>
 */
public class ProductView extends AbstractPDEView
{
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "cl.pde.productView";

  private FilteredTree productFilteredTree;

  /**
   * The constructor.
   */
  public ProductView()
  {
  }

  /**
   * This is a callback that will allow us
   * to create the viewer and initialize it.
   */
  @Override
  public void createPartControl(Composite parent)
  {
    NotTreeParentPatternFilter filter = new NotTreeParentPatternFilter();
    String[] checkboxLabels = {Constants.INCLUDED_PLUGINS_NODE, Constants.INCLUDED_FEATURES_NODE, Constants.REQUIRED_PLUGINS_NODE};
    productFilteredTree = new AbstractCheckboxFilteredTree(parent, filter)
    {
      @Override
      protected String[] getCheckboxLabels()
      {
        return checkboxLabels;
      }
    };
    getTreeViewer().setContentProvider(new ProductViewContentProvider());

    // Create the help context id for the viewer's control
    PlatformUI.getWorkbench().getHelpSystem().setHelp(getTreeViewer().getControl(), PDEViewActivator.PLUGIN_ID + ".productView");

    super.createPartControl(parent);
  }

  /*
   * @see cl.pde.views.AbstractPDEView#getTreeViewer()
   */
  @Override
  public TreeViewer getTreeViewer()
  {
    return productFilteredTree.getViewer();
  }

  /*
   * @see cl.pde.views.AbstractPDEView#createAllItemsAction()
   */
  @Override
  protected Action createAllItemsAction()
  {
    return new GetAllProductsAction(this);
  }
}
