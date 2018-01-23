package cl.pde.dialog;

import java.util.function.Function;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * The class <b>CheckedFilteredTreeSelectionDialog</b> allows to.<br>
 */
public class CheckedFilteredTreeSelectionDialog extends CheckedTreeSelectionDialog
{
  PatternFilter patternFilter;

  /**
   * Constructor
   * @param shell
   * @param labelProvider
   * @param invalidProjectTreeContentProvider
   * @param patternFilter
   */
  public CheckedFilteredTreeSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider, PatternFilter patternFilter)
  {
    super(parent, labelProvider, contentProvider);
    this.patternFilter = patternFilter;
  }

  /**
   * Constructor
   * @param parent
   * @param labelProvider
   * @param contentProvider
   * @param style
   * @param patternFilter
   */
  public CheckedFilteredTreeSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider, int style, PatternFilter patternFilter)
  {
    super(parent, labelProvider, contentProvider, style);
    this.patternFilter = patternFilter;
  }

  @Override
  protected CheckboxTreeViewer createTreeViewer(Composite parent)
  {
    Function<Composite, CheckboxTreeViewer> f = composite -> super.createTreeViewer(composite);

    int style = SWT.BORDER | SWT.MULTI;
    FilteredTree filteredTree = new FilteredTree(parent, style, patternFilter, true)
    {
      @Override
      protected TreeViewer doCreateTreeViewer(Composite parent, int style)
      {
        return f.apply(parent);
      }
    };
    filteredTree.setQuickSelectionMode(true);

    // https://www.eclipsezone.com/eclipse/forums/t72055.html
    //    final String platform = SWT.getPlatform();
    //    if ("win32".equals(platform)) //$NON-NLS-1$
    //    {
    //      Tree tree = ((CheckboxTreeViewer) filteredTree.getViewer()).getTree();
    //      final int oldValue = OS.GetWindowLong(tree.handle, OS.GWL_STYLE);
    //      final int newValue = oldValue ^ (OS.TVS_HASLINES | OS.TVS_HASBUTTONS);
    //      OS.SetWindowLong(tree.handle, OS.GWL_STYLE, newValue);
    //    }

    return (CheckboxTreeViewer) filteredTree.getViewer();
  }

  @Override
  protected void updateButtonsEnableState(IStatus status)
  {
    if (status.isOK() && getTreeViewer().getCheckedElements().length == 0)
      status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, null, null);
    super.updateButtonsEnableState(status);
  }

  @Override
  protected void computeResult()
  {
    setSelectionResult(getTreeViewer().getCheckedElements());
  }

  @Override
  public CheckboxTreeViewer getTreeViewer()
  {
    return super.getTreeViewer();
  }
}
