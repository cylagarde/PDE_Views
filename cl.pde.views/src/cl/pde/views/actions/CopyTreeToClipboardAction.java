package cl.pde.views.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import cl.pde.Images;
import cl.pde.views.Constants;
import cl.pde.views.RTFBasic;
import cl.pde.views.TreeObject;
import cl.pde.views.Util;

/**
 * The class <b>CopyTreeToClipboardAction</b> allows to copy tree in plain text and RTF format to clipboard.<br>
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
    setToolTipText(getText());
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

    StringBuilder buffer = new StringBuilder(4096);
    RGB black = new RGB(0, 0, 0);
    List<RGB> list = new ArrayList<>();
    list.add(black);
    if (Constants.VERSION_FOREGROUND != null)
      list.add(Constants.VERSION_FOREGROUND.getRGB());
    Stream<RGB> colorStream = Stream.concat(list.stream().filter(Objects::nonNull), Stream.of(Constants.PDE_COLORS).filter(Objects::nonNull).map(Color::getRGB));
    RTFBasic rtfBasic = new RTFBasic(colorStream);

    //
    Map<Integer, String> indentMap = new HashMap<>();
    for(TreeItem parentTreeItem : tree.getSelection())
    {
      TreeObject parentTreeObject = (TreeObject) parentTreeItem.getData();

      BiPredicate<Integer, Object> predicate = (depth, o) -> {
        //        if (depth == 10)
        //          return false;

        TreeObject treeObject = (TreeObject) o;
        int level = treeObject.getLevel(parentTreeObject);
        String indent = indentMap.computeIfAbsent(level, n -> String.join("", Collections.nCopies(n, "    ")));

        String name = treeObject.getLabelText();
        buffer.append(indent).append(name).append('\n');

        //
        rtfBasic.append(indent);

        // bold
        if (treeObject.name != null)
          rtfBasic.useBold(true);

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

        // unbold
        if (treeObject.name != null)
          rtfBasic.useBold(false);

        rtfBasic.appendln();

        return true;
      };

      Util.traverseElement((ITreeContentProvider) treeViewer.getContentProvider(), parentTreeObject, 0, predicate, null);
    }

    //
    Clipboard clipboard = new Clipboard(treeViewer.getControl().getDisplay());
    TextTransfer textTransfer = TextTransfer.getInstance();
    RTFTransfer rtfTransfer = RTFTransfer.getInstance();
    clipboard.setContents(new Object[]{buffer.toString(), rtfBasic.toString()}, new Transfer[]{textTransfer, rtfTransfer});
    clipboard.dispose();
  }
}
