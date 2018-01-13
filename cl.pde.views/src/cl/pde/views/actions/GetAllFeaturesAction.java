package cl.pde.views.actions;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

import cl.pde.Activator;
import cl.pde.Images;

/**
 * The class <b>GetAllFeaturesAction</b> allows to.<br>
 */
public class GetAllFeaturesAction extends AbstractTreeViewerAction
{
  /**
   * Constructor
   * @param featureTreeViewer
   */
  public GetAllFeaturesAction(AbstractTreeViewer featureTreeViewer)
  {
    super(featureTreeViewer);
    setText("Get all features");
    setToolTipText("Get all features");
    setImageDescriptor(Activator.getImageDescriptor(Images.GET_ALL_FEATURES));
  }

  @Override
  public void run()
  {
    IFeatureModel[] allFeatureModels = PDECore.getDefault().getFeatureModelManager().getModels();
    treeViewer.setInput(allFeatureModels);
  }
}
