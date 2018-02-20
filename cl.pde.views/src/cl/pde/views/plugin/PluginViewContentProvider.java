package cl.pde.views.plugin;

import cl.pde.views.AbstractTreeObjectContentProvider;

/**
 * The class <b>ProductViewContentProvider</b> allows to.<br>
 */
public class PluginViewContentProvider extends AbstractTreeObjectContentProvider
{
  @Override
  public Object[] getElements(Object inputElement)
  {
    if (inputElement instanceof Object[])
    {
      Object[] array = (Object[]) inputElement;
      return array;
    }

    return getChildren(inputElement);
  }
}
