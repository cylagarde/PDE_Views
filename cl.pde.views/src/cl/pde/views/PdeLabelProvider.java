package cl.pde.views;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.util.StringMatcher.Position;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;

import cl.pde.Images;
import cl.pde.PDEViewActivator;

/**
 * The class <b>PdeLabelProvider</b> allows to.<br>
 */
public class PdeLabelProvider extends LabelProvider implements IFontProvider, IColorProvider, IStyledLabelProvider
{
  final static ColorRegistry COLOR_REGISTRY = JFaceResources.getColorRegistry();
  final static Font BOLD_FONT = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);

  final static Styler SELECTION_STYLER;
  final static Styler VERSION_STYLER;
  final static Styler LOCATION_STYLER;
  final static Styler DECORATIONS_STYLER;

  static
  {
    COLOR_REGISTRY.put("SELECTION_FOREGROUND", new RGB(255, 0, 0));
    COLOR_REGISTRY.put("VERSION_COLOR", Constants.VERSION_FOREGROUND != null? Constants.VERSION_FOREGROUND.getRGB() : new RGB(0, 0, 0));
    COLOR_REGISTRY.put("DECORATIONS_COLOR", new RGB(149, 125, 71));
    COLOR_REGISTRY.put("LOCATION_COLOR", new RGB(128, 128, 128));

    // stylers
    SELECTION_STYLER = new Styler()
    {
      @Override
      public void applyStyles(TextStyle textStyle)
      {
        textStyle.font = BOLD_FONT;
        textStyle.foreground = COLOR_REGISTRY.get("SELECTION_FOREGROUND");
      }
    };
    VERSION_STYLER = StyledString.createColorRegistryStyler("VERSION_COLOR", null);
    LOCATION_STYLER = StyledString.createColorRegistryStyler("LOCATION_COLOR", null);
    DECORATIONS_STYLER = StyledString.createColorRegistryStyler("DECORATIONS_COLOR", null);
  }

  final NotTreeParentPatternFilter patternFilter;

  /**
   * Constructor
   * @param patternFilter
   */
  public PdeLabelProvider(NotTreeParentPatternFilter patternFilter)
  {
    this.patternFilter = patternFilter;
  }

  @Override
  public String getText(Object element)
  {
    return getStyledText(element).toString();
  }

  @Override
  public StyledString getStyledText(Object element)
  {
    StyledString styledString = new StyledString();
    if (element instanceof TreeObject)
    {
      TreeObject treeObject = (TreeObject) element;
      String name = treeObject.getLabelText();

      styledString.append(name, null);

      // version
      int lastIndex = name.lastIndexOf(')');
      int beginIndex = name.lastIndexOf('(', lastIndex);
      if (lastIndex >= 0 && beginIndex >= 0)
        styledString.setStyle(beginIndex, lastIndex - beginIndex + 1, VERSION_STYLER);

      if (treeObject.data != null)
      {
        // location
        String location = Util.getLocation(treeObject.data);
        if (location != null)
          styledString.append(" - " + location, LOCATION_STYLER);

        //        // resource
        //        IResource resource = Util.getResource(treeObject.data);
        //        if (resource != null)
        //          styledString.append(" - " + resource, DECORATIONS_STYLER);
        //        else
        //          styledString.append(" ERROR " + treeObject.data.getClass(), DECORATIONS_STYLER);
      }

      //
      if (patternFilter.canSearchOnElementPredicate.test(element))
      {
        String text = styledString.getString();
        Position firstPosition = patternFilter.getFirstPosition(text, 0, text.length());
        if (firstPosition != null)
          styledString.setStyle(firstPosition.getStart(), firstPosition.getEnd() - firstPosition.getStart(), SELECTION_STYLER);
      }
    }
    else
      styledString.append(String.valueOf(element), null);
    return styledString;
  }

  @Override
  public Image getImage(Object obj)
  {
    if (obj instanceof TreeObject)
    {
      TreeObject treeObject = (TreeObject) obj;
      if (treeObject.image == null)
      {
        Image img = PDEPlugin.getDefault().getLabelProvider().getImage(treeObject.data);

        Boolean singletonState = Util.getSingletonState(treeObject.data);
        if (singletonState != null && singletonState)
        {
          //
          ImageDescriptor singletonImageDescriptor = PDEViewActivator.getImageDescriptor(Images.SINGLETON);
          String key = String.valueOf(img) + " " + String.valueOf(singletonImageDescriptor);
          Image overlayImage = PDEViewActivator.getDefault().getImageRegistry().get(key);
          if (overlayImage == null)
          {
            DecorationOverlayIcon overlayIcon = new DecorationOverlayIcon(img, singletonImageDescriptor, IDecoration.TOP_RIGHT);
            overlayImage = overlayIcon.createImage();
            PDEViewActivator.getDefault().getImageRegistry().put(key, overlayImage);
          }
          img = overlayImage;
        }

        return img;
      }

      return treeObject.image;
    }

    return null;
  }

  @Override
  public Font getFont(Object obj)
  {
    if (obj instanceof TreeParent)
    {
      TreeParent treeParent = (TreeParent) obj;
      if (treeParent.name != null && treeParent.data == null)
        return BOLD_FONT;
    }
    return null;
  }

  @Override
  public Color getForeground(Object obj)
  {
    if (obj instanceof TreeObject)
    {
      TreeObject treeObject = (TreeObject) obj;
      if (treeObject.foreground != null)
        return treeObject.foreground;
    }
    return null;
  }

  @Override
  public Color getBackground(Object element)
  {
    return null;
  }
}
