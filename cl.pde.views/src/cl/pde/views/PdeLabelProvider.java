package cl.pde.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import cl.pde.Activator;
import cl.pde.Images;

/**
 * The class <b>PdeLabelProvider</b> allows to.<br>
 */
public class PdeLabelProvider extends LabelProvider implements IFontProvider, IColorProvider, IStyledLabelProvider
{
  final Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);

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
      String name = treeObject.name;
      if (name == null)
        name = PDEPlugin.getDefault().getLabelProvider().getText(treeObject.data);

      styledString.append(name, null);

      // version
      int lastIndex = name.lastIndexOf(')');
      int beginIndex = name.lastIndexOf('(', lastIndex);
      if (lastIndex >= 0 && beginIndex >= 0)
        styledString.setStyle(beginIndex, lastIndex - beginIndex + 1, StyledString.COUNTER_STYLER);

      if (treeObject.data != null)
      {
        // location
        String location = Util.getLocation(treeObject.data);
        if (location != null)
          styledString.append(" - " + location, StyledString.QUALIFIER_STYLER);

        //        // resource
        //        IResource resource = Util.getResource(treeObject.data);
        //        if (resource != null)
        //          styledString.append(" - " + resource, StyledString.DECORATIONS_STYLER);
        //        else
        //          styledString.append(" ERROR " + treeObject.data.getClass(), StyledString.DECORATIONS_STYLER);
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
          ImageDescriptor singletonImageDescriptor = Activator.getDefault().getImageDescriptor(Images.SINGLETON);
          String key = String.valueOf(img) + " " + String.valueOf(singletonImageDescriptor);
          Image overlayImage = Activator.getDefault().getImageRegistry().get(key);
          if (overlayImage == null)
          {
            DecorationOverlayIcon overlayIcon = new DecorationOverlayIcon(img, singletonImageDescriptor, IDecoration.TOP_RIGHT);
            overlayImage = overlayIcon.createImage();
            Activator.getDefault().getImageRegistry().put(key, overlayImage);
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
        return boldFont;
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
