package timeflow.app.actions;

import java.awt.event.ActionEvent;

import timeflow.app.TimeflowApp;
import timeflow.model.Display;

public class WebDocAction extends TimeflowAction
{
    public WebDocAction(TimeflowApp app)
    {
        super(app, "Documentation & License Info...", null, "Read web documentation.");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Display.launchBrowser("http://wiki.github.com/FlowingMedia/TimeFlow/");
    }

}
