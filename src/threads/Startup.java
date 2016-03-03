/**
 * ****************************************************************
 * File: 			Startup.java
 * Date Created:  	January 13, 2014
 * Programmer:		Dale Reed
 * 
 * Purpose:			This is the main entry point for TravelTrak. 
 * 					It creates and starts up the following threads,
 * 					all of which should continually be running.
 * 					* LoggerThread
 * 					* MacAddressSensorSegmentProcessor
 * 					* MacAddressTravelTimeProcessor
 * 					* MacAddrssUniqueIDProcessor
 * 					* MacAddressDataProcessor
 * 					* DataProcessingDaemon
 * 					* DeviceDaemon
 * 
 * ****************************************************************
 */
package threads;

import gui.UserGui;

import java.io.File;
import java.util.Calendar;

import objects.Database;
import threads.devices.DeviceDaemon;
import threads.processing.DataProcessingDaemon;
import threads.processing.mac_address_core.MacAddressDataProcessor;
import threads.processing.mac_address_core.MacAddressSensorSegmentProcessor;
import threads.processing.mac_address_core.MacAddressTravelTimeProcessor;
import threads.processing.mac_address_core.MacAddressUniqueIDProcessor;
//import objects.mac_address.MacAddressSensorSegment;

public class Startup extends Thread
{	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- TravelTrak Variable Declarations
	
	private LoggerThread lt;
	private MacAddressSensorSegmentProcessor massp;
	private MacAddressTravelTimeProcessor matp;
	private MacAddressUniqueIDProcessor mauidp;
	private MacAddressDataProcessor madp;
	private DataProcessingDaemon dpd;
	private DeviceDaemon dd;
	private UserGui userGui;
	
	private boolean threadAlive = false;
	
	/**
	 * Entry Point for TravelTrak to start
	 * @param args
	 */
	public static void main(String[] args) 
	{		
		System.out.println("C1 V2 Starting: " + Calendar.getInstance().getTime().toString());
	

		new Startup();
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- TravelTrak Construction
	
	/**
	 * Creates the Startup Object to initialize and start up all of the master threads
	 */
	public Startup()
	{
		lt = new LoggerThread("Logger Thread", new File("logs").getAbsolutePath());
		lt.start();		
		massp = new MacAddressSensorSegmentProcessor("MacAddressSensorSegmentProcessor", lt, 15, new Database());
		massp.start();
		
		matp = new MacAddressTravelTimeProcessor("MacAddressTravelTimeProcessor", lt, 15);

		mauidp = new MacAddressUniqueIDProcessor("MacAddressUniqueIDProcessor", lt, matp, 15);
		mauidp.start();
		
		matp.initializeSubThreads(massp, mauidp);
		matp.start();

		madp = new MacAddressDataProcessor("MacAddressDataProcessor", lt, mauidp);
		madp.start();
		
		dpd = new DataProcessingDaemon("DataProcessingDaemon", madp, lt, new Database());
		dpd.start();
		
		boolean fastQuery = false;
		
		dd = new DeviceDaemon(dpd, massp, lt, fastQuery);
		dd.start();
		
		//setUserGui(new UserGui(this));
	}
	
	
	
	
	
	
	
	
	
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Only used and accessed when a GUI is being displayed for the backend program
	
	
	public void run()
	{
		while (this.threadAlive)
		{
			try 
			{
				Thread.sleep(60000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public LoggerThread getLoggerThread()
	{
		return this.lt;
	}
		
	public MacAddressTravelTimeProcessor getTravelTimeProcessor()
	{
		return this.matp;
	}
	
	public MacAddressUniqueIDProcessor getUniqueIDProcessor()
	{
		return this.mauidp;
	}
	
	public DataProcessingDaemon getDataProcessingDaemon()
	{
		return this.dpd;
	}
	
	public DeviceDaemon getDeviceDaemon()
	{
		return this.dd;
	}

	public UserGui getUserGui() {
		return userGui;
	}

	public void setUserGui(UserGui userGui) {
		this.userGui = userGui;
	}
}
