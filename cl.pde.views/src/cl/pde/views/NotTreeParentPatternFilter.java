package cl.pde.views;

import java.util.function.Predicate;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * The class <b>NotTreeParentPatternFilter</b> allows to.<br>
 */
public class NotTreeParentPatternFilter extends PatternFilter
{
  public Predicate<Object> visiblePredicate;

  public NotTreeParentPatternFilter()
  {
    setIncludeLeadingWildcard(true);
  }

  @Override
  protected boolean isParentMatch(Viewer viewer, Object element)
  {
    if (visiblePredicate != null)
    {
      boolean visible = visiblePredicate.test(element);
      if (! visible)
        return visible;
    }
    return super.isParentMatch(viewer, element);
  }

  @Override
  protected boolean isLeafMatch(Viewer viewer, Object element)
  {
    if (element instanceof TreeParent)
      return false;

    String labelText = null;
    IBaseLabelProvider labelProvider = ((StructuredViewer) viewer).getLabelProvider();
    if (labelProvider instanceof ILabelProvider)
      labelText = ((ILabelProvider) labelProvider).getText(element);
    else if (labelProvider instanceof DelegatingStyledCellLabelProvider)
    {
      DelegatingStyledCellLabelProvider delegatingStyledCellLabelProvider = (DelegatingStyledCellLabelProvider) labelProvider;
      labelText = delegatingStyledCellLabelProvider.getStyledStringProvider().getStyledText(element).toString();
    }

    if (labelText == null)
      return false;
    return wordMatches(labelText);
  }
}
