package cl.pde.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

import cl.pde.Activator;
import cl.pde.Images;
import cl.pde.views.feature.FeatureView;

/**
 * The class <b>GetAllFeaturesAction</b> allows to.<br>
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
    setToolTipText("Get all features");
    setImageDescriptor(Activator.getImageDescriptor(Images.GET_ALL_FEATURES));
  }

  @Override
  public void run()
  {
    IFeatureModel[] allFeatureModels = PDECore.getDefault().getFeatureModelManager().getModels();

    TreeViewer featureViewer = featureView.getFeatureViewer();
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
