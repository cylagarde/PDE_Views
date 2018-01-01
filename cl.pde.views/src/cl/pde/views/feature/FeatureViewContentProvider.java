package cl.pde.views.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
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

/**
 * The class <b>FeatureViewContentProvider</b> allows to.<br>
 */
public class FeatureViewContentProvider implements ITreeContentProvider
{
  @Override
  public Object[] getElements(Object parent)
  {
    if (parent instanceof IFeature)
    {
      IFeature feature = (IFeature) parent;

      TreeParent featureTreeParent = new TreeParent(null, feature);
      featureTreeParent.image = Activator.getImage(Images.FEATURE);

      featureTreeParent.loadChildRunnable = () -> {
        List<TreeParent> elements = getElementsFromFeature(feature);
        elements.forEach(featureTreeParent::addChild);
      };

      return new Object[]{featureTreeParent};
    }

    return getChildren(parent);
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
      Comparator<Object> PDE_COMPARATOR = Comparator.comparing(PDEPlugin.getDefault().getLabelProvider()::getText);
      Arrays.sort(includedPlugins, PDE_COMPARATOR);

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
      Comparator<Object> PDE_COMPARATOR = Comparator.comparing(PDEPlugin.getDefault().getLabelProvider()::getText);
      Arrays.sort(includedFeatures, PDE_COMPARATOR);

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
            for(TreeParent featureElement : featureElements)
              featureChildTreeParent.addChild(featureElement);
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
      Comparator<Object> PDE_COMPARATOR = Comparator.comparing(PDEPlugin.getDefault().getLabelProvider()::getText);
      Arrays.sort(featureImports, PDE_COMPARATOR);

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
          featureChildTreeParent.loadChildRunnable = () -> {
            if (featureImport.getFeature() != null)
            {
              List<TreeParent> featureElements = getElementsFromFeature(featureImport.getFeature());
              for(TreeParent featureElement : featureElements)
                featureChildTreeParent.addChild(featureElement);
            }
          };
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

  @Override
  public Object getParent(Object child)
  {
    if (child instanceof TreeObject)
      return ((TreeObject) child).getParent();
    return null;
  }

  @Override
  public Object[] getChildren(Object parent)
  {
    if (parent instanceof TreeParent)
      return ((TreeParent) parent).getChildren();
    return new Object[0];
  }

  @Override
  public boolean hasChildren(Object parent)
  {
    if (parent instanceof TreeParent)
      return ((TreeParent) parent).hasChildren();
    return false;
  }
}
