package cl.pde.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

import cl.pde.Images;
import cl.pde.PDEViewActivator;
import cl.pde.views.feature.FeatureView;

/**
 * The class <b>GetAllFeaturesAction</b> allows to search and display all features from workspace and target platform.<br>
 */
public class GetAllFeaturesAction extends Action
{
  FeatureView featureView;

  /**
   * Constructor
   * @param featureView
   */
  public GetAllFeaturesAction(FeatureView featureView)
  {
    this.featureView = featureView;
    setText("Get all features");
    setToolTipText(getText());
    setImageDescriptor(PDEViewActivator.getImageDescriptor(Images.GET_ALL_FEATURES));
  }

  @Override
  public void run()
  {
    IFeatureModel[] allFeatureModels = PDECore.getDefault().getFeatureModelManager().getModels();

    TreeViewer featureViewer = featureView.getTreeViewer();
    featureViewer.getControl().setRedraw(false);
    try
    {
      featureView.setInput(allFeatureModels);
      featureViewer.expandToLevel(2);
    }
    finally
    {
      featureViewer.getControl().setRedraw(true);
    }
  }
}
