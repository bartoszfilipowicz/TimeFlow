package timeflow.app.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import timeflow.app.TimeflowApp;
import timeflow.app.ui.ReorderFieldsPanel;


public class ReorderFieldsAction extends TimeflowAction
{

    public ReorderFieldsAction(TimeflowApp app)
    {
        super(app, "Reorder Fields...", null, "Edit the order of fields");
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        ReorderFieldsPanel panel = new ReorderFieldsPanel(getModel());
        Object[] options = { "Cancel", "Apply" };
        int n = JOptionPane.showOptionDialog(app,
                                             panel,
                                             "Reorder Fields",
                                             JOptionPane.YES_NO_CANCEL_OPTION,
                                             JOptionPane.PLAIN_MESSAGE,
                                             null,
                                             options,
                                             "Apply");
        panel.detachFromModel();
        if (n == 1)
        {
            panel.applyReordering();
            getModel().noteSchemaChange(this);
        }
    }

}
