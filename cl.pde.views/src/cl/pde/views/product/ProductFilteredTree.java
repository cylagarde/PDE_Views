package cl.pde.views.product;

import java.util.function.Predicate;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;

import cl.pde.views.NotTreeParentPatternFilter;
import cl.pde.views.PdeLabelProvider;
import cl.pde.views.TreeParent;

/**
 * The class <b>ProductFilteredTree</b> allows to.<br>
 */
class ProductFilteredTree extends FilteredTree
{
  Button seeIncludedPluginsButton;
  Button seeIncludedFeaturesButton;
  Button seeDependenciesButton;

  Predicate<Object> visiblePredicate = element -> {
    if (element instanceof TreeParent)
    {
      TreeParent treeParent = (TreeParent) element;
      if (treeParent.data != null)
        return true;

      // included plugins
      if (PDEUIMessages.FeatureEditor_ReferencePage_title.equals(treeParent.name))
        return seeIncludedPluginsButton.getSelection();

      // included features
      if (PDEUIMessages.FeatureEditor_IncludesPage_title.equals(treeParent.name))
        return seeIncludedFeaturesButton.getSelection();

      //
      if (PDEUIMessages.FeatureEditor_DependenciesPage_title.equals(treeParent.name))
        return seeDependenciesButton.getSelection();
    }

    return true;
  };

  /**
   * Constructor
   * @param parent
   * @param filter
   */
  ProductFilteredTree(Composite parent, NotTreeParentPatternFilter filter)
  {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
    filter.visiblePredicate = visiblePredicate;
    setInitialText("Plugin name filter");
  }

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
    buttonLayout.spacing = 10;
    buttonComposite.setLayout(buttonLayout);
    buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    //
    seeIncludedPluginsButton = new Button(buttonComposite, SWT.CHECK);
    seeIncludedPluginsButton.setText(PDEUIMessages.FeatureEditor_ReferencePage_title);
    seeIncludedPluginsButton.setToolTipText("See " + PDEUIMessages.FeatureEditor_ReferencePage_title + " node");
    seeIncludedPluginsButton.setSelection(true);
    seeIncludedPluginsButton.setBackground(parent.getBackground());

    //
    seeIncludedFeaturesButton = new Button(buttonComposite, SWT.CHECK);
    seeIncludedFeaturesButton.setText(PDEUIMessages.FeatureEditor_IncludesPage_title);
    seeIncludedFeaturesButton.setToolTipText("See " + PDEUIMessages.FeatureEditor_IncludesPage_title + " node");
    seeIncludedFeaturesButton.setSelection(true);
    seeIncludedFeaturesButton.setBackground(parent.getBackground());

    //
    seeDependenciesButton = new Button(buttonComposite, SWT.CHECK);
    seeDependenciesButton.setText(PDEUIMessages.FeatureEditor_DependenciesPage_title);
    seeDependenciesButton.setToolTipText("See " + PDEUIMessages.FeatureEditor_DependenciesPage_title + " node");
    seeDependenciesButton.setSelection(true);
    seeDependenciesButton.setBackground(parent.getBackground());

    SelectionAdapter listener = new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        textChanged();
      }
    };
    seeIncludedPluginsButton.addSelectionListener(listener);
    seeIncludedFeaturesButton.addSelectionListener(listener);
    seeDependenciesButton.addSelectionListener(listener);

    return filterComposite;
  }

  @Override
  protected TreeViewer doCreateTreeViewer(Composite parent, int style)
  {
    TreeViewer productViewer = super.doCreateTreeViewer(parent, style);

    productViewer.setContentProvider(new ProductViewContentProvider());
    productViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new PdeLabelProvider()));
    setBackground(productViewer.getTree().getBackground());

    ViewerFilter featureViewerFilter = new ViewerFilter()
    {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element)
      {
        return visiblePredicate.test(element);
      }
    };

    productViewer.addFilter(featureViewerFilter);

    return productViewer;
  }
};
