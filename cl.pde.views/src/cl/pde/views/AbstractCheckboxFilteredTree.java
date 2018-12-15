package cl.pde.views;

import java.util.function.Predicate;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The class <b>AbstractCheckboxFilteredTree</b> allows to.<br>
 */
public abstract class AbstractCheckboxFilteredTree extends FilteredTree
{
  private Button[] checkboxButtons;
  private StackLayout stackLayout;
  private Label itemNotFoundLabel;
  private Composite checkboxButtonsComposite;

  private Predicate<Object> visiblePredicate = element -> {
    if (element instanceof TreeParent)
    {
      TreeParent treeParent = (TreeParent) element;
      if (treeParent.data != null)
        return true;

      if (checkboxButtons != null)
      {
        for(Button checkboxButton : checkboxButtons)
        {
          String label = (String) checkboxButton.getData("LABEL");
          if (label.equals(treeParent.name))
            return checkboxButton.getSelection();
        }
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
    filter.setVisiblePredicate(visiblePredicate);
    setInitialText("Plugin name filter");
    getFilterControl().setVisible(false);
  }

  /**
   * Get checkbox labels
   */
  protected abstract String[] getCheckboxLabels();

  @Override
  protected Control createTreeControl(Composite parent, int style)
  {
    stackLayout = new StackLayout();
    parent.setLayout(stackLayout);

    Control control = super.createTreeControl(parent, style);
    stackLayout.topControl = control;

    itemNotFoundLabel = new Label(parent, SWT.NONE);
    itemNotFoundLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));

    return control;
  }

  @Override
  protected Composite createFilterControls(Composite parent)
  {
    Composite filterComposite = super.createFilterControls(parent);

    String[] checkboxLabels = getCheckboxLabels();
    if (checkboxLabels != null && checkboxLabels.length != 0)
    {
      Composite content = new Composite(parent.getParent(), SWT.NONE);
      GridLayout layout = new GridLayout(1, false);
      layout.marginWidth = layout.marginHeight = 2;
      content.setLayout(layout);
      content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      filterComposite.setParent(content);

      checkboxButtonsComposite = new Composite(content, SWT.NONE);

      RowLayout buttonLayout = new RowLayout();
      buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
      buttonLayout.marginTop = buttonLayout.marginBottom = 0;
      buttonLayout.spacing = 10;
      checkboxButtonsComposite.setLayout(buttonLayout);
      checkboxButtonsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      SelectionAdapter listener = new SelectionAdapter()
      {
        @Override
        public void widgetSelected(SelectionEvent e)
        {
          textChanged();
        }
      };

      //
      checkboxButtons = new Button[checkboxLabels.length];
      for(int i = 0; i < checkboxButtons.length; i++)
      {
        String label = checkboxLabels[i];
        checkboxButtons[i] = new Button(checkboxButtonsComposite, SWT.CHECK);
        checkboxButtons[i].setText(label);
        checkboxButtons[i].setData("LABEL", label);
        checkboxButtons[i].setToolTipText("See " + label + " node");
        checkboxButtons[i].setSelection(true);
        checkboxButtons[i].setBackground(parent.getBackground());
        checkboxButtons[i].addSelectionListener(listener);
      }
    }

    return filterComposite;
  }

  @Override
  protected void textChanged()
  {
    Util.setUseCache(true);
    ((NotTreeParentPatternFilter) getPatternFilter()).clearCache();
    try
    {
      super.textChanged();
    }
    finally
    {
      ((NotTreeParentPatternFilter) getPatternFilter()).clearCache();
      Util.setUseCache(false);
    }
  }

  protected String getLabelWhenItemNotFound()
  {
    return "Not found";
  }

  @Override
  protected WorkbenchJob doCreateRefreshJob()
  {
    WorkbenchJob refreshJob = super.doCreateRefreshJob();
    refreshJob.addJobChangeListener(new IJobChangeListener()
    {
      @Override
      public void sleeping(IJobChangeEvent event)
      {
      }

      @Override
      public void scheduled(IJobChangeEvent event)
      {
      }

      @Override
      public void running(IJobChangeEvent event)
      {
      }

      @Override
      public void done(IJobChangeEvent event)
      {
        if (treeViewer.getTree().getItemCount() == 0)
        {
          checkboxButtonsComposite.setVisible(false);
          itemNotFoundLabel.setText(getLabelWhenItemNotFound());
          stackLayout.topControl = itemNotFoundLabel;
        }
        else
        {
          checkboxButtonsComposite.setVisible(true);
          stackLayout.topControl = treeViewer.getControl();
        }
        stackLayout.topControl.getParent().layout();
      }

      @Override
      public void awake(IJobChangeEvent event)
      {
      }

      @Override
      public void aboutToRun(IJobChangeEvent event)
      {
      }
    });
    return refreshJob;
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
        if (event.getElement() instanceof TreeObject)
          ((TreeObject) event.getElement()).reset();
        treeViewer.getTree().getDisplay().asyncExec(() -> treeViewer.refresh(event.getElement()));
      }

      @Override
      public void treeCollapsed(TreeExpansionEvent event)
      {
      }
    });

    treeViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new PdeLabelProvider((NotTreeParentPatternFilter) getPatternFilter())));
    setBackground(treeViewer.getTree().getBackground());

    ViewerFilter viewerFilter = new ViewerFilter()
    {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element)
      {
        return visiblePredicate.test(element);
      }
    };

    treeViewer.addFilter(viewerFilter);

    return treeViewer;
  }
}
