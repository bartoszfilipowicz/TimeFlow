package timeflow.vis.calendar;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collection;

import javax.swing.JComponent;

import timeflow.data.db.DBUtils;
import timeflow.data.time.Interval;
import timeflow.data.time.TimeUnit;
import timeflow.model.TFEvent;
import timeflow.model.TFModel;
import timeflow.model.VirtualField;
import timeflow.vis.Mouseover;
import timeflow.vis.VisualEncoder;

public class CalendarVisuals
{
    public Grid grid;
    VisualEncoder encoder;
    TFModel model;
    Rectangle bounds = new Rectangle();
    DrawStyle drawStyle = DrawStyle.ICON;
    FitStyle fitStyle = FitStyle.LOOSE;
    private Layout layoutStyle = Layout.DAY;

    public CalendarVisuals(TFModel model)
    {
        this.model = model;
        encoder = new VisualEncoder(model);
    }

    public void render(JComponent component, Graphics2D g, Collection<Mouseover> objectLocations, boolean leftToRight)
    {
        grid.render(component, g, model.getDisplay(), bounds, this, objectLocations, leftToRight);
    }

    public Interval getGlobalInterval()
    {
        return DBUtils.range(model.getDB().all(), VirtualField.START);
    }

    public Rectangle getBounds()
    {
        return bounds;
    }

    public void setBounds(int x, int y, int w, int h)
    {
        bounds.setBounds(x, y, w, h);
        if (grid == null)
        {
            makeGrid(true);
        }
    }

    public void setDrawStyle(DrawStyle drawStyle)
    {
        this.drawStyle = drawStyle;
    }

    public void setLayoutStyle(Layout layoutStyle)
    {
        this.layoutStyle = layoutStyle;
        makeGrid(true);
    }

    public void setFitStyle(FitStyle fitStyle)
    {
        this.fitStyle = fitStyle;
        makeGrid(true);
    }

    public void makeGrid(boolean fresh)
    {
        Interval interval = getGlobalInterval();
        int dy = 0;
        if (grid != null)
        {
            dy = grid.dy;
        }
        switch (layoutStyle)
        {
            case DAY:
                grid = new Grid(TimeUnit.WEEK, TimeUnit.DAY_OF_WEEK, interval);
                break;
            case WEEK:
                grid = new Grid(TimeUnit.multipleWeeks(8), TimeUnit.WEEK, interval);
                break;
            case MONTH:
                grid = new Grid(TimeUnit.YEAR, TimeUnit.MONTH, interval);
                break;
            case YEAR:
                grid = new Grid(TimeUnit.DECADE, TimeUnit.YEAR, interval);
        }
        grid.makeCells(encoder.getVisualActs());
        if (!fresh)
        {
            grid.dy = dy;
        }
    }

    public void init()
    {
        note(new TFEvent(TFEvent.Type.DATABASE_CHANGE, null));
    }

    public void initAllButGrid()
    {
        encoder.createVisualActs();
        encoder.apply();
    }

    public void note(TFEvent e)
    {
        if (e.affectsRowSet())
        {
            encoder.createVisualActs();
        }

        updateVisuals(e.affectsRowSet());
    }

    public void updateVisuals(boolean fresh)
    {
        encoder.apply();
        makeGrid(fresh);
    }

    public enum Layout
    {
        DAY, WEEK, MONTH, YEAR
    }

    public enum DrawStyle
    {
        LABEL, ICON
    }

    public enum FitStyle
    {
        LOOSE, TIGHT
    }
}
