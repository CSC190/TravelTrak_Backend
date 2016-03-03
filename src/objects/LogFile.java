package objects;

import java.util.Calendar;

public class LogFile 
{
	private String 	fileName;
	private Calendar dateTime;
	
	public LogFile(String n, Calendar c)
	{
		this.fileName = n;
		
		this.dateTime = c;
	}
	
	public String getFileName()
	{
		return this.fileName;
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
}
