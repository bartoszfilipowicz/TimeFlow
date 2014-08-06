package timeflow.app.actions;

import java.awt.Toolkit;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import timeflow.app.TimeflowApp;
import timeflow.model.TFModel;

public abstract class TimeflowAction extends AbstractAction
{
    TimeflowApp app;

    public TimeflowAction(TimeflowApp app, String text, ImageIcon icon, String desc)
    {
        super(text, icon);
        this.app = app;
        putValue(SHORT_DESCRIPTION, desc);
    }


    protected void accelerate(char c)
    {
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(c,
                                                                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
    }


    protected TFModel getModel()
    {
        return app.model;
    }


}
