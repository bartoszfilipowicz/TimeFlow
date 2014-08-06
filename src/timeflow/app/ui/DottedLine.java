package timeflow.app.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class DottedLine extends JPanel
{
    public void paintComponent(Graphics g)
    {
        int w = getSize().width, h = getSize().height;
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.lightGray);
        if (w > h)
        {
            for (int x = 0; x < w; x += 4)
            {
                g.drawLine(x, 0, x + 1, 0);
            }
        }
        else
        {
            for (int y = 0; y < h; y += 4)
            {
                g.drawLine(0, y, 0, y + 1);
            }
        }
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(1, 1);
    }
}
