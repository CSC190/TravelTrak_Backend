package objects;

import java.util.Date;

public class ThreadInformation 
{
	private String threadManager;
	private String threadName;
	private String threadType;
	private Date dateActive;
	
	public ThreadInformation(String tm, String tn, String tt, Date active)
	{
		this.threadManager = tm;
		this.threadName = tn;
		this.threadType = tt;
		this.dateActive = active;
	}
	
	public String getThreadManager()
	{
		return this.threadManager;
	}
	
	public String getThreadName()
	{
		return this.threadName;
	}
	
	public String getThreadType()
	{
		return this.threadType;
	}
	
	public Date getDateActive()
	{
		return this.dateActive;
	}
	
	public String toString()
	{
		return String.format("%s - %s - %s - %s", this.threadManager, this.threadName, this.threadType, this.dateActive.toString());
	}
}
