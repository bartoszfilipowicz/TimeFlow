package timeflow.app.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import timeflow.data.db.Field;
import timeflow.model.ModelPanel;
import timeflow.model.TFEvent;
import timeflow.model.TFModel;
import timeflow.views.TableView;

public class ReorderFieldsPanel extends ModelPanel
{
    TableView table;

    public ReorderFieldsPanel(TFModel model)
    {
        super(model);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new GridLayout(3, 1));
        top.add(new JPanel());
        JLabel instructions = new JLabel("Drag and drop the column headers to reorder fields.");
        top.add(instructions);
        top.add(new JPanel());
        add(top, BorderLayout.NORTH);

        table = new TableView(model);
        add(table, BorderLayout.CENTER);
        table.setEditable(false);
        table.setReorderable(true);
        table.onscreen(true);
    }

    public void applyReordering()
    {
        Enumeration<TableColumn> columns = table.getTable().getTableHeader().getColumnModel().getColumns();
        ArrayList<Field> newOrder = new ArrayList<Field>();
        while (columns.hasMoreElements())
        {
            TableColumn col = columns.nextElement();
            String name = col.getHeaderValue().toString();
            newOrder.add(getModel().getDB().getField(name));
        }
        getModel().getDB().setNewFieldOrder(newOrder);
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
