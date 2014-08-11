package timeflow.vis.timeline;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import timeflow.data.db.Act;
import timeflow.data.db.ActDB;
import timeflow.data.db.ActList;
import timeflow.data.db.DBUtils;
import timeflow.data.db.Field;
import timeflow.data.db.filter.TimeIntervalFilter;
import timeflow.data.time.Interval;
import timeflow.model.TFEvent;
import timeflow.model.TFModel;
import timeflow.model.VirtualField;
import timeflow.vis.TimeScale;
import timeflow.vis.VisualAct;
import timeflow.vis.VisualEncoder;

/*
 * A VisualEncoding takes the info about which fields to translate to
 * which visual aspects, and applies that to particular Acts.
 */
public class TimelineVisuals
{
    ArrayList<TimelineTrack> trackList = new ArrayList<TimelineTrack>();
    private TimeScale timeScale = new TimeScale();
    private Rectangle bounds = new Rectangle();
    private boolean frameChanged;
    private int numShown = 0;

    private Interval globalInterval;
    private Layout layoutStyle = Layout.LOOSE;

    private VisualEncoder encoder;
    private TFModel model;
    private boolean leftToRight;
    private int fullHeight;

    public TimelineVisuals(TFModel model, boolean leftToRight)
    {
        this.model = model;
        this.leftToRight = leftToRight;
        encoder = new VisualEncoder(model);
    }

    public int getFullHeight()
    {
        return fullHeight;
    }

    public TimeScale getTimeScale()
    {
        return timeScale;
    }

    public Rectangle getBounds()
    {
        return bounds;
    }

    public void setBounds(int x, int y, int w, int h)
    {
        bounds.setBounds(x, y, w, h);
        timeScale.setLow(x);
        timeScale.setHigh(x + w);
        frameChanged = true;
    }

    public Layout getLayoutStyle()
    {
        return layoutStyle;
    }

    public void setLayoutStyle(Layout style)
    {
        layoutStyle = style;
        layout();
    }

    public Interval getFitToVisibleRange()
    {
        ActList acts = model.getActs();

        // add a little bit to the right so we can see labels...
        ActDB db = getModel().getDB();
        Field endField = db.getField(VirtualField.END);
        Interval i;
        if (endField == null)
        {
            i = DBUtils.range(acts, VirtualField.START);
        }
        else
        {
            i = DBUtils.range(acts, new Field[] { db.getField(VirtualField.START), endField });
        }

        if (i.length() == 0)
        {
            i = i.expand(globalInterval.length() / 20);
        }

        i = i.subinterval(-.05, 1.1);
        i.intersection(globalInterval);
        return i;
    }

    public void fitToVisible()
    {
        Interval i = getFitToVisibleRange();
        setTimeBounds(i.start, i.end);
    }

    public void zoomOut()
    {
        setTimeBounds(globalInterval.start, globalInterval.end);
    }

    public void setTimeBounds(long first, long last)
    {
        timeScale.setDateRange(first, last);
        frameChanged = true;
        model.setViewInterval(new Interval(first, last));
    }

    public Interval getGlobalInterval()
    {
        if (globalInterval == null && model != null && model.getDB() != null)
        {
            createGlobalInterval();
        }
        return globalInterval;
    }

    public void createGlobalInterval()
    {
        globalInterval = DBUtils.range(model.getDB().all(), VirtualField.START).subinterval(-.05, 1.1);
    }

    public Interval getViewInterval()
    {
        return timeScale.getInterval();
    }

    public void setViewInterval(Interval interval)
    {
        timeScale.setInterval(interval);
    }

    public java.util.List<VisualAct> getVisualActs()
    {
        return encoder.getVisualActs();
    }

    public void layoutIfChanged()
    {
        if (frameChanged)
        {
            layout();
        }
    }

    public void init(boolean majorChange)
    {
        note(new TFEvent(majorChange ? TFEvent.Type.DATABASE_CHANGE : TFEvent.Type.ACT_CHANGE, null));
    }

    public void note(TFEvent e)
    {
        ActList all;
        if (e.type == TFEvent.Type.DATABASE_CHANGE)
        {
            all = model.getDB().all();
            createGlobalInterval();
            Interval i = guessInitialViewInterval(all, globalInterval);
            setTimeBounds(i.start, i.end);
        }
        if (e.affectsRowSet())
        {
            all = model.getDB().all();
            encoder.createVisualActs();
            createGlobalInterval();
        }
        else
        {
            encoder.createVisualActs();
        }
        Interval v = model.getViewInterval();
        if (v != null && v.start != timeScale.getInterval().start)
        {
            timeScale.setInterval(timeScale.getInterval().translateTo(v.start));
        }
        updateVisuals();
    }

    // TODO: performance
    private Interval guessInitialViewInterval(ActList acts, Interval fullRange)
    {
        if (acts.size() < 50)
        {
            return fullRange.copy();
        }

        Interval best = null;
        int most = -1;
        double d = Math.max(.1, 50. / acts.size());
        d = Math.min(1. / 3, d);
        for (double x = 0; x < 1 - d; x += d / 4)
        {
            Interval i = fullRange.subinterval(x, x + d);
            TimeIntervalFilter f = new TimeIntervalFilter(i, getModel().getDB().getField(VirtualField.START));
            int num = 0;
            for (Act a : acts)
            {
                if (f.accept(a))
                {
                    num++;
                }
            }
            if (num > most)
            {
                most = num;
                best = i;
            }
        }
        return best;
    }

    public void updateVisuals()
    {
        updateVisualEncoding();
        layout();
    }

    public TFModel getModel()
    {
        return model;
    }

    public int getNumTracks()
    {
        return trackList.size();
    }

    public void layout()
    {
        ActList acts = model.getActs();
        if (acts == null)
        {
            return;
        }

        double min = bounds.height == 0 ? 0 : 30. / bounds.height;
        double top = 0;
        for (TimelineTrack t : trackList)
        {
            double height = Math.max(min, t.size() / (double) numShown);
            t.layout(top, height, this, leftToRight);
            top += height;
        }
        fullHeight = (int) (top * bounds.height);

        Collections.sort(trackList);
        frameChanged = false;
    }

    private void updateVisualEncoding()
    {
        java.util.List<VisualAct> acts = encoder.apply();

        // now arrange on tracks
        ConcurrentMap<String, TimelineTrack> trackTable = new ConcurrentHashMap<>();
        final AtomicInteger visibleCount = new AtomicInteger();

        acts.stream()
            .parallel()
            .filter(VisualAct::isVisible)
            .forEach(visualAct -> {
                visibleCount.incrementAndGet();

                String trackName = visualAct.getTrackString();
                trackTable.putIfAbsent(trackName, new TimelineTrack(trackName));
                TimelineTrack track = trackTable.get(trackName);

                visualAct.setTrack(track);
                track.add(visualAct);
            });

        numShown = visibleCount.get();
        trackList = new ArrayList<>(trackTable.values());
    }

    /**
     * Sets whether the timeline is in LTR or RTL mode.
     *
     * @param leftToRight {@code true} if in lef-to-right mode.
     */
    public void setLeftToRight(boolean leftToRight)
    {
        this.leftToRight = leftToRight;
    }

    public enum Layout
    {
        TIGHT, LOOSE, GRAPH
    }
}

