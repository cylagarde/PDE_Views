package cl.pde.views.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import cl.pde.views.Constants;
import cl.pde.views.TreeObject;

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
    setText("Copy id/version to clipboard");
    setToolTipText("Copy id/version to clipboard");
    setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
  }

  @Override
  public boolean isEnabled() {
    ISelection selection = treeViewer.getSelection();
    Object[] items = ((IStructuredSelection) selection).toArray();
    if (items == null || items.length == 0)
      return false;

    for(Object item : items)
    {
      if (item instanceof TreeObject)
      {
        TreeObject treeObject = (TreeObject) item;
        if (treeObject.data == null)
          continue;

        return true;
      }
    }
    return false;
  }

  @Override
  public void run()
  {
    ISelection selection = treeViewer.getSelection();
    Object[] items = ((IStructuredSelection) selection).toArray();

    StringBuilder buffer = new StringBuilder(4096);
    StringBuilder rtfBuffer = new StringBuilder(4096);

    //
    rtfBuffer.append("{\\rtf1");

    rtfBuffer.append("{\\colortbl;");
    // black
    rtfBuffer.append("\\red0\\green0\\blue0;");
    // COUNTER_STYLER
    rtfBuffer.append("\\red0\\green127\\blue174;");

    // colors
    Map<Color, String> colorMap = new HashMap<>();
    Stream.of(Constants.PDE_COLORS).filter(Objects::nonNull).distinct().forEach(color -> {
      //
      colorMap.put(color, "\\cf" + (colorMap.size() + 3));

      rtfBuffer.append("\\red");
      rtfBuffer.append(color.getRed());
      rtfBuffer.append("\\green");
      rtfBuffer.append(color.getGreen());
      rtfBuffer.append("\\blue");
      rtfBuffer.append(color.getBlue());
      rtfBuffer.append(";");
    });
    rtfBuffer.append("}");

    //
    for(Object item : items)
    {
      TreeObject treeObject = (TreeObject) item;
      if (treeObject.data == null)
        continue;

      String name = treeObject.name;
      if (name == null)
        name = PDEPlugin.getDefault().getLabelProvider().getText(treeObject.data);

      buffer.append(name).append('\n');

      //
      if (treeObject.foreground != null)
        rtfBuffer.append(colorMap.get(treeObject.foreground));
      else
        rtfBuffer.append("\\cf1"); //black

      // version
      int lastIndex = name.lastIndexOf(')');
      int beginIndex = name.lastIndexOf('(', lastIndex);
      if (lastIndex >= 0 && beginIndex >= 0)
      {
        rtfBuffer.append(name.substring(0, beginIndex));

        // COUNTER_STYLER
        rtfBuffer.append("\\cf2");
        rtfBuffer.append(name.substring(beginIndex));
      }
      else
        rtfBuffer.append(name);

      rtfBuffer.append("\\line");
    }
    rtfBuffer.append("}");

    //
    Clipboard clipboard = new Clipboard(treeViewer.getControl().getDisplay());
    TextTransfer textTransfer = TextTransfer.getInstance();
    RTFTransfer rtfTransfer = RTFTransfer.getInstance();
    clipboard.setContents(new Object[]{buffer.toString(), rtfBuffer.toString()}, new Transfer[]{textTransfer, rtfTransfer});
    clipboard.dispose();
  }
}
