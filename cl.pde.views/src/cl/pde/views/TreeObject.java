package cl.pde.views;

import java.util.Comparator;
import java.util.Objects;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * The class <b>TreeObject</b> allows to.<br>
 */
public class TreeObject implements IAdaptable
{
  public static final Comparator<TreeObject> TREEOBJECT_COMPARATOR = Comparator.comparing(TreeObject::getDisplayText, String.CASE_INSENSITIVE_ORDER);

  public final Object data;
  public String name;
  public Image image;
  public Color foreground;
  private String displayText = null;
  private Image displayImage = null;
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
    displayText = null;
    displayImage = null;
  }

  /**
   * Return the text to display
   */
  public String getDisplayText()
  {
    if (displayText == null)
    {
      if (name != null)
        displayText = name;
      else
        displayText = PDEPlugin.getDefault().getLabelProvider().getText(data);
    }
    return displayText;
  }

  /**
   * Return the image to display
   */
  public Image getDisplayImage()
  {
    if (displayImage == null)
      displayImage = Util.getImage(data, image);
    return displayImage;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + getDisplayText() + "]";
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
      return Objects.equals(getDisplayText(), otherTreeObject.getDisplayText());
    return true;
  }

  /**
   * Return level from root
   */
  public int getLevel()
  {
    return getLevel(null);
  }

  /**
   * Return level from parent
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
