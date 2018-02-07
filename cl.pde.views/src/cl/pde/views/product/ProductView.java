package cl.pde.views.product;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IContentProvider;

import cl.pde.PDEViewActivator;
import cl.pde.views.AbstractPDEView;
import cl.pde.views.Constants;
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

  @Override
  protected String getHelpContextId()
  {
    return PDEViewActivator.PLUGIN_ID + ".productView";
  }

  @Override
  protected String[] getCheckboxLabels()
  {
    String[] checkboxLabels = {Constants.INCLUDED_PLUGINS_NODE, Constants.INCLUDED_FEATURES_NODE, Constants.REQUIRED_PLUGINS_NODE};
    return checkboxLabels;
  }

  @Override
  protected Action createAllItemsAction()
  {
    return new GetAllProductsAction(this);
  }

  @Override
  protected IContentProvider getViewContentProvider()
  {
    return new ProductViewContentProvider();
  }
}
