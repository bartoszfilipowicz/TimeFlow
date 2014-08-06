package timeflow.model;


import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.UIManager;

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
    Color fallback = new Color(0, 53, 153, 128);
    String fontName = "Verdana";
    Font tinyFont = new Font(fontName, Font.BOLD, 9);
    Font timeLabelFont = tinyFont;
    FontMetrics timeLabelFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(timeLabelFont);
    FontMetrics tinyFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(tinyFont);
    Font smallFont = new Font(fontName, Font.PLAIN, 11);
    Font boldFont = new Font(fontName, Font.BOLD, 12);
    FontMetrics boldFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(boldFont);
    Font hugeFont = new Font(fontName, Font.BOLD, 16);
    FontMetrics hugeFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(hugeFont);
    Font plainFont = UIManager.getFont("Label.font");
    FontMetrics plainFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(plainFont);
    Font bigFont = plainFont.deriveFont(Font.BOLD);
    FontMetrics bigFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(bigFont);
    HashMap<String, Color> topColors = new HashMap<String, Color>();
    ValueFilter grayFilter;

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
        StringBuffer b = new StringBuffer();
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

    public static ArrayList<String> breakLines(String s, int lineChars, int firstOffset)
    {
        ArrayList<String> lines = new ArrayList<String>();
        String[] words = s.split(" ");
        String line = "";

        for (int i = 0; i < words.length; i++)
        {
            // is the word just too, too long?
            int n = words[i].length();
            if (n > lineChars - 5)
            {
                words[i] = words[i].substring(0, lineChars / 2 - 2) + "..." + words[i].substring(n - lineChars / 2 + 2, n);
            }
            if (line.length() + words[i].length() > (lines.size() == 0 ? lineChars - firstOffset : lineChars))
            {
                lines.add(line);
                line = "";
            }
            line += " " + words[i];
        }
        lines.add(line);
        return lines;
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
            return new Color(200, 200, 200);//"null.color");
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
            return ((RoughTime) o).format();//UnitOfTime.format((RoughTime)o);
        }
        if (o instanceof Double)
        {
            return df.format((Double) o);
        }
        return o.toString();
    }

    public String format(RoughTime time)
    {
        if (time == null)
        {
            return getString("null.label");
        }
        return time.format();//UnitOfTime.format(time);
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
