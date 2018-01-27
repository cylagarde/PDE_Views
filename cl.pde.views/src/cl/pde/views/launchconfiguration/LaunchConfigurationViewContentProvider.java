package cl.pde.views.launchconfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.debug.core.ILaunchConfiguration;

import cl.pde.views.AbstractTreeObjectContentProvider;
import cl.pde.views.TreeObject;
import cl.pde.views.TreeParent;
import cl.pde.views.Util;

/**
 * The class <b>LaunchConfigurationViewContentProvider</b> allows to.<br>
 */
public class LaunchConfigurationViewContentProvider extends AbstractTreeObjectContentProvider
{
  @Override
  public Object[] getElements(Object inputElement)
  {
    if (inputElement instanceof ILaunchConfiguration)
    {
      ILaunchConfiguration launchConfiguration = (ILaunchConfiguration) inputElement;
      inputElement = Collections.singletonList(launchConfiguration);
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
          .filter(ILaunchConfiguration.class::isInstance)
          .map(ILaunchConfiguration.class::cast)
          .map(Util::getLaunchConfigurationTreeParent)
          .sorted(TreeObject.TREEOBJECT_COMPARATOR)
          .collect(Collectors.toList());

      return treeParentList.toArray();
    }

    return getChildren(inputElement);
  }

}
