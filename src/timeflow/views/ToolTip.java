package timeflow.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.LayoutStyle;

import timeflow.app.ui.RoundedPane;
import timeflow.model.Display;

/**
 * A tool-tip which is rendered over the visualisation when the mouse hovers over a node.
 */
public class ToolTip extends RoundedPane
{
    public ToolTip()
    {
        setOpaque(true);
        setBackground(Color.white);
        getContentPanel().setBackground(Color.white);
    }

    /**
     * Sets the content for this tool tip.
     *
     * @param fieldValueMap the map of key/value pairs.
     * @param display the display settings.
     */
    public void setContent(Map<String, Object> fieldValueMap, Display display)
    {
        getContentPanel().removeAll();

        GroupLayout layout = new GroupLayout(getContentPanel());
        getContentPanel().setLayout(layout);

        List<JLabel> labels = new ArrayList<JLabel>();
        List<JComponent> values = new ArrayList<JComponent>();

        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();

        for (Map.Entry<String, Object> entry : fieldValueMap.entrySet())
        {
            JLabel label = new JLabel(entry.getKey());
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            labels.add(label);

            Object value = entry.getValue();
            JComponent valueComponent;
            if (value instanceof Image)
            {
                valueComponent = new JLabel(new ImageIcon((Image) value));
                values.add(valueComponent);
            }
            else
            {
                valueComponent = new JLabel(display.toString(value));
                values.add(valueComponent);
            }

            verticalGroup.addGroup(
                layout
                    .createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(label, GroupLayout.Alignment.LEADING)
                    .addComponent(valueComponent, GroupLayout.Alignment.TRAILING)
            );
        }

        GroupLayout.ParallelGroup labelGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup valueGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

        for (JLabel label : labels)
        {
            labelGroup.addComponent(label);
        }

        for (JComponent component : values)
        {
            valueGroup.addComponent(component);
        }

        GroupLayout.SequentialGroup horizontalGroup = layout.createSequentialGroup();
        horizontalGroup.addGroup(labelGroup).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(valueGroup);

        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);

        getContentPanel().doLayout();
        doLayout();
    }
}
