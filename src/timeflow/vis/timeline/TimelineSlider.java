package timeflow.vis.timeline;

import timeflow.data.time.Interval;
import timeflow.model.ModelPanel;
import timeflow.model.TFEvent;
import timeflow.vis.TimeScale;
import timeflow.vis.VisualAct;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.LineMetrics;
import java.util.ResourceBundle;

public class TimelineSlider extends ModelPanel
{
    /**
     * The resources.
     */
    private static final ResourceBundle bundle = ResourceBundle.getBundle("timeflow/vis/timeline/Bundle");
	
	TimelineVisuals visuals;
	Interval original;
	long minRange;
	int ew=10;
	int eventRadius=2;
	TimeScale scale;
	
	Point mouseHit=new Point();
	Point mouse=new Point(-1,0);
	enum Modify {START, END, POSITION, NONE};
	Modify change=Modify.NONE;
	Rectangle startRect=new Rectangle(-1,-1,0,0);
	Rectangle endRect=new Rectangle(-1,-1,0,0);
	Rectangle positionRect=new Rectangle(-1,-1,0,0);
	Color sidePlain=Color.orange;
	Color sideMouse=new Color(230,100,0);

	
	public TimelineSlider(final TimelineVisuals visuals, final long minRange, final Runnable action)
	{
		super(visuals.getModel());
		
		this.minRange=minRange;
		this.visuals=visuals;

        addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                Interval window = null;

                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)
                {
                    // Zooming...

                    if (e.getWheelRotation() > 0)
                    {
                        // Zoom out
                        window = visuals.getViewInterval()
                            .subinterval(-0.5, 1.5)
                            .intersection(visuals.getGlobalInterval());
                    }
                    else
                    {
                        // Zoom in
                        double mouseAdjustment = e.getX() / (double) getWidth();

                        window = visuals.getViewInterval()
                            .subinterval(-0.3 + mouseAdjustment, 0.3 + mouseAdjustment)
                            .intersection(visuals.getGlobalInterval());

                        if (window.length() < 1000)
                        {
                            window = window.expand(1000);
                        }
                    }
                }
                else
                {
                    // Horizontal Scrolling...

                    long start = visuals.getViewInterval().start;
                    long length = Math.max(visuals.getViewInterval().length(), 1000);

                    long adjustment = e.getWheelRotation() * length / 5;

                    window = visuals.getViewInterval()
                        .translateTo(start + adjustment)
                        .intersection(visuals.getGlobalInterval());

                    // If the scroll bumps up against the edge of the slider just move it to the global 'start' or 'end'
                    if (window.length() != length)
                    {
                        if (e.getWheelRotation() < 0)
                        {
                            window = new Interval(visuals.getGlobalInterval().start,
                                                  visuals.getGlobalInterval().start + length)
                                .intersection(visuals.getGlobalInterval());
                        }
                        else
                        {
                            window = new Interval(visuals.getGlobalInterval().end - length,
                                                  visuals.getGlobalInterval().end)
                                .intersection(visuals.getGlobalInterval());
                        }
                    }
                }

