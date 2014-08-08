package timeflow.vis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Objects;

import timeflow.data.db.Act;
import timeflow.data.time.RoughTime;
import timeflow.model.Display;
import timeflow.util.ColorUtils;
import timeflow.vis.timeline.TimelineTrack;

public class VisualAct implements Comparable<VisualAct>
{
    final Act act;
    Color color;
    String label;
    String mouseOver;
    double size = 1;
    String trackString;
    TimelineTrack track;
    boolean visible;
    int x, y;
    int spaceNextTo;
    RoughTime start, end;
    int endX;

    public VisualAct(Act act)
    {
        this.act = act;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getDotR()
    {
        return Math.max(1, (int) Math.abs(size));
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }


    public void draw(Graphics2D g, int ox, int oy, int r, Rectangle maxFill, boolean showDuration)
    {
        g.setColor(getColor());
        if (size >= 0)
        {
            g.fillOval(ox, y - r, 2 * r, 2 * r);
        }
        else
        {
            g.drawOval(ox, y - r, 2 * r, 2 * r);
        }
        if (end != null && showDuration)
        {
            int lineY = y + 6;
            g.fillRect(getX(), lineY, getEndX() - getX(), 2);
            g.drawLine(getX(), lineY, getX(), lineY - 4);
        }

    }

    // TODO: what is up with all these magic numbers??
    public Mouseover draw(Graphics2D g, Rectangle maxFill, Rectangle bounds,
                          Display display, boolean showText, boolean showDuration, boolean leftToRight)
    {
        if (!isVisible())
        {
            return null;
        }

        if (x > bounds.x + bounds.width && (end == null || endX > bounds.x + bounds.width) ||
            x < bounds.x - 200 && (end == null || endX < bounds.x - 200))
        {
            return null;
        }

        g.setFont(display.plain());

        // Limit the radius to 1 - 30
        int radius = Math.min(Math.max(getDotR(), 1), 30);

        int originX = showText ? x - 2 * radius : x;
        draw(g, originX, y - 2, radius, maxFill, showDuration);

        if (!showText)
        {
            return new VisualActMouseover(this, originX - 2, y - radius - 4, 4 + 2 * radius, 4 + 2 * radius);
        }

        int textXInset = 5;
        int labelSpace = getSpaceNextTo() - textXInset * 2;
        int stringWidth = 0;
        int stringHeight = 0;

        if (labelSpace > 50)
        {
            String text = display.format(getLabel(), labelSpace / display.plainFontMetrics().charWidth('m'), true);
            stringWidth = display.plainFontMetrics().stringWidth(text);

            int n = text.indexOf(' ');
            int tx = leftToRight ? originX + textXInset : originX - stringWidth - textXInset;
            int ty = y + 4;

            if (n < 1)
            {
                g.drawString(text, tx, ty);
            }
            else
            {
                String first = text.substring(0, n);
                g.drawString(first, tx, ty);
                Color c = ColorUtils.interpolate(g.getColor(), Color.white, .33);
                g.setColor(c);
                g.drawString(text.substring(n), tx + display.plainFontMetrics().stringWidth(first), ty);
            }

            stringHeight = display.plainFontMetrics().getHeight();
        }

        int vx = leftToRight ? originX - textXInset : originX - stringWidth - 2 * textXInset;
        int vy = y - 3 - Math.max(radius, stringHeight / 2);
        int width = 2 * radius + 3 * textXInset + stringWidth;
        int height = 4 + Math.max(2 * radius, stringHeight);

        return new VisualActMouseover(this, vx, vy, width, height);
    }


    public Act getAct()
    {
        return act;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public RoughTime getStart()
    {
        return start;
    }

    public void setStart(RoughTime start)
    {
        this.start = start;
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getMouseOver()
    {
        return mouseOver;
    }

    public void setMouseOver(String mouseOver)
    {
        this.mouseOver = mouseOver;
    }

    public double getSize()
    {
        return size;
    }

    public void setSize(double size)
    {
        this.size = size;
    }

    public String getTrackString()
    {
        return trackString == null ? "" : trackString;
    }

    public void setTrackString(String track)
    {
        this.trackString = track;
    }

    public TimelineTrack getTrack()
    {
        return track;
    }

    public void setTrack(TimelineTrack track)
    {
        this.track = track;
    }

    public int getSpaceNextTo()
    {
        return spaceNextTo;
    }

    public void setSpaceNextTo(int spaceNextTo)
    {
        this.spaceNextTo = spaceNextTo;
    }

    public int getEndX()
    {
        return endX;
    }

    public void setEndX(int endX)
    {
        this.endX = endX;
    }

    public RoughTime getEnd()
    {
        return end;
    }

    public void setEnd(RoughTime end)
    {
        this.end = end;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(act, label, start, end);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof VisualAct))
        {
            return false;
        }

        VisualAct other = (VisualAct) obj;

        return
            Objects.equals(act, other.act) &&
            Objects.equals(label, other.label) &&
            Objects.equals(start, other.start) &&
            Objects.equals(end, other.end);
    }

    @Override
    public int compareTo(VisualAct o)
    {
        int comparison = RoughTime.compare(start, o.start);
        if (comparison != 0)
        {
            return comparison;
        }

        return Integer.compare(hashCode(), o.hashCode());
    }
}
