package timeflow.vis.calendar;


import java.awt.Rectangle;
import java.util.ArrayList;

import timeflow.data.time.RoughTime;
import timeflow.vis.VisualAct;

public class GridCell
{
    ArrayList<VisualAct> visualActs = new ArrayList<VisualAct>();
    Rectangle bounds;
    RoughTime time;
    int gridX, gridY;

    GridCell(RoughTime time)
    {
        this.time = time;
    }
}
