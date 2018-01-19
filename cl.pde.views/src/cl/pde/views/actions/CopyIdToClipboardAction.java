package cl.pde.views.actions;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import cl.pde.views.TreeObject;
import cl.pde.views.Util;

/**
 * The class <b>CopyIdToClipboardAction</b> allows to.<br>
 */
public class CopyIdToClipboardAction extends AbstractTreeViewerAction
{
  /**
   * Constructor
   * @param treeViewer
   */
  public CopyIdToClipboardAction(AbstractTreeViewer treeViewer)
  {
    super(treeViewer);
    setText("Copy id to clipboard");
    setToolTipText("Copy id to clipboard");
    setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
  }

  @Override
  public boolean isEnabled() {
    ISelection selection = treeViewer.getSelection();
    Object obj = ((IStructuredSelection) selection).getFirstElement();
    if (obj instanceof TreeObject)
    {
      TreeObject treeObject = (TreeObject) obj;
      if (treeObject.data == null)
        return false;
      String id = Util.getId(treeObject.data);
      if (id == null)
        return false;
      return true;
    }

    return false;
  }

  @Override
  public void run()
  {
    ISelection selection = treeViewer.getSelection();
    Object obj = ((IStructuredSelection) selection).getFirstElement();
    if (obj instanceof TreeObject)
    {
      TreeObject treeObject = (TreeObject) obj;
      if (treeObject.data == null)
        return;
      String id = Util.getId(treeObject.data);
      if (id == null)
        return;

      Clipboard clipboard = new Clipboard(treeViewer.getControl().getDisplay());
      TextTransfer textTransfer = TextTransfer.getInstance();
      clipboard.setContents(new String[]{id}, new Transfer[]{textTransfer});
      clipboard.dispose();
    }
  }
}
