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
  Button seIncludedFeaturesButton;
  Button seDependenciesButton;

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
    GridLayout buttonLayout = new GridLayout(5, false);
    buttonLayout.marginWidth = buttonLayout.marginHeight = 0;
    buttonLayout.horizontalSpacing = 10;
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
    seeExternalFeatureButton.setText(Constants.EXTERNAL_FEATURE);
    seeExternalFeatureButton.setToolTipText("See " + Constants.EXTERNAL_FEATURE + " node");
    seeExternalFeatureButton.setSelection(true);
    seeExternalFeatureButton.setBackground(parent.getBackground());

    //
    seeIncludedPluginsButton = new Button(buttonComposite, SWT.CHECK);
    seeIncludedPluginsButton.setText(PDEUIMessages.FeatureEditor_ReferencePage_title);
    seeIncludedPluginsButton.setToolTipText("See " + PDEUIMessages.FeatureEditor_ReferencePage_title + " node");
    seeIncludedPluginsButton.setSelection(true);
    seeIncludedPluginsButton.setBackground(parent.getBackground());

    //
    seIncludedFeaturesButton = new Button(buttonComposite, SWT.CHECK);
    seIncludedFeaturesButton.setText(PDEUIMessages.FeatureEditor_IncludesPage_title);
    seIncludedFeaturesButton.setToolTipText("See " + PDEUIMessages.FeatureEditor_IncludesPage_title + " node");
    seIncludedFeaturesButton.setSelection(true);
    seIncludedFeaturesButton.setBackground(parent.getBackground());

    //
    seDependenciesButton = new Button(buttonComposite, SWT.CHECK);
    seDependenciesButton.setText(PDEUIMessages.FeatureEditor_DependenciesPage_title);
    seDependenciesButton.setToolTipText("See " + PDEUIMessages.FeatureEditor_DependenciesPage_title + " node");
    seDependenciesButton.setSelection(true);
    seDependenciesButton.setBackground(parent.getBackground());

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
    seIncludedFeaturesButton.addSelectionListener(listener);
    seDependenciesButton.addSelectionListener(listener);

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
          if (Constants.EXTERNAL_FEATURE.equals(treeParent.name))
            return seeExternalFeatureButton.getSelection();

          // included plugins
          if (PDEUIMessages.FeatureEditor_ReferencePage_title.equals(treeParent.name))
            return seeIncludedPluginsButton.getSelection();

          // included features
          if (PDEUIMessages.FeatureEditor_IncludesPage_title.equals(treeParent.name))
            return seIncludedFeaturesButton.getSelection();

          //
          if (PDEUIMessages.FeatureEditor_DependenciesPage_title.equals(treeParent.name))
            return seDependenciesButton.getSelection();
        }

        return true;
      }
    };

    featureViewer.addFilter(featureViewerFilter);

    return featureViewer;
  }
};
