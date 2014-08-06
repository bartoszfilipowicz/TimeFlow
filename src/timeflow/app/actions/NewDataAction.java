package timeflow.app.actions;

import java.awt.event.ActionEvent;

import timeflow.app.TimeflowApp;
import timeflow.data.db.BasicDB;

public class NewDataAction extends TimeflowAction
{

    public NewDataAction(TimeflowApp app)
    {
        super(app, "New", null, "Create a new, blank database");
        accelerate('N');

    }

    public void actionPerformed(ActionEvent e)
    {
        if (app.checkSaveStatus())
        {
            getModel().setDB(new BasicDB("Unspecified"), "[new data]", true, this);
        }
    }
}
