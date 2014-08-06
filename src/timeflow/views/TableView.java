package timeflow.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import timeflow.data.db.Act;
import timeflow.data.db.ActList;
import timeflow.data.db.Field;
import timeflow.data.time.RoughTime;
import timeflow.format.field.DateTimeGuesser;
import timeflow.model.TFEvent;
import timeflow.model.TFModel;
import timeflow.model.VirtualField;

public class TableView extends AbstractView
{

    private JTable table = new JTable();
    private int colorColumn = -1, labelColumn = -1;
    private Font font, bold;
    private boolean editable = true;
    private JPanel controls;

    public TableView(TFModel model)
    {
        super(model);

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        font = model.getDisplay().plain();
        bold = model.getDisplay().bold();
        table.setFont(font);
        final int fh = getFontMetrics(font).getHeight();
        table.setRowHeight(fh + 12);
        table.setShowGrid(false);
        table.getTableHeader().setPreferredSize(new Dimension(10, fh + 15));
        table.getTableHeader().setFont(font);
        setReorderable(false);
        setLayout(new GridLayout(1, 1));
        add(scrollPane);

        controls = new HtmlControls("Use the table view for<br> a rapid overview<br> " +
                                    "of your data. <p>You can click<br> on the headers to sort the columns,<br> " +
                                    "and you can edit data<br> directly in the table cells.");
    }

    @Override
    public JComponent _getControls()
    {
        return controls;
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }

    public JTable getTable()
    {
        return table;
    }

    @Override
    public void onscreen(boolean majorChange)
    {
        _note(null);
    }

    @Override
    protected void _note(TFEvent e)
    {
        setActs(getModel().getActs());
    }

    public void setActs(ActList acts)
    {
        TableModel t = new TimelineTableModel(acts);
        table.setModel(t);
        ActTableRenderer r = new ActTableRenderer(acts);
        table.setDefaultRenderer(Object.class, r);
        table.setDefaultRenderer(Double.class, r);
        table.setDefaultEditor(String[].class, new StringArrayEditor());
        table.setDefaultEditor(RoughTime.class, new RoughTimeEditor());
        repaint();
    }

    @Override
    public String getName()
    {
        return "Table";
    }

    public void setReorderable(boolean allow)
    {
        table.getTableHeader().setReorderingAllowed(allow);
    }

    class ActTableRenderer extends DefaultTableCellRenderer
    {

        ActList acts;
        Color zebra = new Color(240, 240, 240);
        boolean color = false;
        Color dataColor;

        ActTableRenderer(ActList acts)
        {
            this.acts = acts;
        }

        public void setValue(Object value)
        {
            if (value == null)
            {
                super.setValue("");
                return;
            }
            setHorizontalAlignment(value instanceof Double ? SwingConstants.RIGHT : SwingConstants.LEFT);
            super.setValue(getModel().getDisplay().toString(value));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex)
        {

            if (vColIndex == labelColumn || vColIndex == colorColumn)
            {
                setFont(bold);
            }
            else
            {
                setFont(font);
            }
            setBackground(rowIndex % 2 == 0 ? Color.white : zebra);
            color = vColIndex == colorColumn || vColIndex == labelColumn;
            Field colorField = null;
            if (color)
            {
                colorField = getModel().getColorField();
                color &= colorField != null;
            }
            if (color)
            {
                int actIndex = table.convertRowIndexToModel(rowIndex);
                Act act = acts.get(actIndex);

                if (colorField == null || colorField.getType() != String.class)
                {
                    dataColor = getModel().getDisplay().getColor("timeline.unspecified.color");
                }
                else
                {
                    dataColor = getModel().getDisplay().makeColor(act.getString(colorField));
                }
                setForeground(dataColor);
                setValue(value);
            }
            else
            {
                setForeground(Color.black);
                setValue(value);
            }
            return this;
        }
    }

    class TimelineTableModel implements TableModel
    {
        ActList acts;
        Field[] fields;

        TimelineTableModel(ActList acts)
        {
            this.acts = acts;
            ArrayList<Field> a = new ArrayList<Field>();
            int i = 0;
            Field colorField = getModel().getColorField();
            Field labelField = getModel().getDB().getField(VirtualField.LABEL);
            for (Field f : acts.getDB().getFields())
            {
                a.add(f);
                if (f == colorField)
                {
                    colorColumn = i;
                }
                if (f == labelField)
                {
                    labelColumn = i;
                }
                i++;
            }
            fields = (Field[]) a.toArray(new Field[0]);
        }

        @Override
        public void addTableModelListener(TableModelListener l)
        {
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return fields[columnIndex].getType();
        }

        @Override
        public int getColumnCount()
        {
            return fields.length;
        }

        @Override
        public String getColumnName(int columnIndex)
        {
            return fields[columnIndex].getName();
        }

        @Override
        public int getRowCount()
        {
            return acts.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return acts.get(rowIndex).get(fields[columnIndex]);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return editable;
        }

        @Override
        public void removeTableModelListener(TableModelListener l)
        {
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            acts.get(rowIndex).set(fields[columnIndex], aValue);
            getModel().noteRecordChange(TableView.this);
        }
    }

    public class StringArrayEditor extends AbstractCellEditor implements TableCellEditor
    {
        JComponent component = new JTextField();

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int rowIndex, int vColIndex)
        {
            ((JTextField) component).setText(getModel().getDisplay().toString(value));
            return component;
        }

        public Object getCellEditorValue()
        {
            String s = ((JTextField) component).getText();
            String[] tags = s.split(",");
            for (int i = 0; i < tags.length; i++)
            {
                tags[i] = tags[i].trim();
            }
            return tags;
        }
    }

    public class RoughTimeEditor extends AbstractCellEditor implements TableCellEditor
    {
        JComponent component = new JTextField();
        DateTimeGuesser guesser = new DateTimeGuesser();

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int rowIndex, int vColIndex)
        {

            RoughTime r = (RoughTime) value;
            JTextField t = ((JTextField) component);
            t.setText(r == null ? "" : r.format());
            return component;
        }

        public Object getCellEditorValue()
        {
            String s = ((JTextField) component).getText();
            return s.trim().length() == 0 ? null : guesser.guess(s);
        }
    }

}
