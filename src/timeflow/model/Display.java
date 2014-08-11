package timeflow.model;


import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import sun.font.FontUtilities;
import timeflow.data.db.filter.ValueFilter;
import timeflow.data.time.RoughTime;

// to do: read from a properties file!
public class Display
{
    public static final String MISC_CODE = "Misc.    ";
    public static final double MAX_DOT_SIZE = 10;
    public static final int CALENDAR_CELL_HEIGHT = 80;
    /**
     * The resources.
     */
    private static final ResourceBundle bundle = ResourceBundle.getBundle("timeflow/model/Bundle");
    private static final double PHI = (1 + Math.sqrt(5)) / 2;
    public static Color barColor = new Color(150, 170, 200);
    static DecimalFormat df = new DecimalFormat("###,###,###,###.##");
    static DecimalFormat roundFormat = new DecimalFormat("###,###,###,###");
    static Color[] handPalette = {
        new Color(203, 31, 23),
        new Color(237, 131, 0),
        new Color(71, 175, 13),
        new Color(6, 119, 207),
        new Color(0, 188, 184),
        new Color(209, 80, 174),
        new Color(146, 6, 0),
        new Color(175, 103, 0),
        new Color(76, 124, 0),
        new Color(0, 80, 143),
        new Color(0, 128, 124),
        new Color(153, 56, 126)
    };
    HashMap<String, Integer> ints = new HashMap<String, Integer>();
    HashMap<String, Color> colors = new HashMap<String, Color>();
    HashMap<String, Color> topColors = new HashMap<String, Color>();
    ValueFilter grayFilter;

    final Font tinyFont;
    final Font timeLabelFont;
    final Font smallFont;
    final Font boldFont;
    final Font hugeFont;
    final Font plainFont = UIManager.getFont("Label.font");
    final Font bigFont = plainFont.deriveFont(Font.BOLD);
    final FontMetrics timeLabelFontMetrics;
    final FontMetrics tinyFontMetrics;
    final FontMetrics boldFontMetrics;
    final FontMetrics hugeFontMetrics;
    final FontMetrics plainFontMetrics;

    public Display()
    {
        colors.put("chart.background", Color.white);
        Color ui = new Color(240, 240, 240);
        colors.put("splash.background", Color.white);
        colors.put("splash.text", new Color(50, 50, 50));
        colors.put("filterpanel.background", ui);
        colors.put("visualpanel.background", ui);
        colors.put("text.prominent", Color.black);
        colors.put("text.normal", Color.gray);
        colors.put("timeline.label", new Color(0, 53, 153));
        colors.put("timeline.label.lesser", new Color(110, 153, 200));
        colors.put("timeline.grid", new Color(240, 240, 240));
        colors.put("timeline.grid.vertical", new Color(240, 240, 240));
        colors.put("timeline.zebra", new Color(245, 245, 245));
        colors.put("null.color", new Color(230, 230, 230));
        colors.put("timeline.unspecified.color", new Color(0, 53, 153));
        colors.put("highlight.color", new Color(0, 53, 153));

        ints.put("timeline.datelabel.height", 20);
        ints.put("timeline.item.height.min", 16);

        Font baseFont = FontUtilities.getCompositeFontUIResource(new Font("Verdana", Font.PLAIN, 11));
        tinyFont = baseFont.deriveFont(baseFont.getStyle() | Font.BOLD, 9);
        smallFont = baseFont.deriveFont(11f);
        boldFont = baseFont.deriveFont(baseFont.getStyle() | Font.BOLD, 12);
        hugeFont= baseFont.deriveFont(baseFont.getStyle() | Font.BOLD, 16);
        timeLabelFont = tinyFont;

        timeLabelFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(timeLabelFont);
        tinyFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(tinyFont);
        boldFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(boldFont);
        hugeFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(hugeFont);
        plainFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(plainFont);
    }

