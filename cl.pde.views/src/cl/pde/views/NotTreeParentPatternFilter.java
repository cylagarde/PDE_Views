package cl.pde.views;

import java.util.Map;
import java.util.WeakHashMap;
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
  private Predicate<Object> visiblePredicate;
  private final Predicate<Object> canSearchOnElementPredicate;
  private StringMatcher matcher;
  Map<Object, Boolean> isLeafMatchCacheMap = new WeakHashMap<>();
  Map<Object, Boolean> isParentMatchCacheMap = new WeakHashMap<>();

  /**
   * Constructor
   */
  public NotTreeParentPatternFilter(Predicate<Object> canSearchOnElementPredicate)
  {
    this.canSearchOnElementPredicate = canSearchOnElementPredicate;
    setIncludeLeadingWildcard(includeLeadingWildcard);
  }

  void clearCache()
  {
    isLeafMatchCacheMap.clear();
    isParentMatchCacheMap.clear();
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

    //    System.err.println("isParentMatch "+element);
    return isParentMatchCacheMap.computeIfAbsent(element, e -> super.isParentMatch(viewer, e));
    //    Boolean isParentMatch = isParentMatchCacheMap.get(element);
    //    if (isParentMatch == null) {
    //      System.err.println("calc "+element);
    //      isParentMatch = super.isParentMatch(viewer, element);
    //      isParentMatchCacheMap.put(element, isParentMatch);
    //    }
    //    else
    //      System.err.println("UUSe cache "+element);
    //    return isParentMatch;
  }

  @Override
  protected boolean isLeafMatch(Viewer viewer, Object element)
  {
    if (!canSearchOnElementPredicate.test(element))
      return false;

    Boolean isLeafMatch = isLeafMatchCacheMap.get(element);
    if (isLeafMatch == null)
    {
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

      isLeafMatch = labelText == null? false: wordMatches(labelText);
      isLeafMatchCacheMap.put(element, isLeafMatch);
    }

    return isLeafMatch;
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

  public boolean canSearchOnElementPredicate(Object element)
  {
    return canSearchOnElementPredicate.test(element);
  }

  public void setVisiblePredicate(Predicate<Object> visiblePredicate)
  {
    this.visiblePredicate = visiblePredicate;
  }
}
