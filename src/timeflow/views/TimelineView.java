package timeflow.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

import timeflow.app.ui.ComponentCluster;
import timeflow.data.db.DBUtils;
import timeflow.data.time.Interval;
import timeflow.data.time.RoughTime;
import timeflow.data.time.TimeUnit;
import timeflow.model.TFEvent;
import timeflow.model.TFModel;
import timeflow.model.VirtualField;
import timeflow.vis.Mouseover;
import timeflow.vis.TimeScale;
import timeflow.vis.timeline.AxisRenderer;
import timeflow.vis.timeline.TimelineRenderer;
import timeflow.vis.timeline.TimelineSlider;
import timeflow.vis.timeline.TimelineVisuals;

public class TimelineView extends AbstractView
{
    AxisRenderer grid;
    TimelineRenderer timeline;
    TimelineVisuals visuals;
    TimelinePanel timelinePanel;
    JButton fit;
    ScrollingTimeline scroller;
    JPanel controls;
    int animationSteps = 15;
    TimeAnimator currentAnimator;

    public TimelineView(TFModel model)
    {
        super(model);
        visuals = new TimelineVisuals(model, getComponentOrientation().isLeftToRight());
        grid = new AxisRenderer(visuals);
        timeline = new TimelineRenderer(visuals);

        timelinePanel = new TimelinePanel(model);
        scroller = new ScrollingTimeline();
        setLayout(new BorderLayout());
        add(scroller, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());
        add(bottom, BorderLayout.SOUTH);

        TimelineSlider slider = new TimelineSlider(visuals, 24 * 60 * 60 * 1000L, new Runnable()
        {
            @Override
            public void run()
            {
                redraw();
            }
        });
        bottom.add(slider, BorderLayout.CENTER);

        controls = new JPanel();
        controls.setBackground(Color.white);
        controls.setLayout(new BorderLayout());

        // top part of grid: zoom buttons.
        ComponentCluster buttons = new ComponentCluster("Zoom");
        ImageIcon zoomOutIcon = new ImageIcon("images/zoom_out.gif");
        JButton zoomOut = new JButton(zoomOutIcon);
        buttons.addContent(zoomOut);
        zoomOut.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Interval zoom = visuals.getViewInterval().subinterval(-1, 2).intersection(visuals.getGlobalInterval());
                moveTime(zoom);
            }
        });

        ImageIcon zoomOut100Icon = new ImageIcon("images/zoom_out_100.gif");
        JButton zoomOutAll = new JButton(zoomOut100Icon);
        buttons.addContent(zoomOutAll);
        zoomOutAll.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                moveTime(visuals.getGlobalInterval());
            }
        });

		/*
		// UI for zooming to precisely fit the visible selection.
		// No one seemed to think this was so important, but we may want it again some day.
		// if you uncomment this, then also uncomment the line in reset().
		ImageIcon zoomSelection=new ImageIcon("images/zoom_selection.gif");
		fit=new JButton(zoomSelection);
		fit.setBackground(Color.white);
		buttons.addContent(fit);
		fit.setEnabled(false);
		fit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveTime(visuals.getFitToVisibleRange());
			}});
			*/
        controls.add(buttons, BorderLayout.NORTH);

        // ok, now do second part of grid: layout style buttons.
        ComponentCluster layoutPanel = new ComponentCluster("Layout");

        ButtonGroup layoutGroup = new ButtonGroup();
        ImageIcon looseIcon = new ImageIcon("images/layout_loose.gif");
        JRadioButton loose = new JRadioButton(looseIcon, true);
        loose.setSelectedIcon(new ImageIcon("images/layout_loose_selected.gif"));
        layoutPanel.addContent(loose);
        loose.addActionListener(new LayoutSetter(TimelineVisuals.Layout.LOOSE));
        layoutGroup.add(loose);

        ImageIcon diagonalIcon = new ImageIcon("images/layout_diagonal.gif");
        JRadioButton diagonal = new JRadioButton(diagonalIcon, false);
        diagonal.setSelectedIcon(new ImageIcon("images/layout_diagonal_selected.gif"));
        layoutPanel.addContent(diagonal);
        diagonal.addActionListener(new LayoutSetter(TimelineVisuals.Layout.TIGHT));
        layoutGroup.add(diagonal);

        ImageIcon graphIcon = new ImageIcon("images/layout_graph.gif");
        JRadioButton graph = new JRadioButton(graphIcon, false);
        graph.setSelectedIcon(new ImageIcon("images/layout_graph_selected.gif"));
        layoutPanel.addContent(graph);
        graph.addActionListener(new LayoutSetter(TimelineVisuals.Layout.GRAPH));
        layoutGroup.add(graph);

        controls.add(layoutPanel, BorderLayout.CENTER);
    }

    @Override
    public JComponent _getControls()
    {
        return controls;
    }

    public TimelineVisuals getVisuals()
    {
        return visuals;
    }

    public TimelinePanel getTimelinePanel()
    {
        return timelinePanel;
    }

    public void setAnimationSteps(int animationSteps)
    {
        this.animationSteps = animationSteps;
    }

    public void setAllowPopupMenu(boolean allowPopupMenu)
    {
        timelinePanel.setAllowPopupMenu(allowPopupMenu);
    }

    void moveTime(Interval interval)
    {
        if (currentAnimator != null)
        {
            currentAnimator.cancel();
        }

        currentAnimator = new TimeAnimator(interval, animationSteps);
        currentAnimator.start();
    }

    void redraw()
    {
        visuals.layout();
        timelinePanel.drawVisualization();
        repaint();
    }

    @Override
    protected void onscreen(boolean majorChange)
    {
        visuals.init(majorChange);
        reset(majorChange);
        redraw();
        scroller.calibrate();
    }

    @Override
    protected void _note(TFEvent e)
    {
        visuals.note(e);
        reset(e.affectsRowSet());
    }

    void reset(boolean forceViewChange)
    {
        if (forceViewChange || getModel().getViewInterval() == null)
        {
            int numSelected = getModel().getActs().size();
            int numVisible = DBUtils.count(getModel().getActs(), visuals.getViewInterval(),
                                           getModel().getDB().getField(VirtualField.START));
            if (numVisible < 10 && numSelected > numVisible)
            {
                moveTime(visuals.getFitToVisibleRange());
            }
        }
        // uncomment this if we are using the fit button again.
        //fit.setEnabled(getModel().getActs().size()<getModel().getDB().size());
        redraw();
        scroller.calibrate();
    }

    @Override
    public String getName()
    {
        return "Timeline";
    }

    class LayoutSetter implements ActionListener
    {
        TimelineVisuals.Layout layout;

        LayoutSetter(TimelineVisuals.Layout layout)
        {
            this.layout = layout;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            visuals.setLayoutStyle(layout);
            redraw();
        }
    }

    class TimeAnimator extends Thread
    {
        boolean cancelled = false;
        Interval i1, i2;
        int animationSteps;

        TimeAnimator(Interval i2, int animationSteps)
        {
            this.i1 = visuals.getViewInterval();
            this.i2 = i2;
            this.animationSteps = animationSteps;
        }

        TimeAnimator(Interval i1, Interval i2)
        {
            this.i1 = i1;
            this.i2 = i2;
        }

        @Override
        public void run()
        {
            int n = animationSteps;

            for (int i = 0; i < n && !cancelled; i++)
            {
                final long start = ((n - i) * i1.start + i * i2.start) / n;
                final long end = ((n - i) * i1.end + i * i2.end) / n;
                try
                {
                    SwingUtilities.invokeAndWait(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            visuals.setTimeBounds(start, end);
                            redraw();
                        }
                    });
                    sleep(20);
                }
                catch (Exception e)
                {
                }
            }

            if (cancelled)
            {
                return;
            }

            // Set the final position
            try
            {
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        visuals.setTimeBounds(i2.start, i2.end);
                        redraw();
                    }
                });
            }
            catch (Exception e)
            {
            }
        }

        public void cancel()
        {
            cancelled = true;
        }
    }

    class ScrollingTimeline extends JPanel
    {
        JScrollBar bar;

        public ScrollingTimeline()
        {
            setLayout(new BorderLayout());
            add(timelinePanel, BorderLayout.CENTER);
            bar = new JScrollBar(JScrollBar.VERTICAL);
            add(bar, BorderLayout.EAST);
            bar.addAdjustmentListener(new AdjustmentListener()
            {
                @Override
                public void adjustmentValueChanged(AdjustmentEvent e)
                {
                    timeline.setDY(bar.getValue());
                    timelinePanel.drawVisualization();
                    timelinePanel.repaint();
                }
            });
        }

        public void setBounds(int x, int y, int w, int h)
        {
            super.setBounds(x, y, w, h);
            calibrate();
        }

        void calibrate()
        {
            if (visuals == null)
            {
                return;
            }
            final int height = getSize().height;
            final int desired = Math.max(height, visuals.getFullHeight());
            bar.setVisible(desired > height);
            bar.setMinimum(0);   // is this double setting necessary?
            bar.setMaximum(desired); // more testing is needed, it solved problems
            bar.setVisibleAmount(height);  // on certain Macs are one point.
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    bar.setMinimum(0);
                    bar.setMaximum(desired);
                    bar.setVisibleAmount(height);
                }
            });
        }
    }

    public class TimelinePanel extends AbstractVisualizationView
    {
        public TimelinePanel(TFModel model)
        {
            super(model);

            addMouseWheelListener(new MouseWheelListener()
            {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e)
                {
                    if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)
                    {
                        // Zooming...

                        if (e.getWheelRotation() > 0)
                        {
                            // Zoom out
                            Interval zoom = visuals.getViewInterval()
                                                   .subinterval(-0.5, 1.5)
                                                   .intersection(visuals.getGlobalInterval());

                            moveTime(zoom);
                        }
                        else
                        {
                            // Zoom in
                            double mouseAdjustment = e.getX() / (double) getWidth();

                            Interval zoom = visuals.getViewInterval()
                                                   .subinterval(-0.3 + mouseAdjustment, 0.3 + mouseAdjustment)
                                                   .intersection(visuals.getGlobalInterval());

                            if (zoom.length() < 1000)
                            {
                                zoom = zoom.expand(1000);
                            }

                            moveTime(zoom);
                        }
                    }
                    else if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK)
                    {
                        // Horizontal scrolling...

                        long start = visuals.getViewInterval().start;
                        long length = Math.max(visuals.getViewInterval().length(), 1000);

                        long adjustment = e.getWheelRotation() * length / 5;

                        Interval scroll = visuals.getViewInterval()
                                                 .translateTo(start + adjustment)
                                                 .intersection(visuals.getGlobalInterval());

                        // If the scroll bumps up against the edge of the slider just move it to the global 'start' or 'end'
                        if (scroll.length() != length)
                        {
                            if (e.getWheelRotation() < 0)
                            {
                                scroll = new Interval(visuals.getGlobalInterval().start,
                                                      visuals.getGlobalInterval().start + length)
                                    .intersection(visuals.getGlobalInterval());
                            }
                            else
                            {
                                scroll = new Interval(visuals.getGlobalInterval().end - length,
                                                      visuals.getGlobalInterval().end)
                                    .intersection(visuals.getGlobalInterval());
                            }
                        }

                        moveTime(scroll);
                    }
                    else
                    {
                        // Vertical scrolling...
                        JScrollBar scrollBar = TimelineView.this.scroller.bar;
                        int value = scrollBar.getValue();
                        value += e.getWheelRotation() * 32;
                        scrollBar.setValue(value);
                    }
                }
            });

            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseExited(MouseEvent e)
                {
                    mouse.setLocation(new Point(-1, -1));
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e)
                {
                    // was this a right-click or ctrl-click? ignore.
                    if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3)
                    {
                        return;
                    }

                    // did we click on a date label?
                    for (Mouseover o : objectLocations)
                    {
                        if (o.contains(e.getX(), e.getY()) && o.thing instanceof Interval)
                        {
                            moveTime((Interval) o.thing);
                            return;
                        }
                    }
                    // if not, prepare
                    firstMouse.setLocation(e.getX(), e.getY());
                    mouseIsDown = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e)
                {

                    if (!mouseIsDown) // this means we had clicked on a date label.
                    {
                        return;
                    }
                    mouseIsDown = false;
                    int a = Math.min(mouse.x, firstMouse.x);
                    int b = Math.max(mouse.x, firstMouse.x);
                    long start, end;
                    if (b - a < 16) // a click rather than a drag; just zoom in a bit
                    {
                        repaint();
                        return;
                    }
                    else
                    {
                        start = visuals.getTimeScale().toTime(a);
                        end = visuals.getTimeScale().toTime(b);
                    }
                    moveTime(new Interval(start, end));
                }
            });
        }

        @Override
        public RoughTime getTime(Point p)
        {
            TimeScale scale = visuals.getTimeScale();
            long timestamp = scale.toTime(p.x);
            return new RoughTime(timestamp, TimeUnit.DAY);
        }

        @Override
        protected void drawVisualization(Graphics2D g)
        {
            if (g == null)
            {
                return;
            }
            g.setColor(Color.white);
            g.fillRect(0, 0, 2 * getSize().width, getSize().height);
            visuals.setBounds(0, 0, getSize().width, getSize().height);
            objectLocations = new ArrayList<Mouseover>();
            timeline.render(g, objectLocations, getComponentOrientation().isLeftToRight());
            grid.render(g, objectLocations);
        }

        @Override
        protected boolean paintOnTop(Graphics2D g, int w, int h)
        {
            if (!mouseIsDown)
            {
                g.setColor(Color.lightGray);
                g.drawLine(mouse.x, 0, mouse.x, h);
                return false;
            }

            int a = Math.min(mouse.x, firstMouse.x);
            int b = Math.max(mouse.x, firstMouse.x);
            g.setColor(new Color(255, 255, 120, 100));
            g.fillRect(a, 0, b - a, h);
            g.setColor(Color.orange);
            g.drawLine(a, 0, a, h);
            g.drawLine(b, 0, b, h);

            return true;
        }
    }
}
