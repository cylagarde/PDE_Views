package cl.pde.views.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.pde.internal.core.feature.FeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureImport;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;

import cl.pde.Activator;
import cl.pde.Images;
import cl.pde.views.Constants;
import cl.pde.views.TreeObject;
import cl.pde.views.TreeParent;
import cl.pde.views.UseCacheTreeContentProvider;
import cl.pde.views.Util;

/**
 * The class <b>FeatureViewContentProvider</b> allows to.<br>
 */
public class FeatureViewContentProvider extends UseCacheTreeContentProvider
{
  @Override
  public Object[] getElements(Object parent)
  {
    if (parent instanceof IFeature)
    {
      IFeature feature = (IFeature) parent;
      parent = Collections.singletonList(feature);
    }
    if (parent instanceof Collection)
    {
      Collection<?> collection = (Collection<?>) parent;
      return collection.stream()
          .filter(IFeature.class::isInstance)
          .map(IFeature.class::cast)
          .sorted(Util.PDE_LABEL_COMPARATOR)
          .map(FeatureViewContentProvider::getTreeParent)
          .toArray();
    }

    return getChildren(parent);
  }

  /**
   * @param feature
   */
  public static TreeParent getTreeParent(IFeature feature)
  {
    TreeParent featureTreeParent = new TreeParent(null, feature);
    featureTreeParent.foreground = Constants.FEATURE_FOREGROUND;
    featureTreeParent.image = Activator.getImage(Images.FEATURE);

    featureTreeParent.loadChildRunnable = () -> {
      List<TreeParent> elements = getElementsFromFeature(feature);
      elements.forEach(featureTreeParent::addChild);
    };
    return featureTreeParent;
  }

  /**
   * @param feature
   */
  public static List<TreeParent> getElementsFromFeature(IFeature feature)
  {
    List<TreeParent> elements = new ArrayList<>();

    // included plugins
    loadIncludedPlugins(feature, elements);

    // included features
    loadIncludedFeatures(feature, elements);

    // required features/plugins
    loadRequiredPlugins(feature, elements);

    return elements;
  }

  /**
   * Load Included Plugins
   * @param feature
   * @param elements
   */
  private static void loadIncludedPlugins(IFeature feature, List<TreeParent> elements)
  {
    IFeaturePlugin[] includedPlugins = feature.getPlugins();
    if (includedPlugins != null && includedPlugins.length != 0)
    {
      // sort
      Arrays.sort(includedPlugins, Util.PDE_LABEL_COMPARATOR);

      //
      TreeParent includedPluginsTreeParent = new TreeParent(PDEUIMessages.FeatureEditor_ReferencePage_title);
      includedPluginsTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGINS_FRAGMENTS);
      elements.add(includedPluginsTreeParent);

      for(IFeaturePlugin featurePlugin : includedPlugins)
      {
        TreeObject childTreeObject = new TreeObject(null, featurePlugin);
        childTreeObject.foreground = Constants.PLUGIN_FOREGROUND;
        includedPluginsTreeParent.addChild(childTreeObject);
      }
    }
  }

  /**
   * Load Included Features
   * @param feature
   * @param elements
   */
  private static void loadIncludedFeatures(IFeature feature, List<TreeParent> elements)
  {
    IFeatureChild[] includedFeatures = feature.getIncludedFeatures();
    if (includedFeatures != null && includedFeatures.length != 0)
    {
      // sort
      Arrays.sort(includedFeatures, Util.PDE_LABEL_COMPARATOR);

      //
      TreeParent includedFeaturesTreeParent = new TreeParent(PDEUIMessages.FeatureEditor_IncludesPage_title);
      includedFeaturesTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_FEATURE_OBJ);
      elements.add(includedFeaturesTreeParent);

      for(IFeatureChild includedFeature : includedFeatures)
      {
        TreeParent featureChildTreeParent = new TreeParent(null, includedFeature);
        featureChildTreeParent.foreground = Constants.FEATURE_FOREGROUND;
        includedFeaturesTreeParent.addChild(featureChildTreeParent);

        //
        featureChildTreeParent.loadChildRunnable = () -> {
          FeatureChild featureChildImpl = (FeatureChild) includedFeature;
          IFeature referencedFeature = featureChildImpl.getReferencedFeature();
          if (referencedFeature != null)
          {
            List<TreeParent> featureElements = getElementsFromFeature(referencedFeature);
            featureElements.forEach(featureChildTreeParent::addChild);
          }
        };
      }
    }
  }

  /**
   * Load Required Plugins
   * @param feature
   * @param elements
   */
  private static void loadRequiredPlugins(IFeature feature, List<TreeParent> elements)
  {
    IFeatureImport[] featureImports = feature.getImports();
    if (featureImports != null && featureImports.length != 0)
    {
      // sort
      Arrays.sort(featureImports, Util.PDE_LABEL_COMPARATOR);

      //
      TreeParent requiredFeaturesTreeParent = new TreeParent(PDEUIMessages.FeatureEditor_DependenciesPage_title);
      requiredFeaturesTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_REQ_PLUGINS_OBJ);
      elements.add(requiredFeaturesTreeParent);

      for(IFeatureImport featureImport : featureImports)
      {
        if (featureImport.getType() == IFeatureImport.FEATURE)
        {
          TreeParent featureChildTreeParent = new TreeParent(null, featureImport);
          featureChildTreeParent.foreground = Constants.FEATURE_FOREGROUND;
          requiredFeaturesTreeParent.addChild(featureChildTreeParent);

          //
          if (featureImport.getFeature() != null)
          {
            featureChildTreeParent.loadChildRunnable = () -> {
              List<TreeParent> featureElements = getElementsFromFeature(featureImport.getFeature());
              featureElements.forEach(featureChildTreeParent::addChild);
            };
          }
        }
        else if (featureImport.getType() == IFeatureImport.PLUGIN)
        {
          TreeObject childTreeObject = new TreeObject(null, featureImport);
          childTreeObject.foreground = Constants.PLUGIN_FOREGROUND;
          requiredFeaturesTreeParent.addChild(childTreeObject);
        }
      }
    }
  }

}
