package timeflow.util;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

public class Pad extends JPanel
{
    Dimension pref;

    public Pad(int w, int h)
    {
        pref = new Dimension(w, h);
        setBackground(Color.white);
    }

    public Dimension getPreferredSize()
    {
        return pref;
    }
}
