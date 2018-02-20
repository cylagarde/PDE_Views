package cl.pde.views;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.IModel;

import cl.pde.PDEViewActivator;

/**
 * The class <b>TreeParent</b> allows to.<br>
 */
public class TreeParent extends TreeObject
{
  private ArrayList<TreeObject> children;
  public Runnable loadChildRunnable = null;
  boolean loaded = false;
  boolean sorted = false;

  public TreeParent(String name, Object data)
  {
    super(name, data);
    children = new ArrayList<>();
  }

  public TreeParent(String name)
  {
    this(name, null);
  }

  public synchronized void addChild(TreeObject child)
  {
    children.add(child);
    child.setParent(this);
    sorted = false;
  }

  public synchronized void removeChild(TreeObject child)
  {
    children.remove(child);
    child.setParent(null);
    sorted = false;
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

  synchronized void loadChildren()
  {
    //    System.out.println("++loadChildren " + this + "  loadChildRunnable " + loadChildRunnable + "  loaded " + loaded);
    if (loadChildRunnable != null && !loaded)
    {
      loadChildRunnable.run();
      loaded = true;
      sorted = false;
    }
  }

  synchronized void reset()
  {
    if (loadChildRunnable != null && loaded)
    {
      children.clear();
      loaded = false;
      sorted = false;

      if (data instanceof IModel)
      {
        try
        {
          ((IModel) data).load();
        }
        catch(CoreException e)
        {
          PDEViewActivator.logError("Cannot load model " + data, e);
        }
      }
    }
  }

  /**
   */
  public synchronized void sortChildren()
  {
    if (sorted)
      return;
    Collections.sort(children, TREEOBJECT_COMPARATOR);
    sorted = true;
  }
}
