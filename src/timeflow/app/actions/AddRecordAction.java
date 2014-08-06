package timeflow.app.actions;

import java.awt.event.ActionEvent;

import timeflow.app.TimeflowApp;
import timeflow.app.ui.EditRecordPanel;

public class AddRecordAction extends TimeflowAction
{

    public AddRecordAction(TimeflowApp app)
    {
        super(app, "Add Record...", null, "Add a record to this database");
        accelerate('A');
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        EditRecordPanel.add(getModel());
    }
}
