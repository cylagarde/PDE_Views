package cl.pde.views.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import cl.pde.views.Constants;
import cl.pde.views.RTFBasic;
import cl.pde.views.TreeObject;

/**
 * The class <b>CopyIdToClipboardAction</b> allows to copy id and version in plain text and RTF format to clipboard.<br>
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
    setToolTipText(getText());
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
    RGB black = new RGB(0, 0, 0);
    List<RGB> list = new ArrayList<>();
    list.add(black);
    if (Constants.VERSION_FOREGROUND != null)
      list.add(Constants.VERSION_FOREGROUND.getRGB());
    Stream<RGB> colorStream = Stream.concat(list.stream().filter(Objects::nonNull), Stream.of(Constants.PDE_COLORS).filter(Objects::nonNull).map(Color::getRGB));
    RTFBasic rtfBasic = new RTFBasic(colorStream);

    //
    for(Object item : items)
    {
      TreeObject treeObject = (TreeObject) item;
      if (treeObject.data == null)
        continue;

      String name = treeObject.getDisplayText();
      buffer.append(name).append('\n');

      rtfBasic.useColor(treeObject.foreground != null? treeObject.foreground.getRGB() : black);

      // find version
      int lastIndex = name.lastIndexOf(')');
      int beginIndex = name.lastIndexOf('(', lastIndex);
      if (lastIndex >= 0 && beginIndex >= 0)
      {
        // id
        rtfBasic.append(name.substring(0, beginIndex));

        // version
        rtfBasic.useColor(Constants.VERSION_FOREGROUND != null? Constants.VERSION_FOREGROUND.getRGB() : black);
        rtfBasic.append(name.substring(beginIndex));
      }
      else
        rtfBasic.append(name);

      rtfBasic.appendln();
    }

    //
    Clipboard clipboard = new Clipboard(treeViewer.getControl().getDisplay());
    TextTransfer textTransfer = TextTransfer.getInstance();
    RTFTransfer rtfTransfer = RTFTransfer.getInstance();
    clipboard.setContents(new Object[]{buffer.toString().trim(), rtfBasic.toString()}, new Transfer[]{textTransfer, rtfTransfer});
    clipboard.dispose();
  }
}
