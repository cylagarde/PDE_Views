package cl.pde.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * The class <b>SearchElementTreeSelectionDialog</b> allows to.<br>
 * PatternFilter filter = new PatternFilter();
 * filter.setIncludeLeadingWildcard(true);
 * ElementTreeSelectionDialog dialog = new SearchElementTreeSelectionDialog(shell, labelProvider, contentProvider, filter);
 */
public class SearchElementTreeSelectionDialog extends ElementTreeSelectionDialog
{
  static String SELECT_ALL_TITLE = WorkbenchMessages.SelectionDialog_selectLabel;
  static String DESELECT_ALL_TITLE = WorkbenchMessages.SelectionDialog_deselectLabel;

  private final PatternFilter filter;
  private FilteredTree filteredTree;

  /**
   * Constructor
   *
   * @param parent
   * @param labelProvider
   * @param contentProvider
   * @param filter
   */
  public SearchElementTreeSelectionDialog(Shell parent, IBaseLabelProvider labelProvider, ITreeContentProvider contentProvider, PatternFilter filter)
  {
    super(parent, labelProvider, contentProvider);
    this.filter = filter;
  }

  @Override
  protected Control createDialogArea(Composite parent)
  {
    Composite composite = (Composite) super.createDialogArea(parent);
    addSelectionButtons(composite);

    return composite;
  }

  @Override
  protected void updateButtonsEnableState(IStatus status) {
    if (status.isOK() && getCheckboxTreeViewer().getCheckedElements().length == 0)
      status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, null, null);
    super.updateButtonsEnableState(status);
  }

  /**
   * Add the selection and deselection buttons to the dialog.
   * @param composite org.eclipse.swt.widgets.Composite
   */
  protected Composite addSelectionButtons(Composite composite)
  {
    Composite buttonComposite = new Composite(composite, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 0;
    layout.marginWidth = 0;
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    buttonComposite.setLayout(layout);
    buttonComposite.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));

    // select all
    Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, SELECT_ALL_TITLE, false);
    SelectionListener listener = SelectionListener.widgetSelectedAdapter(e -> selectAll(true));
    selectButton.addSelectionListener(listener);

    // deselect all
    Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, DESELECT_ALL_TITLE, false);
    listener = SelectionListener.widgetSelectedAdapter(e -> selectAll(false));
    deselectButton.addSelectionListener(listener);

    return buttonComposite;
  }

  /**
   * Select all
   * @param all
   */
  protected void selectAll(boolean all)
  {
    getCheckboxTreeViewer().setAllChecked(all);
    updateOKStatus();
  }

  @Override
  protected TreeViewer doCreateTreeViewer(Composite parent, int style)
  {
    filteredTree = new FilteredTree(parent, style, filter, true)
    {
      @Override
      protected TreeViewer doCreateTreeViewer(Composite parent, int style)
      {
        return new CheckboxTreeViewer(parent, style);
      }
    };
    filteredTree.setQuickSelectionMode(true);
    return filteredTree.getViewer();
  }

  /**
   * Return the filteredTree
   */
  public FilteredTree getFilteredTree()
  {
    return filteredTree;
  }

  /**
   * Return the CheckboxTreeViewer
   */
  public CheckboxTreeViewer getCheckboxTreeViewer()
  {
    return (CheckboxTreeViewer) filteredTree.getViewer();
  }

  @Override
  protected void computeResult() {
    setSelectionResult(getCheckboxTreeViewer().getCheckedElements());
  }

//  /**
//   * The <code>ListSelectionDialog</code> implementation of this
//   * <code>Dialog</code> method builds a list of the selected elements for later
//   * retrieval by the client and closes this dialog.
//   */
//  @Override
//  protected void okPressed()
//  {
//    // Get the input children.
//    Object[] children = getCheckboxTreeViewer().getCheckedElements();
//    setResult(Arrays.asList(children));
//
//    super.okPressed();
//  }
}
