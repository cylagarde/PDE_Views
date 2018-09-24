package cl.pde.views;

import java.util.stream.Stream;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;

/**
 * The class <b>ExpandTreeViewerListener</b> allows to expand at level+1.<br>
 */
public class ExpandTreeViewerListener implements ITreeViewerListener
{
  @Override
  public void treeExpanded(TreeExpansionEvent event)
  {
    TreeParent treeParent = (TreeParent) event.getElement();
    if (treeParent.data != null)
    {
      boolean allMatch = Stream.of(treeParent.getChildren()).allMatch(treeObject -> treeObject.data == null);
      if (allMatch)
      {
        AbstractTreeViewer treeViewer = event.getTreeViewer();
        treeViewer.getControl().getDisplay().asyncExec(() -> treeViewer.expandToLevel(treeParent, 2));
      }
    }
  }

  @Override
  public void treeCollapsed(TreeExpansionEvent event)
  {
  }
}
