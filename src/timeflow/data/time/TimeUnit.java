package timeflow.data.time;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUnit {

	public static final TimeUnit YEAR=new TimeUnit("Years", Calendar.YEAR, 365*24*60*60*1000L, "yyyy", "yyyy");
	public static final TimeUnit MONTH=new TimeUnit("Months", Calendar.MONTH, 30*24*60*60*1000L, "MMM", "MMM yyyy");
	public static final TimeUnit WEEK=new TimeUnit("Weeks", Calendar.WEEK_OF_YEAR, 7*24*60*60*1000L, "d", "MMM d yyyy");
	public static final TimeUnit DAY=new TimeUnit("Days", Calendar.DAY_OF_MONTH, 24*60*60*1000L, "d", "MMM d yyyy");
	public static final TimeUnit DAY_OF_WEEK=new TimeUnit("Days", Calendar.DAY_OF_WEEK, 24*60*60*1000L, "d", "MMM d yyyy");
	public static final TimeUnit HOUR=new TimeUnit("Hours", Calendar.HOUR_OF_DAY, 60*60*1000L, "kk:mm", "MMM d yyyy kk:mm");
	public static final TimeUnit MINUTE=new TimeUnit("Minutes", Calendar.MINUTE, 60*1000L, ":mm", "MMM d yyyy kk:mm");
	public static final TimeUnit SECOND=new TimeUnit("Seconds", Calendar.SECOND, 1000L, ":ss", "MMM d yyyy kk:mm:ss");
	public static final TimeUnit DECADE=multipleYears(10);
	public static final TimeUnit CENTURY=multipleYears(100);
	
	private int quantity;	
	private long roughSize;
	private SimpleDateFormat format, fullFormat;
	private String name;
	private int calendarCode;
	
	private TimeUnit()
	{		
	}
	
	private TimeUnit(String name, int calendarCode, long roughSize, String formatPattern, String fullFormatPattern)
	{
		this.name=name;
		this.calendarCode=calendarCode;
		this.roughSize=roughSize;
		format=new SimpleDateFormat(formatPattern);
		fullFormat=new SimpleDateFormat(fullFormatPattern);
		quantity=1;
	}
	
	public String toString()
	{
		return "[TimeUnit: "+name+"]";
	}

	public static TimeUnit multipleYears(int numYears)
	{
		TimeUnit t=new TimeUnit();
		t.name=numYears+" Years";
		t.calendarCode=Calendar.YEAR;
		t.roughSize=YEAR.roughSize*numYears;
		t.format=YEAR.format;
		t.fullFormat=YEAR.fullFormat;
		t.quantity=numYears;
		return t;
	}
	
	public static TimeUnit multipleWeeks(int num)
	{
		TimeUnit t=new TimeUnit();
		t.name=num+" Weeks";
		t.calendarCode=Calendar.WEEK_OF_YEAR;
		t.roughSize=WEEK.roughSize*num;
		t.format=WEEK.format;
		t.fullFormat=WEEK.fullFormat;
		t.quantity=num;
		return t;
	}
	
	public TimeUnit times(int quantity)
	{
		TimeUnit t=new TimeUnit();
		t.name=quantity+" "+this.name;
		t.calendarCode=this.calendarCode;
		t.roughSize=this.roughSize*quantity;
		t.format=this.format;
		t.fullFormat=this.fullFormat;
		t.quantity=quantity;
		return t;
		
	}

	
	public int numUnitsIn(TimeUnit u)
	{
		return (int)Math.round(u.getRoughSize()/(double)getRoughSize());
	}
	
	public boolean isDayOrLess()
	{
		return roughSize <= 24*60*60*1000L;
	}
	
	public RoughTime roundDown(long timestamp)
	{
		return round(timestamp, false);
	}
	
	public RoughTime roundUp(long timestamp)
	{
		return round(timestamp, true);
	}

    private DateTime.Property getProperty(DateTime dateTime)
    {
        switch (calendarCode)
        {
            case Calendar.YEAR:
                return dateTime.year();

            case Calendar.MONTH:
                return dateTime.monthOfYear();

            case Calendar.WEEK_OF_YEAR:
                return dateTime.weekOfWeekyear();

            case Calendar.DAY_OF_WEEK:
                return dateTime.dayOfWeek();

            case Calendar.DAY_OF_MONTH:
                return dateTime.dayOfMonth();

            case Calendar.HOUR_OF_DAY:
                return dateTime.hourOfDay();

            case Calendar.MINUTE:
                return dateTime.minuteOfHour();

            case Calendar.SECOND:
                return dateTime.secondOfMinute();

            default:
                throw new IllegalStateException("Unsupported calendar code: " + calendarCode);
        }
    }

	public RoughTime round(long timestamp, boolean up)
	{
        DateTime dateTime = new DateTime(timestamp);
        DateTime.Property property = getProperty(dateTime);

        dateTime = up
                ? property.roundCeilingCopy()
                : property.roundFloorCopy();
		
		return new RoughTime(dateTime.getMillis(), this);
	}
	
	public int get(long timestamp)
	{
        DateTime dateTime = new DateTime(timestamp);
        DateTime.Property property = getProperty(dateTime);
        int n = property.get();

		return quantity == 1 ? n : n % quantity;
	}
	
	public void addTo(RoughTime r)
	{
		addTo(r,1);
	}
	
	public void addTo(RoughTime r, int times)
	{
        DateTime dateTime = new DateTime(r.getTime());
        DateTime.Property property = getProperty(dateTime);
        dateTime = property.addToCopy(quantity * times);

		r.setTime(dateTime);
	}
	
	// Finding the difference between two dates, in a given unit of time,
	// is much subtler than you'd think! And annoyingly, the Calendar class does not do
	// this for you, even though it actually "knows" how to do so since it
	// can add fields.
	//
	// The most vexing problem is dealing with daylight savings time,
	// which means that one day a year has 23 hours and one day has 25 hours.
	// We also have to handle the fact that months and years aren't constant lengths.
	//
	// Rather than write all this ourselves, in this code we
	// use the Calendar^H^H^H^H^H^H^H^H^H Joda DateTime class to do the heavy lifting.
	public long difference(long x, long y)
	{
        DateTime dateTime1 = new DateTime(x);
        DateTime dateTime2 = new DateTime(y);
        DateTime.Property property = getProperty(dateTime1);
        return property.getDifference(dateTime2);
	}

	public long approxNumInRange(long start, long end)
	{
		return 1+(end-start)/roughSize;
	}
	
	public long getRoughSize() {
		return roughSize;
	}

	public String format(DateTime dateTime)
	{
		return format.format(dateTime.toDate());
	}

	public String formatFull(Date date)
	{
		return fullFormat.format(date);
	}

	public String formatFull(long timestamp)
	{
		return fullFormat.format(new Date(timestamp));
	}

	public String getName() {
		return name;
	}
}