    public static void launchBrowser(String urlString)
    {
        if (Desktop.isDesktopSupported())
        {
            Desktop desktop = Desktop.getDesktop();
            try
            {
                desktop.browse(new URI(urlString));
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }
        else
        {
            System.out.println("Desktop not supported!");
        }
    }

    public static String arrayToString(Object[] s)
    {
        if (s == null || s.length == 0)
        {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length; i++)
        {
            if (i > 0)
            {
                b.append(", ");
            }
            b.append(s[i]);

        }
        return b.toString();
    }

    public static String version()
    {
        return "TimeFlow 0.5";
    }

    public static Color palette(double x)
    {
        float h = (float) (Math.abs(x) % 1);
        float s = .8f - .25f * h;
        float b = .8f - .25f * h;
        return new Color(Color.HSBtoRGB(h, s, b));
    }

    public static String format(double x)
    {
        return Math.abs(x) > 999 ? roundFormat.format(x) : df.format(x);
    }

    public Font bold()
    {
        return boldFont;
    }

    public Font plain()
    {
        return plainFont;
    }

    public Font small()
    {
        return smallFont;
    }

    public FontMetrics hugeFontMetrics()
    {
        return hugeFontMetrics;
    }

    public FontMetrics plainFontMetrics()
    {
        return plainFontMetrics;
    }

    public FontMetrics boldFontMetrics()
    {
        return boldFontMetrics;
    }

    public FontMetrics tinyFontMetrics()
    {
        return tinyFontMetrics;
    }

    public FontMetrics timeLabelFontMetrics()
    {
        return timeLabelFontMetrics;
    }

    public Font huge()
    {
        return hugeFont;
    }

    public Font big()
    {
        return bigFont;
    }

    public Font tiny()
    {
        return tinyFont;
    }

    public Font timeLabel()
    {
        return timeLabelFont;
    }

    public String getString(String s)
    {
        return bundle.getString(s);
    }

    public int getInt(String s)
    {
        return ints.get(s);
    }

    public Color getColor(String key)
    {
        if (colors.get(key) == null)
        {
            throw new IllegalArgumentException("No color for " + key);
        }
        return colors.get(key);
    }

    public JLabel label(String s)
    {
        return new JLabel(s);
    }

    /**
     * Formats the input string to a limited length. This method will sensibly chop Arabic text at word boundaries.
     *
     * @param input the input string.
     * @param component the component which holds the string to draw.
     * @param fontMetrics the font metrics.
     * @param width the maximum width.
     * @return the string, possibly truncated.
     */
    public String format(String input, JComponent component, FontMetrics fontMetrics, int width)
    {
        Rectangle viewRectangle = new Rectangle(0, 0, width, fontMetrics.getHeight());
        Rectangle textRectangle = new Rectangle(viewRectangle);
        Rectangle iconRectangle = new Rectangle();

        return SwingUtilities.layoutCompoundLabel(
            component,
            fontMetrics,
            input,
            null, // No icon
            SwingConstants.BOTTOM,
            SwingUtilities.LEADING,
            SwingConstants.BOTTOM,
            SwingUtilities.LEADING,
            viewRectangle,
            iconRectangle,
            textRectangle,
            0); // Icon/text gap
    }

    /**
     * Formats a string.
     *
     * @param s
     * @param maxLength
     * @param tryNoDots
     * @return
     * @deprecated splits the string mid-word which is not valid for Arabic text. Use SwingUtilities.layoutCompoundLabel() instead.
     */
    @Deprecated
    public String format(String s, int maxLength, boolean tryNoDots)
    {
        if (s == null)
        {
            return "";
        }
        int n = s.length();
        if (n <= maxLength)
        {
            return s;
        }
        if (maxLength < 4)
        {
            return "...";
        }
        if (!tryNoDots)
        {
            return s.substring(0, maxLength - 3) + "...";
        }
        // find last space before maxLength and after maxLength/2
        for (int j = maxLength - 1; j > maxLength / 2; j--)
        {
            if (s.charAt(j) == ' ')
            {
                return s.substring(0, j);
            }
        }
        return s.length() <= maxLength ? s : (maxLength < 6 ? "" : s.substring(0, maxLength - 3) + "...");
    }

    public void refreshColors(Iterable<String> list)
    {
        topColors = new HashMap<String, Color>();
        double x = .1;
        int i = 0;
        for (String s : list)
        {
            topColors.put(s, i < handPalette.length ? handPalette[i] : palette(x));
            i++;
            x += PHI;
        }
    }

    public Color makeColor(String text)
    {
        if (grayFilter != null && !grayFilter.ok(text))
        {
            return new Color(200, 200, 200);
        }
        Color c = topColors.get(text);
        return c == null ? _makeColor(text) : c;
    }

    private Color _makeColor(String text)
    {
        if (text == null)
        {
            return getColor("null.color");
        }

        int c = Math.abs(text.hashCode());
        double h = ((c >> 8) % 255) / 255.;
        return palette(h);
    }

    public String getMiscLabel()
    {
        return getString("other.label");
    }

    public String getNullLabel()
    {
        return getString("null.label");
    }

    public String toString(Object o)
    {
        if (o == null)
        {
            return getNullLabel();
        }
        if (o instanceof Object[])
        {
            return arrayToString((Object[]) o);
        }
        if (o instanceof RoughTime)
        {
            return ((RoughTime) o).format();
        }
        if (o instanceof Double)
        {
            return df.format(o);
        }
        return o.toString();
    }

    public String format(RoughTime time)
    {
        if (time == null)
        {
            return getString("null.label");
        }
        return time.format();
    }

    public boolean emptyMessage(Graphics g, TFModel model)
    {
        if (model.getActs() == null || model.getActs().size() == 0)
        {
            g.setColor(getColor("text.prominent"));

            String label = model.getDB() == null || model.getDB().size() == 0
                           ? bundle.getString("emptyDatabase")
                           : bundle.getString("noItemsFound");

            g.drawString(label, 10, 25);
            return true;
        }
        return false;
    }
}
