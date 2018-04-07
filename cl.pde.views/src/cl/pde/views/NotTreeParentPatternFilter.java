package cl.pde.views;

import java.util.function.Predicate;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.internal.ui.util.StringMatcher;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * The class <b>NotTreeParentPatternFilter</b> allows to.<br>
 */
public class NotTreeParentPatternFilter extends PatternFilter
{
  private final boolean includeLeadingWildcard = true;
  public Predicate<Object> visiblePredicate;
  public Predicate<Object> canSearchOnElementPredicate;
  private StringMatcher matcher;

  /**
   * Constructor
   */
  public NotTreeParentPatternFilter()
  {
    setIncludeLeadingWildcard(includeLeadingWildcard);
  }

  @Override
  protected boolean isParentMatch(Viewer viewer, Object element)
  {
    if (visiblePredicate != null)
    {
      boolean visible = visiblePredicate.test(element);
      if (!visible)
        return visible;
    }
    return super.isParentMatch(viewer, element);
  }

  @Override
  protected boolean isLeafMatch(Viewer viewer, Object element)
  {
    if (!canSearchOnElementPredicate.test(element))
      return false;

    String labelText = null;

    if (element instanceof TreeObject)
      labelText = ((TreeObject) element).getDisplayText();
    else
    {
      IBaseLabelProvider labelProvider = ((StructuredViewer) viewer).getLabelProvider();
      if (labelProvider instanceof ILabelProvider)
        labelText = ((ILabelProvider) labelProvider).getText(element);
      else if (labelProvider instanceof DelegatingStyledCellLabelProvider)
      {
        DelegatingStyledCellLabelProvider delegatingStyledCellLabelProvider = (DelegatingStyledCellLabelProvider) labelProvider;
        labelText = delegatingStyledCellLabelProvider.getStyledStringProvider().getStyledText(element).toString();
      }
    }

    if (labelText == null)
      return false;
    return wordMatches(labelText);
  }

  @Override
  public void setPattern(String patternString)
  {
    if (patternString == null || patternString.equals("")) //$NON-NLS-1$
      matcher = null;
    else
    {
      String pattern = patternString + "*"; //$NON-NLS-1$
      if (includeLeadingWildcard)
        pattern = "*" + pattern; //$NON-NLS-1$
      matcher = new StringMatcher(pattern, true, false);
    }

    super.setPattern(patternString);
  }

  /**
   * Get first position of text
   * @param text
   */
  public StringMatcher.Position getFirstPosition(String text, int start, int end)
  {
    return matcher == null? null : matcher.find(text, start, end);
  }
}