                if (window != null)
                {
                    setWindow(window);
                    getModel().setViewInterval(window);
                    action.run();
                    repaint();
                }
            }
        });

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				int mx=e.getX();
				int my=e.getY();
				if (positionRect.contains(mx,my))
					change=Modify.POSITION;
				else if (startRect.contains(mx, my))
					change=Modify.START;
				else if (endRect.contains(mx,my))
					change=Modify.END;
				else
					change=Modify.NONE;
				mouseHit.setLocation(mx,my);
				original=getWindow();
				mouse.setLocation(mx,my);
				repaint();
			}

            @Override
            public void mouseClicked(MouseEvent e)
            {
                Interval window = null;

                if (e.getClickCount() == 1)
                {
                    if (SwingUtilities.isLeftMouseButton(e))
                    {
                        // Left-click sets the center of the view interval
                        long timeDiff = scale.spaceToTime(e.getX());
                        Interval limits = visuals.getGlobalInterval();
                        window = getWindow();

                        window = window
                            .translateTo(limits.start + timeDiff - window.length() / 2);

                        if (limits.length() > 1000)
                        {
                            window = window.clampInside(limits);
                        }
                    }
                    else if (SwingUtilities.isRightMouseButton(e))
                    {
                        // Zoom out a bit on right-click
                        window = getWindow()
                            .subinterval(-0.5, 1.5)
                            .intersection(visuals.getGlobalInterval());
                    }
                    else if (SwingUtilities.isMiddleMouseButton(e))
                    {
                        // Zoom out 100%
                        window = visuals.getGlobalInterval();
                    }
                }
                else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
                {
                    // Zoom in a bit on left double-click
                    double mouseAdjustment = e.getX() / (double) getWidth();

                    window = visuals.getViewInterval()
                        .subinterval(-0.3 + mouseAdjustment, 0.3 + mouseAdjustment)
                        .intersection(visuals.getGlobalInterval());

                    if (window.length() < 1000)
                    {
                        window = window.expand(1000);
                    }
                }

                if (window != null)
                {
                    setWindow(window);
                    getModel().setViewInterval(window);
                    action.run();
                    repaint();
                }
            }

			@Override
			public void mouseReleased(MouseEvent e) {
				change=Modify.NONE;
				repaint();
			}});

		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				
				if (change==Modify.NONE)
					return;
				mouse.setLocation(e.getX(), e.getY());
				int mouseDiff=mouse.x-mouseHit.x;
				Interval limits=visuals.getGlobalInterval();
				long timeDiff=scale.spaceToTime(mouseDiff);

                Interval window = getWindow();
				switch (change)
				{
					case POSITION: 						
						window = window
                            .translateTo(original.start+timeDiff)
                            .clampInside(limits);
                        setWindow(window);
						break;

					case START:
                        long start = Math.min(original.start+timeDiff, original.end-minRange);
                        start = Math.max(start, limits.start);
                        window = window.setTo(start, window.end);
                        setWindow(window);
						break;

					case END:
                        long end = Math.max(original.end+timeDiff, original.start+minRange);
                        end = Math.min(end, limits.end);
                        window = window.setTo(window.start, end);
                        setWindow(window);
                        break;
				}

				getModel().setViewInterval(window);
				action.run();
				repaint();
			}
		});
	}
	
	private Interval getWindow()
	{
		return visuals.getViewInterval();
	}

	private void setWindow(Interval interval)
	{
		visuals.setViewInterval(interval);
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(600,30);
	}
	
	public void setMinRange(long minRange)
	{
		this.minRange=minRange;
	}

	@Override
	public void note(TFEvent e) {
		repaint();
	}
	
	void setTimeInterval(Interval interval)
	{
        setWindow(interval);
		repaint();
	}
	
	public void paintComponent(Graphics g1)
	{
		int w=getSize().width, h=getSize().height;
		Graphics2D g=(Graphics2D)g1;
		
		long start=System.currentTimeMillis();
		
		// draw main backdrop.
		g.setColor(Color.white);
		g.fillRect(0,0,w,h);
		
		if (visuals.getModel()==null || visuals.getModel().getActs()==null)
		{
			g.setColor(Color.darkGray);
			g.drawString(bundle.getString("TimelineSlider.noData"), 5, 20);
			return;
		}
		
		scale=new TimeScale();
		scale.setDateRange(visuals.getGlobalInterval());
		scale.setNumberRange(ew, w-ew);
		
		
		// draw the area for the central "thumb".
		int lx=scale.toInt(getWindow().start);
		int rx=scale.toInt(getWindow().end);
		g.setColor(change==Modify.POSITION ? new Color(255,255,120) : new Color(255,245,200));
		positionRect.setBounds(lx,0,rx-lx,h);
		g.fill(positionRect);		

		// Figure out how best to draw events.
		// If there are too many, we just draw a kind of histogram of frequency,
		// rather than using the timeline layout.
		int slotW=2*eventRadius;
		int slotNum=w/slotW+1;
		int[] slots=new int[slotNum];
		int mostInSlot=0;
		for (VisualAct v: visuals.getVisualActs())
		{
			if (!v.isVisible())
				continue;
			int x=scale.toInt(v.getStart().getTime());
			int s=x/slotW;
			if (s>=0 && s<slotNum)
			{
				slots[s]++;
				mostInSlot=Math.max(mostInSlot, slots[s]);
			}
		}

		if (mostInSlot > 30)
		{
            double logMax = Math.log10(mostInSlot);

			g.setColor(Color.gray);
			for (int i=0; i<slots.length; i++)
			{
                double logValue = Math.log10(slots[i]);
                if (Double.isInfinite(logValue) || Double.isNaN(logValue))
                {
                    logValue = 0;
                }

				int sh = (int)((h*logValue) / logMax);
				g.fillRect(slotW*i, h-sh, slotW, sh);
			}
		}
		else
		{
			// draw individual events.
			for (VisualAct v: visuals.getVisualActs())
			{
				if (!v.isVisible())
					continue;
				g.setColor(v.getColor());
				int x=scale.toInt(v.getStart().getTime());
	
				int y=eventRadius+(int)(v.getY()*h)/(visuals.getBounds().height-2*eventRadius);
				g.fillRect(x-1,y-eventRadius,2*eventRadius,3);
				if (v.getEnd()!=null)
				{
					int endX=scale.toInt(v.getEnd().getTime());
					g.drawLine(x,y,endX,y);
				}
			}
		}
		
		g.setColor(Color.gray);
		g.drawLine(0,0,w,0);
		g.drawLine(0,h-1,w,h-1);
		
		// draw "expansion" areas on sides of thumb.
		startRect.setBounds(positionRect.x-ew,1,ew,h-2);
		g.setColor(change==Modify.START ? sideMouse : sidePlain);
		g.fill(startRect);
		endRect.setBounds(positionRect.x+positionRect.width,1,ew,h-2);
		g.setColor(change==Modify.END ? sideMouse : sidePlain);
		g.fill(endRect);

        if (mostInSlot > 30)
        {
            // Render the scale label
            g.setColor(Color.lightGray);
            String label = bundle.getString("TimelineSlider.log10Label");
            LineMetrics lineMetrics = g.getFont().getLineMetrics(label, g.getFontRenderContext());
            g.drawString(label, 0, h - lineMetrics.getHeight());
        }
	}
}
