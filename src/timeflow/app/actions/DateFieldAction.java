package timeflow.app.actions;

import java.awt.event.ActionEvent;

import timeflow.app.TimeflowApp;
import timeflow.app.ui.DateFieldPanel;

public class DateFieldAction extends TimeflowAction
{

    public DateFieldAction(TimeflowApp app)
    {
        super(app, "Set Date Fields...", null, "Set date fields corresponding to start, end.");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        DateFieldPanel.popWindow(app.model);
    }
}
