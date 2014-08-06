package timeflow.vis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

import timeflow.data.db.Act;
import timeflow.data.db.Field;
import timeflow.model.VirtualField;
import timeflow.util.DoubleBag;

public class GroupVisualAct extends VisualAct
{
    /**
     * The resources.
     */
    private static final ResourceBundle bundle = ResourceBundle.getBundle("timeflow/vis/Bundle");
    int numActs = 0;
    double total = 0;
    private ArrayList<Act> group = new ArrayList<Act>();
    private boolean mixed = false;
    private DoubleBag<Color> colorBag;

    public GroupVisualAct(java.util.List<VisualAct> vacts, boolean mixed, Rectangle bounds)
    {
        super(vacts.get(0).act);
        int n = vacts.size();

        VisualAct proto = vacts.get(0);

        this.color = proto.color;
        this.trackString = proto.trackString;
        this.visible = proto.visible;
        this.x = proto.x;
        this.y = proto.y;

        this.spaceToRight = proto.spaceToRight;
        this.start = proto.start;
        this.group = new ArrayList<Act>();
        this.label = MessageFormat.format(bundle.getString("GroupVisualAct.label"), n);
        this.mouseOver = this.label;
        this.colorBag = new DoubleBag<Color>();
        Field sizeField = act.getDB().getField(VirtualField.SIZE);
        for (VisualAct v : vacts)
        {
            numActs++;
            if (sizeField != null)
            {
                total += v.act.getValue(sizeField);
            }
            this.size += v.size;
            this.colorBag.add(v.color, v.size);
        }
        this.size = Math.sqrt(this.size);
        this.mixed = mixed;
    }

    public int getNumActs()
    {
        return numActs;
    }

    public void add(Act secondAct)
    {
        if (group == null)
        {
            group = new ArrayList<Act>();
            if (act != null)
            {
                group.add(act);
            }
        }
        group.add(secondAct);
    }

    public void draw(Graphics2D g, int ox, int oy, int r, Rectangle maxFill, boolean showDuration)
    {
        if (!mixed)
        {
            g.setColor(color);
            g.fillOval(ox, oy - r, 2 * r, 2 * r);
            g.drawOval(ox - 2, oy - r - 2, 2 * r + 3, 2 * r + 3);
        }
        else
        {
            java.util.List<Color> colors = colorBag.listTop(8, true);
            double total = 0;
            for (Color c : colors)
            {
                total += colorBag.num(c);
            }

            // now draw pie chart thing.
            double angle = 0;
            int pieCenterX = ox + r;
            int pieCenterY = oy;
            for (Color c : colors)
            {
                double num = colorBag.num(c);
                double sa = (360 * angle) / total;
                int startAngle = (int) (sa);
                int arcAngle = (int) (((360 * (angle + num))) / total - sa);
                g.setColor(c);
                g.fillArc(pieCenterX - r, pieCenterY - r, 2 * r, 2 * r, startAngle, arcAngle);
                angle += num;
            }
        }
    }
}
