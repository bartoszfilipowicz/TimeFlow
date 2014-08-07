package timeflow.vis;

public class MouseoverLabel extends Mouseover
{
    public final String label1, label2;

    public MouseoverLabel(String label1, String label2, int x, int y, int w, int h)
    {
        super(label1, x, y, w, h);
        this.label1 = label1;
        this.label2 = label2;
    }
}

