package timeflow.vis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import timeflow.model.Display;

public class Mouseover extends Rectangle
{
    public final Object thing;

    public Mouseover(Object thing, int x, int y, int w, int h)
    {
        super(x, y, w, h);
        this.thing = thing;
    }

    public void draw(Graphics2D g, int maxW, int maxH, Display display)
    {
        g.setColor(new Color(0, 53, 153));
        g.setColor(new Color(255, 255, 0, 100));
        g.fill(this);
    }
}
