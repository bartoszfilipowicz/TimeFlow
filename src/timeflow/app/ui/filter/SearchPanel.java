package timeflow.app.ui.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import timeflow.model.ModelPanel;
import timeflow.model.TFEvent;
import timeflow.model.TFModel;

public class SearchPanel extends ModelPanel
{

    JTextField entry;
    JCheckBox invert;

    public SearchPanel(TFModel model, final FilterControlPanel f)
    {
        super(model);
        setBackground(Color.white);
        setBorder(BorderFactory.createEmptyBorder(15, 5, 0, 0));
        setLayout(new GridLayout(1, 1));
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        add(top);
        top.setBackground(Color.white);
        JLabel label = model.getDisplay().label("Search");
        top.add(label, BorderLayout.WEST);
        entry = new JTextField(8);
        top.add(entry, BorderLayout.CENTER);
        entry.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                f.makeFilter();
            }
        });

        invert = new JCheckBox("Invert", false);
        top.add(invert, BorderLayout.EAST);
        invert.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                f.setInverted(invert.isSelected());
            }
        });
        invert.setFont(f.getModel().getDisplay().small());
        invert.setForeground(Color.gray);
        invert.setBackground(Color.white);
    }

    @Override
    public void note(TFEvent e)
    {
    }

}
