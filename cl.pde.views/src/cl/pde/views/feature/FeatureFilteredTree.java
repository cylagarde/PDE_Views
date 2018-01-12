package cl.pde.views.feature;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import cl.pde.views.PdeLabelProvider;
import cl.pde.views.TreeParent;

/**
  * The class <b>FeatureFilteredTree</b> allows to.<br>
  */
class FeatureFilteredTree extends FilteredTree
{
  Button searchInIncludedPluginsButton;
  Button searchInIncludedFeaturesButton;
  Button searchInDependenciesButton;

  FeatureFilteredTree(Composite parent, PatternFilter filter)
  {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
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
    buttonComposite.setVisible(false);
    GridLayout buttonLayout = new GridLayout(3, false);
    buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
    buttonLayout.horizontalSpacing = 10;
    buttonComposite.setLayout(buttonLayout);
    buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    searchInIncludedPluginsButton = new Button(buttonComposite, SWT.CHECK);
    searchInIncludedPluginsButton.setText("Included plugins");
    searchInIncludedPluginsButton.setToolTipText("See included plugins node");
    searchInIncludedPluginsButton.setSelection(true);
    searchInIncludedPluginsButton.setBackground(parent.getBackground());

    searchInIncludedFeaturesButton = new Button(buttonComposite, SWT.CHECK);
    searchInIncludedFeaturesButton.setText("Included features");
    searchInIncludedFeaturesButton.setToolTipText("See included features node");
    searchInIncludedFeaturesButton.setSelection(true);
    searchInIncludedFeaturesButton.setBackground(parent.getBackground());

    searchInDependenciesButton = new Button(buttonComposite, SWT.CHECK);
    searchInDependenciesButton.setText("Dependencies");
    searchInDependenciesButton.setToolTipText("See dependencies node");
    searchInDependenciesButton.setSelection(true);
    searchInDependenciesButton.setBackground(parent.getBackground());

    SelectionAdapter listener = new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        textChanged();
      }
    };
    searchInIncludedPluginsButton.addSelectionListener(listener);
    searchInIncludedFeaturesButton.addSelectionListener(listener);
    searchInDependenciesButton.addSelectionListener(listener);

    buttonComposite.setVisible(true);

    return filterComposite;
  }

  @Override
  protected TreeViewer doCreateTreeViewer(Composite parent, int style)
  {
    TreeViewer featureViewer = super.doCreateTreeViewer(parent, style);

    featureViewer.setContentProvider(new FeatureViewContentProvider());
    featureViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new PdeLabelProvider()));
    setBackground(featureViewer.getTree().getBackground());

    ViewerFilter featureViewerFilter = new ViewerFilter()
    {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element)
      {
        if (element instanceof TreeParent)
        {
          TreeParent treeParent = (TreeParent) element;
          // included plugins
          if (PDEUIMessages.FeatureEditor_ReferencePage_title.equals(treeParent.name))
            return searchInIncludedPluginsButton.getSelection();

          // included features
          if (PDEUIMessages.FeatureEditor_IncludesPage_title.equals(treeParent.name))
            return searchInIncludedFeaturesButton.getSelection();

          //
          if (PDEUIMessages.FeatureEditor_DependenciesPage_title.equals(treeParent.name))
            return searchInDependenciesButton.getSelection();
        }

        return true;
      }
    };

    featureViewer.addFilter(featureViewerFilter);

    return featureViewer;
  }
};
