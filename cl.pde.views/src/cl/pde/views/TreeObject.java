package cl.pde.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * The class <b>TreeObject</b> allows to.<br>
 */
public class TreeObject implements IAdaptable
{
  public final Object data;
  public String name;
  public Image image;
  public Color foreground;
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

  public void setParent(TreeParent parent)
  {
    this.parent = parent;
  }

  public TreeParent getParent()
  {
    return parent;
  }

  @Override
  public String toString()
  {
    return name != null? name : String.valueOf(data);
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
    result = prime * result + ((name == null)? 0 : name.hashCode());
    result = prime * result + ((data == null)? 0 : data.hashCode());
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
    TreeObject other = (TreeObject) obj;
    if (name == null)
    {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    if (data == null)
    {
      if (other.data != null)
        return false;
    }
    else if (!data.equals(other.data))
      return false;
    return true;
  }

}
