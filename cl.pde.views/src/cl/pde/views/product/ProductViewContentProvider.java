package cl.pde.views.product;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.pde.internal.core.iproduct.IProductModel;

import cl.pde.views.AbstractTreeObjectContentProvider;
import cl.pde.views.TreeObject;
import cl.pde.views.TreeParent;
import cl.pde.views.Util;

/**
 * The class <b>ProductViewContentProvider</b> allows to.<br>
 */
public class ProductViewContentProvider extends AbstractTreeObjectContentProvider
{
  @Override
  public Object[] getElements(Object inputElement)
  {
    if (inputElement instanceof IProductModel)
    {
      IProductModel productModel = (IProductModel) inputElement;
      inputElement = Collections.singletonList(productModel);
    }
    if (inputElement instanceof Object[])
    {
      Object[] array = (Object[]) inputElement;
      inputElement = Arrays.asList(array);
    }

    if (inputElement instanceof Collection)
    {
      Collection<?> collection = (Collection<?>) inputElement;

      List<TreeParent> treeParentList = collection.stream()
          .filter(IProductModel.class::isInstance)
          .map(IProductModel.class::cast)
          .map(Util::getProductModelTreeParent)
          .sorted(TreeObject.TREEOBJECT_COMPARATOR)
          .collect(Collectors.toList());

      return treeParentList.toArray();
    }

    return getChildren(inputElement);
  }
}
