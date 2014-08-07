package timeflow.views;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.ItemSelectable;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.CellRendererPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;

import timeflow.app.ui.EditRecordPanel;
import timeflow.data.db.Act;
import timeflow.data.db.ActDB;
import timeflow.data.db.Field;
import timeflow.data.time.RoughTime;
import timeflow.model.Display;
import timeflow.model.TFModel;
import timeflow.model.VirtualField;
import timeflow.vis.GroupVisualAct;
import timeflow.vis.Mouseover;
import timeflow.vis.MouseoverLabel;
import timeflow.vis.VisualAct;
import timeflow.vis.VisualActMouseover;

public abstract class AbstractVisualizationView extends JPanel implements ItemSelectable
{
    /**
     * The resources.
     */
    private static final ResourceBundle bundle = ResourceBundle.getBundle("timeflow/vis/Bundle");

    Image buffer;
    Graphics2D graphics;
    Point mouse = new Point(-10000, 0), firstMouse = new Point();
    boolean mouseIsDown;
    ArrayList<Mouseover> objectLocations = new ArrayList<Mouseover>();
    TFModel model;
    Act selectedAct;
    RoughTime selectedTime;
    Set<JMenuItem> urlItems = new HashSet<JMenuItem>();
    boolean allowPopupMenu = true;
    EventListenerList listenerList = new EventListenerList();

    /**
     * The cell render pane used to render the custom 'tool tip'.
     */
    protected final CellRendererPane renderPane;

    /**
     * The custom tool tip rendered next to the mouse when hovering over nodes.
     */
    protected final ToolTip toolTip = new ToolTip();

    public AbstractVisualizationView(TFModel model)
    {
        this.model = model;

        renderPane = new CellRendererPane();
        add(renderPane);

        // deal with mouseovers.
        addMouseMotionListener(new MouseMotionListener()
        {

            @Override
            public void mouseDragged(MouseEvent e)
            {
                mouse.setLocation(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {
                mouse.setLocation(e.getX(), e.getY());
                repaint();
            }
        });

        final JPopupMenu popup = new JPopupMenu();
        final JMenuItem edit = new JMenuItem("Edit");
        edit.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                EditRecordPanel.edit(getModel(), selectedAct);
            }
        });
        popup.add(edit);

