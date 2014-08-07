package timeflow.vis;


/**
 * A mouse over for a visual act.
 */
public class VisualActMouseover extends Mouseover
{
    public final VisualAct visualAct;

    public VisualActMouseover(VisualAct visualAct, int x, int y, int w, int h)
    {
        super(visualAct, x, y, w, h);
        this.visualAct = visualAct;
    }
}
