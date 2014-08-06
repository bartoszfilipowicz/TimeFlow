package timeflow.app.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import timeflow.app.TimeflowApp;
import timeflow.model.TFModel;


public class EditSourceAction extends TimeflowAction
{

    public EditSourceAction(TimeflowApp app)
    {
        super(app, "Edit Source/Credit Line...", null, "Edit credit line for this database");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        TFModel model = getModel();
        String source = (String) JOptionPane.showInputDialog(
            app,
            null,
            "Edit Source/Credit Line",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            model.getDB().getSource());

        if (source != null)
        {
            model.getDB().setSource(source);
            model.noteNewSource(this);
            return;
        }
    }

}
