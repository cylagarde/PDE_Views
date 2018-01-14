package cl.pde.views.product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.eclipse.pde.internal.ui.PDELabelProvider;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;

import cl.pde.Activator;
import cl.pde.Images;
import cl.pde.views.AbstractTreeObjectContentProvider;
import cl.pde.views.Constants;
import cl.pde.views.TreeObject;
import cl.pde.views.TreeParent;
import cl.pde.views.Util;

/**
 * The class <b>ProductViewContentProvider</b> allows to.<br>
 */
public class ProductViewContentProvider extends AbstractTreeObjectContentProvider
{
  @Override
  public Object[] getElements(Object parent)
  {
    if (parent instanceof IProductModel)
    {
      IProductModel productModel = (IProductModel) parent;

      TreeParent productTreeParent = new TreeParent(null, productModel);
      productTreeParent.foreground = Constants.PRODUCT_FOREGROUND;
      IResource underlyingResource = productModel.getUnderlyingResource();
      productTreeParent.name = underlyingResource.getName();
      IProduct product = productModel.getProduct();
      if (!VersionUtil.isEmptyVersion(product.getVersion()))
        productTreeParent.name += ' ' + PDELabelProvider.formatVersion(product.getVersion());
      productTreeParent.image = Activator.getImage(Images.PRODUCT);

      productTreeParent.loadChildRunnable = () -> {
        List<TreeParent> elements = getElementsFromProduct(product);
        elements.forEach(productTreeParent::addChild);
      };

      return new Object[]{productTreeParent};
    }

    return getChildren(parent);
  }

  /**
   * @param product
   */
  private static List<TreeParent> getElementsFromProduct(IProduct product)
  {
    List<TreeParent> elements = new ArrayList<>();

    if (product.useFeatures())
      loadFeatures(product, elements);
    else
      loadPlugins(product, elements);

    return elements;
  }

  /**
   * Load IProductFeatures
   * @param productFeatures
   * @param elements
   */
  private static void loadFeatures(IProduct product, List<TreeParent> elements)
  {
    //
    TreeParent featuresTreeParent = new TreeParent("Features");
    featuresTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_FEATURE_MF_OBJ);
    elements.add(featuresTreeParent);

    featuresTreeParent.loadChildRunnable = () -> {
      IProductFeature[] productFeatures = product.getFeatures();

      // sort
      Arrays.sort(productFeatures, Util.PDE_LABEL_COMPARATOR);

      for(IProductFeature productFeature : productFeatures)
      {
        TreeParent featureTreeParent = new TreeParent(null, productFeature);
        featureTreeParent.foreground = Constants.FEATURE_FOREGROUND;
        featuresTreeParent.addChild(featureTreeParent);

        featureTreeParent.loadChildRunnable = () -> {
          FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
          IFeatureModel featureModel = manager.findFeatureModel(productFeature.getId(), productFeature.getVersion());
          if (featureModel != null)
          {
            IFeature feature = featureModel.getFeature();
            if (feature != null)
            {
              List<TreeParent> childElements = Util.getElementsFromFeature(feature);
              childElements.forEach(featureTreeParent::addChild);
            }
          }
        };
      }
    };
  }

  /**
   * Load IProductPlugins
   * @param productPlugins
   * @param elements
   */
  private static void loadPlugins(IProduct product, List<TreeParent> elements)
  {
    //
    TreeParent pluginsTreeParent = new TreeParent("Plugins");
    pluginsTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGIN_OBJ);
    elements.add(pluginsTreeParent);

    pluginsTreeParent.loadChildRunnable = () -> {
      IProductPlugin[] productPlugins = product.getPlugins();

      // sort
      Arrays.sort(productPlugins, Util.PDE_LABEL_COMPARATOR);

      for(IProductPlugin productPlugin : productPlugins)
      {
        TreeObject productPluginTreeObject = new TreeObject(null, productPlugin);
        productPluginTreeObject.foreground = Constants.PLUGIN_FOREGROUND;
        pluginsTreeParent.addChild(productPluginTreeObject);
      }
    };
  }
}
