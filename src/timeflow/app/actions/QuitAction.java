package timeflow.app.actions;

import java.awt.event.ActionEvent;

import timeflow.app.TimeflowApp;
import timeflow.model.TFModel;

public class QuitAction extends TimeflowAction
{

    public QuitAction(TimeflowApp app, TFModel model)
    {
        super(app, "Quit", null, "Quit the program");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        quit();
    }

    public void quit()
    {
        if (app.checkSaveStatus())
        {
            System.exit(0);
        }
    }
}
