package timeflow.app.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JPanel;

import timeflow.data.db.Act;
import timeflow.data.db.ActDB;
import timeflow.data.db.ActList;
import timeflow.model.ModelPanel;
import timeflow.model.TFEvent;
import timeflow.model.TFModel;
import timeflow.views.TableView;

public class MassDeletePanel extends ModelPanel
{
    TableView table;
    ActList keepers;

    public MassDeletePanel(TFModel model, ActList keepers, String title)
    {
        super(model);
        this.keepers = keepers;
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new GridLayout(4, 1));
        top.add(new JPanel());
        top.add(new JLabel(title));
        int n = keepers.size();
        String message = null;
        if (n > 1)
        {
            message = "These are the " + n + " items that will remain.";
        }
        else if (n == 1)
        {
            message = "This in the only item that will remain.";
        }
        else
        {
            message = "No items will remain!";
        }

        JLabel instructions = new JLabel(message);
        top.add(instructions);
        top.add(new JPanel());
        add(top, BorderLayout.NORTH);

        table = new TableView(model);
        model.removeListener(table);
        add(table, BorderLayout.CENTER);
        table.setEditable(false);
        table.setActs(keepers);
    }

    public void applyAction()
    {
        ActDB db = getModel().getDB();
        HashSet<Act> keepSet = new HashSet<Act>();
        keepSet.addAll(keepers);

        for (Act a : db.all())
        {
            if (!keepSet.contains(a))
            {
                db.delete(a);
            }
        }

        // we assume the caller will decide what event to fire.
    }

    public void detachFromModel()
    {
        TFModel model = getModel();
        model.removeListener(table);
        model.removeListener(this);
    }

    public Dimension getPreferredSize()
    {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(700, d.width), 250);
    }

    @Override
    public void note(TFEvent e)
    {
        // TODO Auto-generated method stub

    }
}
