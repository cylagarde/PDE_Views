package cl.pde.views.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;

import cl.pde.views.Constants;
import cl.pde.views.TreeParent;
import cl.pde.views.UseCacheTreeContentProvider;
import cl.pde.views.Util;

/**
 * The class <b>FeatureViewContentProvider</b> allows to.<br>
 */
public class FeatureViewContentProvider extends UseCacheTreeContentProvider
{
  @Override
  public Object[] getElements(Object inputElement)
  {
    if (inputElement instanceof IFeatureModel)
    {
      IFeatureModel featureModel = (IFeatureModel) inputElement;
      inputElement = Collections.singletonList(featureModel);
    }
    if (inputElement instanceof Object[])
    {
      Object[] array = (Object[]) inputElement;
      inputElement = Arrays.asList(array);
    }

    if (inputElement instanceof Collection)
    {
      Collection<?> collection = (Collection<?>) inputElement;
      Map<Boolean, List<IFeatureModel>> map = collection.stream()
          .filter(IFeatureModel.class::isInstance)
          .map(IFeatureModel.class::cast)
          //          .peek(feature -> System.out.println(feature+" "+feature.getModel().getClass()))
          .sorted(Util.PDE_LABEL_COMPARATOR)
          .collect(Collectors.partitioningBy(featureModel -> featureModel instanceof WorkspaceFeatureModel));

      List<TreeParent> treeParentList = new ArrayList<>();

      List<IFeatureModel> workspaceFeatureList = map.get(Boolean.TRUE);
      if (!workspaceFeatureList.isEmpty())
      {
        TreeParent workspaceFeatureTreeParent = new TreeParent(Constants.WORKSPACE_FEATURE);
        workspaceFeatureTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);
        treeParentList.add(workspaceFeatureTreeParent);

        workspaceFeatureTreeParent.loadChildRunnable = () -> {
          workspaceFeatureList.stream().map(Util::getTreeParent).forEach(workspaceFeatureTreeParent::addChild);
        };
      }

      List<IFeatureModel> externalFeatureList = map.get(Boolean.FALSE);
      if (!externalFeatureList.isEmpty())
      {
        TreeParent externalFeatureTreeParent = new TreeParent(Constants.TARGET_FEATURE);
        externalFeatureTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_SITE_OBJ);
        treeParentList.add(externalFeatureTreeParent);

        externalFeatureTreeParent.loadChildRunnable = () -> {
          externalFeatureList.stream().map(Util::getTreeParent).forEach(externalFeatureTreeParent::addChild);
        };
      }

      return treeParentList.toArray();
    }

    return getChildren(inputElement);
  }

}
