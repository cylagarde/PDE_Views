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
}
