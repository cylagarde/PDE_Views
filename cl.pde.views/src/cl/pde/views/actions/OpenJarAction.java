package cl.pde.views.actions;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import cl.pde.Images;
import cl.pde.PDEViewActivator;
import cl.pde.views.TreeObject;
import cl.pde.views.Util;

/**
 * The class <b>OpenJarAction</b> allows to open jar.<br>
 */
public class OpenJarAction extends AbstractTreeViewerAction
{
  private File jarFile;

  /**
   * Constructor
   * @param treeViewer
   */
  public OpenJarAction(AbstractTreeViewer treeViewer)
  {
    super(treeViewer);
    setText("Open jar");
    setToolTipText(getText());
    setImageDescriptor(PDEViewActivator.getImageDescriptor(Images.JAR_ICON));
  }

  @Override
  public boolean isEnabled()
  {
    ISelection selection = treeViewer.getSelection();
    Object[] items = ((IStructuredSelection) selection).toArray();
    if (items == null || items.length != 1)
      return false;

    Object item = items[0];
    if (item instanceof TreeObject)
    {
      TreeObject treeObject = (TreeObject) item;
      if (treeObject.data == null)
        return false;

      String location = Util.getLocation(treeObject.data);
      if (location == null)
        return false;

      jarFile = new File(location);
      if (!jarFile.isFile())
        return false;

      return true;
    }
    return false;
  }

  @Override
  public void run()
  {
    try
    {
      Desktop.getDesktop().open(jarFile);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }
}
