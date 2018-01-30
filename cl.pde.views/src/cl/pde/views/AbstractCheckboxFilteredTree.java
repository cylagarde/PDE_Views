package cl.pde.views;

import java.util.function.Predicate;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;

/**
 * The class <b>AbstractCheckboxFilteredTree</b> allows to.<br>
 */
public abstract class AbstractCheckboxFilteredTree extends FilteredTree
{
  Button[] checkboxButtons;

  Predicate<Object> visiblePredicate = element -> {
    if (element instanceof TreeParent)
    {
      TreeParent treeParent = (TreeParent) element;
      if (treeParent.data != null)
        return true;

      for(Button checkboxButton : checkboxButtons)
      {
        String label = (String) checkboxButton.getData("LABEL");
        if (label.equals(treeParent.name))
          return checkboxButton.getSelection();
      }
    }

    return true;
  };

  /**
   * Constructor
   * @param parent
   * @param filter
   */
  public AbstractCheckboxFilteredTree(Composite parent, NotTreeParentPatternFilter filter)
  {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
    setQuickSelectionMode(true);
    filter.visiblePredicate = visiblePredicate;
    setInitialText("Plugin name filter");
  }

  /**
   * Get checkbox labels
   */
  protected abstract String[] getCheckboxLabels();

  @Override
  protected Composite createFilterControls(Composite parent)
  {
    Composite filterComposite = super.createFilterControls(parent);

    Composite content = new Composite(parent.getParent(), SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    layout.marginWidth = layout.marginHeight = 2;
    content.setLayout(layout);
    content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    filterComposite.setParent(content);

    Composite buttonComposite = new Composite(content, SWT.NONE);
    RowLayout buttonLayout = new RowLayout();
    buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
    buttonLayout.marginTop = buttonLayout.marginBottom = 0;
    buttonLayout.spacing = 10;
    buttonComposite.setLayout(buttonLayout);
    buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    SelectionAdapter listener = new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        textChanged();
      }
    };

    String[] checkboxLabels = getCheckboxLabels();

    //
    checkboxButtons = new Button[checkboxLabels.length];
    for(int i = 0; i < checkboxButtons.length; i++)
    {
      String label = checkboxLabels[i];
      checkboxButtons[i] = new Button(buttonComposite, SWT.CHECK);
      checkboxButtons[i].setText(label);
      checkboxButtons[i].setData("LABEL", label);
      checkboxButtons[i].setToolTipText("See " + label + " node");
      checkboxButtons[i].setSelection(true);
      checkboxButtons[i].setBackground(parent.getBackground());
      checkboxButtons[i].addSelectionListener(listener);
    }

    return filterComposite;
  }

  @Override
  protected TreeViewer doCreateTreeViewer(Composite parent, int style)
  {
    TreeViewer treeViewer = super.doCreateTreeViewer(parent, style | SWT.NO_FOCUS);

    treeViewer.addTreeListener(new ITreeViewerListener()
    {
      @Override
      public void treeExpanded(TreeExpansionEvent event)
      {
        if (event.getElement() instanceof TreeParent)
          ((TreeParent) event.getElement()).reset();
        treeViewer.getTree().getDisplay().asyncExec(() -> treeViewer.refresh(event.getElement()));
      }

      @Override
      public void treeCollapsed(TreeExpansionEvent event)
      {
      }
    });

    treeViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new PdeLabelProvider((NotTreeParentPatternFilter) getPatternFilter())));
    setBackground(treeViewer.getTree().getBackground());

    ViewerFilter featureViewerFilter = new ViewerFilter()
    {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element)
      {
        return visiblePredicate.test(element);
      }
    };

    treeViewer.addFilter(featureViewerFilter);

    return treeViewer;
  }
}
