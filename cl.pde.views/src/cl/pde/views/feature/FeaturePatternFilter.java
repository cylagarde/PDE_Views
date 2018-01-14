package cl.pde.views.feature;

import java.util.function.Function;

import org.eclipse.jface.viewers.Viewer;

import cl.pde.views.NotTreeParentPatternFilter;

/**
 * The class <b>FeaturePatternFilter</b> allows to.<br>
 */
public class FeaturePatternFilter extends NotTreeParentPatternFilter
{
  public Function<Object, Boolean> selectFunction;

  @Override
  protected boolean isParentMatch(Viewer viewer, Object element)
  {
    if (selectFunction != null)
    {
      Boolean result = selectFunction.apply(element);
      if (result != null)
        return result;
    }
    return super.isParentMatch(viewer, element);
  }

}
