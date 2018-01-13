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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import cl.pde.views.Constants;
import cl.pde.views.PdeLabelProvider;
import cl.pde.views.TreeParent;

/**
  * The class <b>FeatureFilteredTree</b> allows to.<br>
  */
class FeatureFilteredTree extends FilteredTree
{
  Button seeWorkspaceFeatureButton;
  Button seeExternalFeatureButton;
  Button seeIncludedPluginsButton;
  Button seeIncludedFeaturesButton;
  Button seeDependenciesButton;

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
    RowLayout buttonLayout = new RowLayout();
    buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
    buttonLayout.spacing = 10;
    buttonComposite.setLayout(buttonLayout);
    buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    //
    seeWorkspaceFeatureButton = new Button(buttonComposite, SWT.CHECK);
    seeWorkspaceFeatureButton.setText(Constants.WORKSPACE_FEATURE);
    seeWorkspaceFeatureButton.setToolTipText("See " + Constants.WORKSPACE_FEATURE + " node");
    seeWorkspaceFeatureButton.setSelection(true);
    seeWorkspaceFeatureButton.setBackground(parent.getBackground());

    //
    seeExternalFeatureButton = new Button(buttonComposite, SWT.CHECK);
    seeExternalFeatureButton.setText(Constants.TARGET_FEATURE);
    seeExternalFeatureButton.setToolTipText("See " + Constants.TARGET_FEATURE + " node");
    seeExternalFeatureButton.setSelection(true);
    seeExternalFeatureButton.setBackground(parent.getBackground());

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
    seeWorkspaceFeatureButton.addSelectionListener(listener);
    seeExternalFeatureButton.addSelectionListener(listener);
    seeIncludedPluginsButton.addSelectionListener(listener);
    seeIncludedFeaturesButton.addSelectionListener(listener);
    seeDependenciesButton.addSelectionListener(listener);

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
          if (treeParent.data != null)
            return true;

          // Workspace
          if (Constants.WORKSPACE_FEATURE.equals(treeParent.name))
            return seeWorkspaceFeatureButton.getSelection();

          // External
          if (Constants.TARGET_FEATURE.equals(treeParent.name))
            return seeExternalFeatureButton.getSelection();

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
      }
    };

    featureViewer.addFilter(featureViewerFilter);

    return featureViewer;
  }
};
