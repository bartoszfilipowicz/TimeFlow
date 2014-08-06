package timeflow.vis;


import java.awt.Graphics2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

import timeflow.data.db.Act;
import timeflow.data.db.ActDB;
import timeflow.data.db.Field;
import timeflow.model.Display;
import timeflow.model.VirtualField;

/**
 * A mouse over for a visual act.
 */
public class VisualActMouseover extends Mouseover
{
    /**
     * The resources.
     */
    private static final ResourceBundle bundle = ResourceBundle.getBundle("timeflow/vis/Bundle");

    private VisualAct visualAct;

    public VisualActMouseover(VisualAct visualAct, int x, int y, int w, int h)
    {
        super(visualAct, x, y, w, h);
        this.visualAct = visualAct;
    }

    @Override
    public void draw(Graphics2D g, int maxW, int maxH, Display display)
    {
        super.draw(g, maxW, maxH, display);
        Act a = visualAct.getAct();
        ActDB db = a.getDB();
        java.util.List<Field> fields = db.getFields();
        ArrayList labels = new ArrayList();
        int charWidth = 40;
        int numLines = 1;
        if (visualAct instanceof GroupVisualAct)
        {
            GroupVisualAct gv = (GroupVisualAct) visualAct;
            labels.add(MessageFormat.format(bundle.getString("GroupVisualAct.itemCount"), gv.getNumActs()));
            Field sizeField = db.getField(VirtualField.SIZE);
            if (sizeField != null)
            {
                labels.add(MessageFormat.format(bundle.getString("GroupVisualAct.totalLabel"), sizeField.getName()));
                double t = ((GroupVisualAct) (visualAct)).total;
                labels.add(Display.format(t));
                numLines++;
            }
        }
        else
        {
            for (Field f : fields)
            {
                labels.add(f.getName());
                Object val = a.get(f);
                String valString = display.toString(val);
                if (f.getName().length() + valString.length() + 2 > charWidth)
                {
                    ArrayList<String> lines = Display.breakLines(valString, charWidth, 2 + f.getName().length());
                    labels.add(lines);
                    numLines += lines.size() + 1;
                }
                else
                {
                    labels.add(valString);
                    numLines++;
                }
            }
        }
        draw(g, maxW, maxH, display, labels, numLines);
    }
}
