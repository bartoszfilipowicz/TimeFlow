package timeflow.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import timeflow.data.db.ActDB;
import timeflow.model.ModelPanel;
import timeflow.model.TFEvent;
import timeflow.model.TFModel;

// superclass of all timeline views
public abstract class AbstractView extends ModelPanel
{
    protected boolean ignoreEventsWhenInvisible = true;
    JPanel panel;
    ActDB lastDrawn, lastNotified;

    public AbstractView(TFModel model)
    {
        super(model);
    }

    public void paintComponent(Graphics g)
    {
        g.drawString(getName(), 10, 30);
    }

    public final JComponent getControls()
    {
        if (panel != null)
        {
            return panel;
        }

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(_getControls(), BorderLayout.CENTER);
        JLabel controlLabel = new JLabel(" " + getName() + " Controls")
        {
            public Dimension getPreferredSize()
            {
                return new Dimension(30, 30);
            }
        };
        controlLabel.setBackground(Color.lightGray);
        controlLabel.setForeground(Color.darkGray);
        panel.add(controlLabel, BorderLayout.NORTH);

        return panel;
    }

    protected JComponent _getControls()
    {
        return new JLabel("local: " + getName());
    }

    public abstract String getName();

    protected abstract void _note(TFEvent e);

    protected abstract void onscreen(boolean majorChangeHappened);


    @Override
    public final void note(TFEvent e)
    {
        lastNotified = getModel().getDB();
        if (isVisible() || !ignoreEventsWhenInvisible)
        {
            _note(e);
            lastDrawn = lastNotified;
        }
    }

    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        if (visible && getModel().getDB() != null)
        {
            onscreen(lastDrawn != lastNotified);
            lastDrawn = lastNotified;
        }
    }
}
