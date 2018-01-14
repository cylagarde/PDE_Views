package cl.pde.views.search_pde;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.ui.PlatformUI;

import cl.pde.Activator;
import cl.pde.views.Constants;
import cl.pde.views.TreeObject;
import cl.pde.views.TreeParent;
import cl.pde.views.AbstractTreeObjectContentProvider;
import cl.pde.views.Util;

/**
 * The class <b>SearchPDEViewContentProvider</b> allows to.<br>
 */
public class SearchPDEViewContentProvider extends AbstractTreeObjectContentProvider
{
  @Override
  public Object[] getElements(Object parent)
  {
    if (parent instanceof IProject)
    {
      IProject project = (IProject) parent;

      //
      TreeParent projectTreeParent = new TreeParent(null, project);
      projectTreeParent.name = project.getName();
      projectTreeParent.image = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT);
      projectTreeParent.foreground = Constants.PROJECT_FOREGROUND;

      projectTreeParent.loadChildRunnable = () -> {
        String pluginId = Util.getPluginId(project);
        List<TreeObject> elements = getElementsForPluginId(pluginId);
        elements.forEach(projectTreeParent::addChild);
      };

      return new Object[]{projectTreeParent};
    }

    return getChildren(parent);
  }

  /**
   * @param pluginId
   * @param featureFile
   */
  private IFeature searchPluginIdInFeature(String pluginId, IFile featureFile)
  {
    try
    {
      if (!"org.eclipse.pde.featureManifest".equals(featureFile.getContentDescription().getContentType().getId()))
        return null;
    }
    catch(CoreException e)
    {
      Activator.logInfo("Cannot get content description for featureFile " + featureFile);
      return null;
    }

    WorkspaceFeatureModel workspaceFeatureModel = new WorkspaceFeatureModel(featureFile);
    workspaceFeatureModel.load();

    IFeature feature = workspaceFeatureModel.getFeature();
    IFeaturePlugin[] includedPlugins = feature.getPlugins();
    if (includedPlugins != null)
    {
      for(IFeaturePlugin includedPlugin : includedPlugins)
      {
        if (pluginId.equals(includedPlugin.getId()))
          return feature;
      }
    }

    return null;
  }

  /**
   * @param product
   */
  private List<TreeObject> getElementsForPluginId(String pluginId)
  {
    List<TreeObject> elements = new ArrayList<>();

    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for(IProject workspaceProject : projects)
    {
      if (!workspaceProject.isOpen())
        continue;
      try
      {
        if (!workspaceProject.hasNature(PDE.FEATURE_NATURE))
          continue;
      }
      catch(CoreException e)
      {
        continue;
      }

      //
      IFile featureFile = workspaceProject.getFile("feature.xml");
      if (featureFile.exists())
      {
        IFeature feature = searchPluginIdInFeature(pluginId, featureFile);
        if (feature != null)
        {
          TreeParent featureTreeParent = Util.getTreeParent(feature);
          elements.add(featureTreeParent);
        }
      }
    }

    return elements;
  }
}
