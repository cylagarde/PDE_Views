package cl.pde.views.product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ITreeContentProvider;
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
import cl.pde.views.Constants;
import cl.pde.views.TreeObject;
import cl.pde.views.TreeParent;

/**
 * The class <b>ProductViewContentProvider</b> allows to.<br>
 */
public class ProductViewContentProvider implements ITreeContentProvider
{
  private final Comparator<Object> PDE_COMPARATOR = Comparator.comparing(PDEPlugin.getDefault().getLabelProvider()::getText);

  @Override
  public Object[] getElements(Object parent)
  {
    if (parent instanceof IProduct)
    {
      IProduct product = (IProduct) parent;
      IProductModel productModel = product.getModel();
      IResource underlyingResource = productModel.getUnderlyingResource();

      TreeParent productTreeParent = new TreeParent(null, product);
      productTreeParent.name = underlyingResource.getName();
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
  private List<TreeParent> getElementsFromProduct(IProduct product)
  {
    List<TreeParent> elements = new ArrayList<>();

    if (product.useFeatures())
    {
      // IProductFeatures
      loadFeatures(product.getFeatures(), elements);
    }
    else
    {
      // IProductPlugins
      loadPlugins(product.getPlugins(), elements);
    }

    return elements;
  }

  /**
   * Load IProductFeatures
   * @param productFeatures
   * @param elements
   */
  private void loadFeatures(IProductFeature[] productFeatures, List<TreeParent> elements)
  {
    if (productFeatures != null && productFeatures.length != 0)
    {
      // sort
      Arrays.sort(productFeatures, PDE_COMPARATOR);

      //
      TreeParent featuresTreeParent = new TreeParent("Features");
      featuresTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_FEATURE_MF_OBJ);
      elements.add(featuresTreeParent);

      for(IProductFeature productFeature : productFeatures)
      {
        TreeParent childTreeParent = new TreeParent(null, productFeature);
        childTreeParent.foreground = Constants.FEATURE_FOREGROUND;
        featuresTreeParent.addChild(childTreeParent);

        childTreeParent.loadChildRunnable = () -> {
          FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
          IFeatureModel featureModel = manager.findFeatureModel(productFeature.getId(), productFeature.getVersion());
          if (featureModel != null)
          {
            IFeature feature = featureModel.getFeature();
            if (feature != null)
            {
              List<TreeParent> childElements = cl.pde.views.feature.FeatureViewContentProvider.getElementsFromFeature(feature);
              childElements.forEach(childTreeParent::addChild);
            }
          }
        };
      }
    }
  }

  /**
   * Load IProductPlugins
   * @param productPlugins
   * @param elements
   */
  private void loadPlugins(IProductPlugin[] productPlugins, List<TreeParent> elements)
  {
    if (productPlugins != null && productPlugins.length != 0)
    {
      // sort
      Arrays.sort(productPlugins, PDE_COMPARATOR);

      //
      TreeParent pluginsTreeParent = new TreeParent("Plugins");
      pluginsTreeParent.image = PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PLUGIN_OBJ);
      elements.add(pluginsTreeParent);

      for(IProductPlugin productPlugin : productPlugins)
      {
        TreeObject productPluginTreeObject = new TreeObject(null, productPlugin);
        productPluginTreeObject.foreground = Constants.PLUGIN_FOREGROUND;
        pluginsTreeParent.addChild(productPluginTreeObject);
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
