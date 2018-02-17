package cl.pde.views;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.swt.graphics.RGB;

/**
 * The class <b>RTFBasic</b> allows to.<br>
 */
public class RTFBasic
{
  StringBuilder rtfBuffer = new StringBuilder(4096);
  Map<RGB, String> colorMap = new HashMap<>();
  boolean useBold = false;
  boolean useItalic = false;
  String colorRTF = null;

  /**
   * Constructor
   * @param colorStream
   */
  public RTFBasic(Stream<RGB> colorStream)
  {
    //
    rtfBuffer.append("{\\rtf1\n");

    rtfBuffer.append("{\\colortbl;\n");

    // colors
    colorStream.distinct().filter(Objects::nonNull).forEach(color -> {
      //
      colorMap.put(color, "\\cf" + (1 + colorMap.size()));

      rtfBuffer.append("\\red");
      rtfBuffer.append(color.red);
      rtfBuffer.append("\\green");
      rtfBuffer.append(color.green);
      rtfBuffer.append("\\blue");
      rtfBuffer.append(color.blue);
      rtfBuffer.append(";\n");
    });
    rtfBuffer.append("}\n");
  }

  /**
   *
   * @param color
   */
  public void useColor(RGB color)
  {
    String colorRTF = colorMap.get(color);
    if (!Objects.equals(this.colorRTF, colorRTF))
    {
      this.colorRTF = colorRTF;
      if (colorRTF != null)
        rtfBuffer.append(colorRTF);
    }
  }

  /**
   *
   * @param useBold
   */
  public void useBold(boolean useBold)
  {
    if (this.useBold == useBold)
      return;

    this.useBold = useBold;
    rtfBuffer.append(useBold? "\\b" : "\\b0");
  }

  /**
   *
   * @param useItalic
   */
  public void useItalic(boolean useItalic)
  {
    if (this.useItalic == useItalic)
      return;

    this.useItalic = useItalic;
    rtfBuffer.append(useItalic? "\\i" : "\\i0");
  }

  /**
   *
   * @param text
   */
  public void append(String text)
  {
    rtfBuffer.append(text);
  }

  /**
   *
   */
  public void appendln()
  {
    rtfBuffer.append("\\line\n");
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return rtfBuffer.toString() + "}";
  }
}