        final JMenuItem delete = new JMenuItem("Delete");
        popup.add(delete);
        delete.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getModel().getDB().delete(selectedAct);
                getModel().noteDelete(this);
            }
        });

        final JMenuItem add = new JMenuItem("New...");
        popup.add(add);
        add.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                EditRecordPanel.add(getModel(), selectedTime);
            }
        });

        // deal with right-click.
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Point p = new Point(e.getX(), e.getY());
                Mouseover o = find(p);

                if (o != null && o.thing instanceof VisualAct)
                {
                    VisualAct v = (VisualAct) o.thing;
                    selectedAct = v.getAct();
                }
                else
                {
                    selectedAct = null;
                }

                notifyItemListeners();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                pop(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                pop(e);
            }

            private void pop(MouseEvent e)
            {
                if (e.isPopupTrigger() && allowPopupMenu)
                {
                    Point p = new Point(e.getX(), e.getY());
                    Mouseover o = find(p);
                    boolean onAct = o != null && o.thing instanceof VisualAct;
                    if (onAct)
                    {
                        VisualAct v = (VisualAct) o.thing;
                        selectedAct = v.getAct();
                        String name = " '" + v.getLabel() + "'";
                        edit.setText("Edit" + name + "...");
                        delete.setText("Delete" + name);
                        edit.setEnabled(true);
                        delete.setEnabled(true);
                    }
                    else
                    {
                        selectedAct = null;
                        edit.setEnabled(false);
                        edit.setText("Edit Event");
                        delete.setEnabled(false);
                        delete.setText("Delete Event");
                    }

                    notifyItemListeners();

                    selectedTime = getTime(p);
                    if (selectedTime != null || onAct)
                    {
                        add.setEnabled(selectedTime != null);
                        add.setText(selectedTime == null ? "Add" : "Add Event At " + selectedTime.format() + "...");

                        java.util.List<Field> urlFields = getModel().getDB().getFields(URL.class);
                        if (urlFields.size() > 0)
                        {
                            // remove any old items.
                            for (JMenuItem m : urlItems)
                            {
                                popup.remove(m);
                            }
                            urlItems.clear();

                            if (onAct)
                            {
                                Act a = ((VisualAct) o.thing).getAct();
                                for (Field f : urlFields)
                                {
                                    final URL url = a.getURL(f);
                                    JMenuItem go = new JMenuItem("Go to " + url);
                                    go.addActionListener(new ActionListener()
                                    {
                                        @Override
                                        public void actionPerformed(ActionEvent e)
                                        {
                                            Display.launchBrowser(url.toString());
                                        }
                                    });
                                    popup.add(go);
                                    urlItems.add(go);
                                }
                            }
                        }

                        popup.show(e.getComponent(), p.x, p.y);
                    }
                }
            }
        });
    }

    public boolean getAllowPopupMenu()
    {
        return allowPopupMenu;
    }

    public void setAllowPopupMenu(boolean allowPopupMenu)
    {
        this.allowPopupMenu = allowPopupMenu;
    }

    private void notifyItemListeners()
    {
        for (ItemListener listener : listenerList.getListeners(ItemListener.class))
        {
            int type = selectedAct == null ? ItemEvent.DESELECTED : ItemEvent.SELECTED;
            listener.itemStateChanged(new ItemEvent(this, 0, selectedAct, type));
        }
    }

    @Override
    public Object[] getSelectedObjects()
    {
        if (selectedAct == null)
        {
            return null;
        }
        else
        {
            return new Object[] { selectedAct };
        }
    }

    @Override
    public void addItemListener(ItemListener listener)
    {
        listenerList.add(ItemListener.class, listener);
    }

    @Override
    public void removeItemListener(ItemListener listener)
    {
        listenerList.remove(ItemListener.class, listener);
    }

    public RoughTime getTime(Point p)
    {
        return null;
    }

    public TFModel getModel()
    {
        return model;
    }

    @Override
    public void setBounds(int x, int y, int w, int h)
    {
        super.setBounds(x, y, w, h);
        if (w > 0 && h > 0)
        {
            if (graphics != null)
            {
                graphics.dispose();
            }
            buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            graphics = (Graphics2D) buffer.getGraphics();
            drawVisualization();
            repaint();
        }

    }

    void drawVisualization()
    {
        drawVisualization(graphics);
    }

    protected abstract void drawVisualization(Graphics2D g);

    protected boolean paintOnTop(Graphics2D g, int w, int h)
    {
        return false;
    }

    public Mouseover find(Point p)
    {
        for (Mouseover o : objectLocations)
        {
            if (o.contains(mouse))
            {
                return o;
            }
        }
        return null;
    }

    @Override
    public final void paintComponent(Graphics g1)
    {
        Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(buffer, 0, 0, null);
        int w = getSize().width, h = getSize().height;
        if (paintOnTop(g, w, h))
        {
            return;
        }
        Mouseover highlight = find(mouse);
        if (highlight != null)
        {
            highlight.draw(g, w, h, getModel().getDisplay());

            updateToolTip(getModel().getDisplay(), highlight);

            Dimension tooltipSize = toolTip.getPreferredSize();
            renderPane.paintComponent(g, toolTip, this, highlight.x, highlight.y, tooltipSize.width, tooltipSize.height, true);
        }
    }

    /**
     * Called to update the tool-tip content.
     *
     * @param display the display settings.
     * @param highlight the current highlight.
     */
    protected void updateToolTip(Display display, Mouseover highlight)
    {
        Map<String, Object> fieldValueMap = new LinkedHashMap<String, Object>();

        if (highlight instanceof MouseoverLabel)
        {
            MouseoverLabel mouseoverLabel = (MouseoverLabel) highlight;
            fieldValueMap.put(mouseoverLabel.label1, mouseoverLabel.label2);
        }
        else if (highlight instanceof VisualActMouseover)
        {
            VisualActMouseover visualActMouseover = (VisualActMouseover) highlight;
            VisualAct visualAct = visualActMouseover.visualAct;
            Act act = visualAct.getAct();
            ActDB db = act.getDB();
            List<Field> fields = db.getFields();

            if (visualAct instanceof GroupVisualAct)
            {
                GroupVisualAct groupVisualAct = (GroupVisualAct) visualAct;
                fieldValueMap.put(MessageFormat.format(bundle.getString("GroupVisualAct.itemCount"), groupVisualAct.getNumActs()), "");

                Field sizeField = db.getField(VirtualField.SIZE);
                if (sizeField != null)
                {
                    fieldValueMap.put(
                        MessageFormat.format(bundle.getString("GroupVisualAct.totalLabel"), sizeField.getName()),
                        Display.format(groupVisualAct.getTotal()));
                }
            }
            else
            {
                for (Field field : fields)
                {
                    Object value = act.get(field);
                    String displayString = display.toString(value);
                    fieldValueMap.put(field.getName(), displayString);
                }
            }
        }

        if (!fieldValueMap.isEmpty())
        {
            toolTip.setContent(fieldValueMap, display);
        }
    }
}