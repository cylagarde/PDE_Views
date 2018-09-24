package cl.pde.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The class <b>AbstractTreeObjectContentProvider</b> allows to.<br>
 */
public abstract class AbstractTreeObjectContentProvider implements ITreeContentProvider
{
  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
  }

  @Override
  public Object getParent(Object child)
  {
    if (child instanceof TreeObject)
      return ((TreeObject) child).getParent();
    return null;
  }

  @Override
  public Object[] getChildren(Object parent)
  {
    if (parent instanceof TreeParent)
      return ((TreeParent) parent).getChildren();
    return new Object[0];
  }

  @Override
  public boolean hasChildren(Object parent)
  {
    if (parent instanceof TreeParent)
      return ((TreeParent) parent).hasChildren();
    return false;
  }

  /*
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose()
  {
  }
}
