package cl.pde.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.IModel;

import cl.pde.PDEViewActivator;

/**
 * The class <b>TreeParent</b> allows to.<br>
 */
public class TreeParent extends TreeObject
{
  private List<TreeObject> children;
  public Runnable loadChildRunnable = null;
  boolean loaded = false;
  boolean sorted = false;

  public TreeParent(String name, Object data)
  {
    super(name, data);
  }

  public TreeParent(String name)
  {
    this(name, null);
  }

  public synchronized void addChild(TreeObject child)
  {
    if (children == null)
      children = new ArrayList<>(2);
    children.add(child);
    child.setParent(this);
    sorted = false;
  }

  public synchronized void removeChild(TreeObject child)
  {
    if (children != null)
      children.remove(child);
    child.setParent(null);
    sorted = false;
  }

  public synchronized TreeObject[] getChildren()
  {
    loadChildren();
    if (children == null)
      return new TreeObject[0];
    return children.toArray(new TreeObject[children.size()]);
  }

  public synchronized boolean hasChildren()
  {
    loadChildren();
    return children == null? false : !children.isEmpty();
  }

  synchronized void loadChildren()
  {
    // System.out.println("++loadChildren " + this + " loadChildRunnable " + loadChildRunnable + " loaded " + loaded);
    if (loadChildRunnable != null && !loaded)
    {
      loadChildRunnable.run();
      loaded = true;
      sorted = false;
    }
  }

  @Override
  synchronized void reset()
  {
    super.reset();
    if (loadChildRunnable != null && loaded)
    {
      if (children != null)
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
    if (children != null)
      Collections.sort(children, TREEOBJECT_COMPARATOR);
    sorted = true;
  }
}
