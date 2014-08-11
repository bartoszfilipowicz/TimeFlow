package timeflow.vis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import timeflow.data.db.Act;
import timeflow.data.db.ActDB;
import timeflow.data.db.ActList;
import timeflow.data.db.DBUtils;
import timeflow.data.db.Field;
import timeflow.model.Display;
import timeflow.model.TFModel;
import timeflow.model.VirtualField;

import static java.util.stream.Collectors.*;

public final class VisualEncoder
{

    private TFModel model;
    private Collection<VisualAct> visualActs = new ArrayList<VisualAct>();
    private double maxSize = 0;
    private Field label;
    private Field track;
    private Field color;
    private Field size;

    public VisualEncoder(TFModel model)
    {
        this.model = model;
    }

    public Collection<VisualAct> getVisualActs()
    {
        return visualActs;
    }

    public void createVisualActs()
    {
        Field colorField = model.getDB().getField(VirtualField.COLOR);
        Field trackField = model.getDB().getField(VirtualField.TRACK);
        boolean multipleColors = colorField != null && colorField.getType() == String[].class && colorField != trackField;
        visualActs = VisualActFactory.create(model.getDB().all(), trackField, multipleColors);
        Collections.sort((List<VisualAct>)visualActs);
    }

    public List<VisualAct> apply()
    {
        ActDB db = model.getDB();
        label = db.getField(VirtualField.LABEL);
        track = db.getField(VirtualField.TRACK);
        color = db.getField(VirtualField.COLOR);
        size = db.getField(VirtualField.SIZE);

        ActList modelActs = model.getActs();
        Field start = model.getDB().getField(VirtualField.START);
        Field end = model.getDB().getField(VirtualField.END);
        Field size = model.getDB().getField(VirtualField.SIZE);
        if (size != null)
        {
            double[] minmax = DBUtils.minmax(model.getActs(), size);
            maxSize = Math.max(Math.abs(minmax[0]), Math.abs(minmax[1]));
        }

        // Apply color, label, visibility, etc. Collect the visible items to a sorted list
        return visualActs
            .parallelStream()
            .filter(visualAct -> {
                Act a = visualAct.getAct();
                visualAct.setStart(start == null ? null : a.getTime(start));
                visualAct.setEnd(end == null ? null : a.getTime(end));
                if (modelActs.contains(a) && visualAct.getStart() != null && visualAct.getStart().isDefined())
                {
                    visualAct.setVisible(true);
                }
                else
                {
                    visualAct.setVisible(false);
                }
                apply(visualAct); // Required even if hidden for the bottom slider

                return visualAct.isVisible();
            })
            .sorted()
            .collect(toList());
    }

    private void apply(VisualAct v)
    {
        Display display = model.getDisplay();
        Act a = v.getAct();

        if (label == null)
        {
            v.setLabel("");
        }
        else
        {
            v.setLabel(a.getString(label));
        }

        double s = Display.MAX_DOT_SIZE;
        if (size == null || maxSize == 0)
        {
            v.setSize(s / 3);
        }
        else
        {
            double z = s * Math.sqrt(Math.abs(a.getValue(size)) / maxSize);
            if (a.getValue(size) < 0)
            {
                z = -z;
            }
            v.setSize(z);
        }

        // For setting the track:
        // This is a little complicated, but if the track is set to
        // tags (that is, track.getType()==String[].class) then
        // the track string was set earlier so it doesn't need to be set now.
        if (track == null)
        {
            v.setTrackString("");
        }
        else if (track.getType() == String.class)
        {
            v.setTrackString(a.getString(track));
        }

        if (color == null || color == track)
        {
            if (track == null)
            {
                v.setColor(display.getColor("timeline.unspecified.color"));
            }
            else
            {
                v.setColor(display.makeColor(v.getTrackString()));
            }
        }
        else
        {
            if (color.getType() == String[].class)
            {
                String[] tags = a.getTextList(color);
                if (tags == null || tags.length == 0)
                {
                    ((TagVisualAct) v).setColors(new Color[0]);
                }
                else
                {
                    int n = tags.length;
                    Color[] c = new Color[n];
                    for (int i = 0; i < n; i++)
                    {
                        c[i] = display.makeColor(tags[i]);
                    }
                    ((TagVisualAct) v).setColors(c);
                }
            }
            else
            {
                v.setColor(display.makeColor(a.getString(color)));
            }
        }
    }
}