package cl.pde.views.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import cl.pde.Images;
import cl.pde.views.Constants;
import cl.pde.views.TreeObject;
import cl.pde.views.Util;

/**
 * The class <b>CopyTreeToClipboardAction</b> allows to.<br>
 */
public class CopyTreeToClipboardAction extends AbstractTreeViewerAction
{
  /**
   * Constructor
   * @param treeViewer
   */
  public CopyTreeToClipboardAction(AbstractTreeViewer treeViewer)
  {
    super(treeViewer);
    setText("Copy tree to clipboard");
    setToolTipText("Copy tree to clipboard");
    setImageDescriptor(Images.TREE.getImageDescriptor());
  }

  @Override
  public boolean isEnabled()
  {
    ISelection selection = treeViewer.getSelection();
    Object[] items = ((IStructuredSelection) selection).toArray();
    return items.length != 0;
  }

  @Override
  public void run()
  {
    Tree tree = (Tree) treeViewer.getControl();

    StringBuilder buffer = new StringBuilder(1024);
    StringBuilder rtfBuffer = new StringBuilder(1024);

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
    Map<Integer, String> indentMap = new HashMap<>();
    for(TreeItem parentTreeItem : tree.getSelection())
    {
      TreeObject parentTreeObject = (TreeObject) parentTreeItem.getData();

      BiPredicate<Integer, Object> predicate = (depth, o) -> {
        TreeObject treeObject = (TreeObject) o;
        int level = treeObject.getLevel(parentTreeObject);
        String indent = indentMap.computeIfAbsent(level, n -> String.join("", Collections.nCopies(n, "    ")));

        String name = treeObject.name;
        if (name == null)
          name = PDEPlugin.getDefault().getLabelProvider().getText(treeObject.data);
        //        System.out.println(indent + level + " " + name);
        buffer.append(indent).append(name).append('\n');

        //
        rtfBuffer.append(indent);

        // bold
        if (treeObject.name != null)
          rtfBuffer.append("\\b");

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

        // unbold
        if (treeObject.name != null)
          rtfBuffer.append("\\b0");

        rtfBuffer.append("\\line");

        return true;
      };

      Util.traverseElement((ITreeContentProvider) treeViewer.getContentProvider(), parentTreeObject, 0, predicate, null);
    }

    rtfBuffer.append("}");
    //    System.out.println(rtfBuffer);

    //    rtfBuffer.setLength(0);
    //    rtfBuffer.append("{\\rtf1\\b Hello World}");

    //
    Clipboard clipboard = new Clipboard(treeViewer.getControl().getDisplay());
    TextTransfer textTransfer = TextTransfer.getInstance();
    RTFTransfer rtfTransfer = RTFTransfer.getInstance();
    clipboard.setContents(new Object[]{buffer.toString(), rtfBuffer.toString()}, new Transfer[]{textTransfer, rtfTransfer});
    clipboard.dispose();
  }
}
