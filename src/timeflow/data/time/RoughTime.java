package timeflow.data.time;

import org.joda.time.DateTime;

public class RoughTime implements Comparable
{
    public static final long UNKNOWN = Long.MIN_VALUE;
    private TimeUnit units;
    private DateTime dateTime;

    public RoughTime(TimeUnit units)
    {
        this(null, units);
    }

    public RoughTime(long dateTime, TimeUnit units)
    {
        this(new DateTime(dateTime), units);
    }

    public RoughTime(DateTime dateTime, TimeUnit units)
    {
        this.dateTime = dateTime;
        this.units = units;
    }

    public static int compare(RoughTime t1, RoughTime t2)
    {
        if (t1 == t2)
        {
            return 0;
        }
        else if (t1 == null || !t1.isDefined())
        {
            return -1;
        }
        else if (t2 == null || !t2.isDefined())
        {
            return 1;
        }

        return t1.dateTime.compareTo(t2.dateTime);
    }

    public boolean isDefined()
    {
        return dateTime != null;
    }

    public DateTime getDateTime()
    {
        return dateTime;
    }

    public long getTime()
    {
        return dateTime == null ? UNKNOWN : dateTime.getMillis();
    }

    public void setTime(DateTime time)
    {
        this.dateTime = time;
    }

    public RoughTime plus(TimeUnit unit, int times)
    {
        RoughTime r = copy();
        unit.addTo(r, times);
        return r;
    }

    public String toString()
    {
        if (isDefined())
        {
            return dateTime.toString();
        }

        return "unknown";
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof RoughTime))
        {
            return false;
        }
        RoughTime t = (RoughTime) o;
        return t.units == units && t.dateTime == dateTime;
    }

    public RoughTime copy()
    {
        return new RoughTime(dateTime, units);
    }

    public String format()
    {
        return units.formatFull(dateTime.getMillis());
    }

    @Override
    public int compareTo(Object o)
    {
        return compare(this, (RoughTime) o);
    }
}
