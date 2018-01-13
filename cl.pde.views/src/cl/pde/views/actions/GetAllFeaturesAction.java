package cl.pde.views.actions;

import java.util.stream.Stream;

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

    Object[] features = Stream.of(allFeatureModels).map(IFeatureModel::getFeature).toArray();
    //    List<IFeature> featureList = new ArrayList<>();
    //    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    //    for(IProject workspaceProject : projects)
    //    {
    //      if (!workspaceProject.isOpen())
    //        continue;
    //      try
    //      {
    //        if (!workspaceProject.hasNature(PDE.FEATURE_NATURE))
    //          continue;
    //      }
    //      catch(CoreException e)
    //      {
    //        continue;
    //      }
    //
    //      //
    //      IFile featureFile = workspaceProject.getFile("feature.xml");
    //      if (!featureFile.exists())
    //        continue;
    //
    //      WorkspaceFeatureModel workspaceFeatureModel = new WorkspaceFeatureModel(featureFile);
    //      workspaceFeatureModel.load();
    //
    //      IFeature feature = workspaceFeatureModel.getFeature();
    //      featureList.add(feature);
    //    }

    treeViewer.setInput(features);
  }
}
