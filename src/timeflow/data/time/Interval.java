package timeflow.data.time;

import java.util.*;

public class Interval {
	public final long start;
	public final long end;
	
	public Interval(long start, long end)
	{
        // Sanity check: make sure start <= end
		this.start = Math.min(start, end);
		this.end = Math.max(start, end);
	}
	
	public Interval copy()
	{
		return new Interval(start, end);
	}
	
	public boolean contains(long x)
	{
		return x>=start && x<=end;
	}
	
	public boolean intersects(Interval x)
	{
		return intersects(x.start, x.end);
	}
	
	public boolean intersects(long start1, long end1)
	{
		return start1<=end && end1>=start;
	}
	
	
	public Interval subinterval(double startFraction, double endFraction)
	{
        long length = Math.max(length(), 1);
		return new Interval((long)(start + startFraction * length),
				            (long)(start + endFraction * length));
	}
	
	public Interval setTo(long start, long end)
	{
        return new Interval(start, end);
	}
	
	public Interval include(long time)
	{
        return new Interval(Math.min(start, time), Math.max(end, time));
	}
	
	public Interval expand(long amount)
    {
        return new Interval(start - amount, end + amount);
	}
	
	public Interval add(long amount)
	{
        return new Interval(start + amount, end + amount);
	}
	
	public long length()
	{
		return end-start;
	}
	
	public Interval translateTo(long newStart)
	{
		return add(newStart-start);
	}
	
	public Interval intersection(Interval i)
	{
		return new Interval(Math.max(i.start, start), Math.min(i.end, end));
	}
	
	public Interval clampInside(Interval container)
	{
		if (length() >= container.length())
        {
			throw new IllegalArgumentException("Containing interval too small: "+container+" < "+this);
        }

		if (start >= container.start && end <= container.end)
        {
			return this;
        }

        return
            this.add(Math.max(0, container.start-start))
            .add(Math.min(0, container.end-end));
	}
	
	public String toString()
	{
		return "[Interval: From "+new Date(start)+" to "+new Date(end)+"]";
	}

}
