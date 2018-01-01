package cl.pde.views;

import java.util.ArrayList;

/**
 * The class <b>TreeParent</b> allows to.<br>
 */
public class TreeParent extends TreeObject
{
  private ArrayList<TreeObject> children;
  public Runnable loadChildRunnable = null;
  boolean loaded = false;

  public TreeParent(String name, Object data)
  {
    super(name, data);
    children = new ArrayList<>();
  }

  public TreeParent(String name)
  {
    this(name, null);
  }

  public void addChild(TreeObject child)
  {
    children.add(child);
    child.setParent(this);
  }

  public void removeChild(TreeObject child)
  {
    children.remove(child);
    child.setParent(null);
  }

  public TreeObject[] getChildren()
  {
    loadChildren();
    return children.toArray(new TreeObject[children.size()]);
  }

  public boolean hasChildren()
  {
    loadChildren();
    return children.size() > 0;
  }

  void loadChildren()
  {
    if (loadChildRunnable != null && !loaded)
    {
      loadChildRunnable.run();
      loaded = true;
    }
  }

  void reset()
  {
    if (loadChildRunnable != null && loaded)
    {
      children.clear();
      loaded = false;
    }
  }
}
