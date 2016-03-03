package objects;

import java.util.Calendar;

public class LogItem 
{
	private String threadName;
	private String message;
	private String type;
	
	private Calendar dateTime;
	
	public LogItem (String tn, String mg, String ty, Calendar dt)
	{
		this.threadName = tn;
		this.message = mg;
		this.type = ty;
		
		this.dateTime = dt;
	}
	
	public String getThreadName()
	{
		return this.threadName;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
	public Calendar getCalendar()
	{
		return this.dateTime;
	}

	public int getYear()
	{
		return this.dateTime.get(Calendar.YEAR);
	}

	public int getMonth()
	{
		return this.dateTime.get(Calendar.MONTH) + 1;
	}

	public int getDate()
	{
		return this.dateTime.get(Calendar.DATE);
	}

	public int getHour()
	{
		return this.dateTime.get(Calendar.HOUR_OF_DAY);
	}

	public String toString()
	{
		if (message.equals("\n\n"))
		{
			return "";
		}
		else if (type.equals("CSV"))
		{
			return String.format(message);
		}
		else
		{
			return String.format("%s [%s] - %s", dateTime.getTime().toString(), type, message);
		}
	}
}
