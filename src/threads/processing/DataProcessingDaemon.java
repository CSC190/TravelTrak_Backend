/**
 * ****************************************************************
 * File: 			DataProcessingDaemon.java
 * Date Created:  	January 13, 2014
 * Programmer:		Dale Reed
 * 
 * Purpose:			This thread is accessed by all of the Device
 * 					Threads when they have data that has been 
 * 					received and needs to be processed.
 * 
 * ****************************************************************
 */
package threads.processing;

import java.util.Calendar;
import java.util.Vector;

import objects.Database;
import objects.LogItem;
import objects.mac_address.RawMacData;
import statics.Globals;
import threads.LoggerThread;
import threads.processing.mac_address_core.MacAddressDataProcessor;

public class DataProcessingDaemon extends Thread
{
	
	/**
	 * Provides access to the LoggerThread so that any debug information for this thread can be stored.
	 */
	private LoggerThread lt;
	
	/**
	 * Used for checking if the thread is currently sleeping or not
	 */
	private boolean isSleeping = false;
	
	/**
	 * Used in making sure the thread sleeps for a little bit between running analysis on data
	 */
	private int runInterval = 5000;
	
	/**
	 * 	Indicates if the thread is to continue running
	 */
	private boolean threadAlive = false;
	
	/**
	 * Provides a reference back to the MacAddressDataProcessor so that when the data has gone through the initial processing,
	 * it can be forwarded to the next stage of processing.
	 */
	private MacAddressDataProcessor macAddressProcessor;
	
	/**
	 *  Maintains a running list of all data that needs re-formatted for processing
	 */
	private Vector<RawMacData>rawMacData;

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- DataProcessingDaemon Construction

	/**
	 * Creates the DataProcessingDaemon thread so it can begin accepting data from devices for processing
	 * 
	 * @param threadName 	- The name of the thread
	 * @param map			- Reference to the MacAddressDataProcessor
	 * @param lt			- Reference to the LoggerThread
	 * @param db			- Reference to the Database
	 */
	public DataProcessingDaemon(String threadName, MacAddressDataProcessor map, LoggerThread lt, Database db) 
	{
		// Sets the name of the thread for use with the DataProcessingDaemon
		this.setName(threadName);
		
		// Sets the MacAddressProcessor reference
		this.macAddressProcessor = map;
		
		// Sets the LoggerThread reference
		this.lt = lt;

		// Initializes the array to hold all of the raw data from the devices
		rawMacData = new Vector<RawMacData>();
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Data Processing Daemon Threading Methods 
	// -- Methods contained here:
	// 		-- run()
	// 		-- isSleeping()
	//		-- addData()
	// 		-- createAndSendLogData()
	
	/**
	 * Continually runs as long as the thread's threadAlive status is true. This handles the processing of any data received from
	 * the DeviceThreads and formats them so they may be processed in the MacAddressDataProcessor
	 */
	public void run()
	{
		// Since the thread is starting, set its threadAlive status to true
		this.threadAlive = true;

		// Begin the core loop of the DataProcessingDaemon thread
		while (this.threadAlive)
		{
			// Check and see if the thread has been interrupted. This is a built in Thread method.
			DataProcessingDaemon.interrupted();
			
			// Indicate that the thread is not currently sleeping and is performing analysis
			this.isSleeping = false;
			
			try 
			{
				// Write to a log file that the thread is running and processing data
				createAndSendLogData(false, "Info", "\n" + Calendar.getInstance().getTime().toString() + " -- Running " + this.getName());
				
				// Retrieve the start time so that the duration of processing can be analyzed
				long threadStartTime = Calendar.getInstance().getTimeInMillis();
				
				// If there is records in the rawMacData array, then process them.
				if (rawMacData.size() > 0)
				{
					// Write to a log file information on how many events are being sent off for processing
					this.createAndSendLogData(false, "Info", "Sending " + rawMacData.size() + " mac events to MacAddressDataProcessor");					

					// As long as there are records to be processed, loop through them all.
					while (rawMacData.size() > 0)
					{
						// Remove the first element in the array and send it off for processing.
						this.macAddressProcessor.addData(rawMacData.remove(0));
					}
					
					// Write to a log file that the MacAddressDataProcessor is being woken up
					this.createAndSendLogData(false, "Info", "Waking up MacAddressDataProcessor");
					
					// Wake up the MacAddressProcessor
					this.macAddressProcessor.interrupt();
				}
				
				// Write to a log file information on the elapsed processing time for the data
				createAndSendLogData(false, "Info", "Elapsed processing time: " + (Calendar.getInstance().getTimeInMillis() - threadStartTime) + " milliseconds. Sleeping for " + runInterval + " milliseconds.\n");

				// Indicate that the thread is now sleeping
				this.isSleeping = true;
				
				// Put the thread to sleep
				Thread.sleep(runInterval);
			} 
			catch (InterruptedException e) 
			{
				if (e.getMessage().equalsIgnoreCase(Globals.SLEEP_INTERRUPTED))
				{
					// Indicate that the thread was woken up by an external source
					this.createAndSendLogData(false, "Info", "Sleep was interrupted.");
				}
				else
				{
					e.printStackTrace();
				}	
			} 
		}
	}
	
	/**
	 * Queries the thread for its sleep status
	 * 
	 * @return sleep - returns the sleeping status of the thread
	 */
	public boolean isSleeping()
	{
		return this.isSleeping;
	}
	
	/**
	 * Accepts data from a DeviceThread and adds it to the appropriate array for processing
	 * 
	 * @param rawData
	 */
	public void addData(Object rawData)
	{
		// Write to a log file information about the raw data to be processed
		this.createAndSendLogData(false, "Info", String.format("Data added to DPD: %s", rawData));
		if (rawData instanceof RawMacData)
		{
			// Add the rawData to the array for processing
			rawMacData.add((RawMacData) rawData);
		}
		else
		{
			// TODO: This needs to be filled in with C1 Data. Currently it is just thrown away and discarded.
		}
	}

	
	/**
	 * Sends a log message to the LoggerThread 
	 * 
	 * @param print		- Indicates if the thread is to be printed to the console
	 * @param type		- Indicates what type of message is being written
	 * @param message	- The message that is to be written to the log file
	 */
	private void createAndSendLogData(boolean print, String type, String message)
	{
		// Create a new LogItem using the thread name, message, log type, and the timestamp
		LogItem li = new LogItem(this.getName(), message, type, Calendar.getInstance());

		// Add the log item to the LoggerThread
		lt.addToList(li);
		
		// If print is true, then print the message to the console
		if (print)
			System.out.println(li.getMessage());
	}
}
