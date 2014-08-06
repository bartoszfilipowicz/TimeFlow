package timeflow.app.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import timeflow.app.TimeflowApp;
import timeflow.app.ui.MassDeletePanel;


public class DeleteUnselectedAction extends TimeflowAction
{

    public DeleteUnselectedAction(TimeflowApp app)
    {
        super(app, "Delete Unselected Items...", null, "Delete all but the currently visible events");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        MassDeletePanel panel = new MassDeletePanel(getModel(), getModel().getActs(),
                                                    "Delete unselected items.");
        Object[] options = { "Cancel", "Proceed" };
        int n = JOptionPane.showOptionDialog(app,
                                             panel,
                                             "Delete Unselected",
                                             JOptionPane.YES_NO_CANCEL_OPTION,
                                             JOptionPane.PLAIN_MESSAGE,
                                             null,
                                             options,
                                             "Proceed");
        panel.detachFromModel();
        if (n == 1)
        {
            panel.applyAction();
            app.clearFilters();
            getModel().noteSchemaChange(this);
        }
    }

}
