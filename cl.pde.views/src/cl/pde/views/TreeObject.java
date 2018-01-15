package cl.pde.views;

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
    String txt = name != null? name : PDEPlugin.getDefault().getLabelProvider().getText(data);
    return getClass().getSimpleName() + "[" + txt + "]";
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
    if (data == null)
      return false;
    if (otherTreeObject.data == null)
      return false;

    if (!data.equals(otherTreeObject.data))
    {
      //
      if (data.getClass() != otherTreeObject.data.getClass())
        return false;

      // temporaire
      Object key1 = PDEPlugin.getDefault().getLabelProvider().getText(data);
      Object key2 = PDEPlugin.getDefault().getLabelProvider().getText(otherTreeObject.data);

      return Objects.equals(key1, key2);
    }
    return true;
  }

}
