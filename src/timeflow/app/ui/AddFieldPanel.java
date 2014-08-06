package timeflow.app.ui;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import timeflow.format.field.FieldFormatCatalog;


public class AddFieldPanel extends JPanel
{
    public JTextField name = new JTextField(12);
    public JComboBox typeChoices = new JComboBox();

    public AddFieldPanel()
    {
        for (String choice : FieldFormatCatalog.classNames())
        {
            typeChoices.addItem(choice);
        }
        setLayout(new GridLayout(2, 2));
        add(new JLabel("Field Name"));
        add(name);
        add(new JLabel("Field Type"));
        add(typeChoices);
    }
}
