package cl.pde.views;

import java.util.Comparator;
import java.util.Objects;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import cl.pde.Images;
import cl.pde.PDEViewActivator;

/**
 * The class <b>TreeObject</b> allows to.<br>
 */
public class TreeObject implements IAdaptable
{
  public static final Comparator<TreeObject> TREEOBJECT_COMPARATOR = Comparator.comparing(TreeObject::getLabelText, String.CASE_INSENSITIVE_ORDER);

  public final Object data;
  public String name;
  public Image image;
  public Color foreground;
  private String labelText = null;
  private Image labelImage = null;
  //  Map<Object, Object> map;

  private TreeParent parent;

  public TreeObject(String name, Object data)
  {
    this.data = data;
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  void setParent(TreeParent parent)
  {
    this.parent = parent;
  }

  public final TreeParent getParent()
  {
    return parent;
  }

  synchronized void reset()
  {
    labelText = null;
    labelImage = null;
  }

  /**
   * Return the label text
   */
  public String getLabelText()
  {
    if (labelText == null)
    {
      if (name != null)
        labelText = name;
      else
        labelText = PDEPlugin.getDefault().getLabelProvider().getText(data);
    }
    return labelText;
  }

  /**
   * Return the label image
   */
  public Image getLabelImage()
  {
    if (labelImage == null)
    {
      if (image != null)
        labelImage = image;
      else
      {
        Image img = PDEPlugin.getDefault().getLabelProvider().getImage(data);
        Boolean singletonState = Util.getSingletonState(data);
        if (singletonState != null && singletonState)
        {
          //
          ImageDescriptor singletonImageDescriptor = PDEViewActivator.getImageDescriptor(Images.SINGLETON);
          String key = String.valueOf(img) + " " + String.valueOf(singletonImageDescriptor);
          Image overlayImage = PDEViewActivator.getDefault().getImageRegistry().get(key);
          if (overlayImage == null)
          {
            DecorationOverlayIcon overlayIcon = new DecorationOverlayIcon(img, singletonImageDescriptor, IDecoration.TOP_RIGHT);
            overlayImage = overlayIcon.createImage();
            PDEViewActivator.getDefault().getImageRegistry().put(key, overlayImage);
          }
          img = overlayImage;
        }
        labelImage = img;
      }
    }
    return labelImage;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + getLabelText() + "]";
  }

  @Override
  public <T> T getAdapter(Class<T> key)
  {
    return null;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (name == null? 0 : name.hashCode());
    result = prime * result + (data == null? 0 : data.hashCode());
    return result;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TreeObject otherTreeObject = (TreeObject) obj;
    if (name == null)
    {
      if (otherTreeObject.name != null)
        return false;
    }
    else if (!name.equals(otherTreeObject.name))
      return false;

    //
    if (data == null && otherTreeObject.data == null)
      return Objects.equals(parent, otherTreeObject.parent);
    if (data == null || otherTreeObject.data == null)
      return false;

    if (!data.equals(otherTreeObject.data))
      return Objects.equals(getLabelText(), otherTreeObject.getLabelText());
    return true;
  }

  /**
   * Return level from root
   */
  public int getLevel(TreeObject parent)
  {
    if (this == parent)
      return 0;
    if (this.parent == null)
      return 0;
    if (this.parent == parent)
      return 1;
    return 1 + this.parent.getLevel(parent);
  }
}
