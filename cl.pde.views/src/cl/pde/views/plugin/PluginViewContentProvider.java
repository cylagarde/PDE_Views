package cl.pde.views.plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.pde.core.plugin.IPluginModelBase;

import cl.pde.views.AbstractTreeObjectContentProvider;
import cl.pde.views.TreeObject;
import cl.pde.views.TreeParent;
import cl.pde.views.Util;

/**
 * The class <b>ProductViewContentProvider</b> allows to.<br>
 */
public class PluginViewContentProvider extends AbstractTreeObjectContentProvider
{
  @Override
  public Object[] getElements(Object inputElement)
  {
    if (inputElement instanceof IPluginModelBase)
    {
      IPluginModelBase plugin = (IPluginModelBase) inputElement;
      inputElement = Collections.singletonList(plugin);
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
          .filter(IPluginModelBase.class::isInstance)
          .map(IPluginModelBase.class::cast)
          .map(Util::getTreeParent)
          .sorted(TreeObject.TREEOBJECT_COMPARATOR)
          .collect(Collectors.toList());

      return treeParentList.toArray();
    }

    return getChildren(inputElement);
  }
}
