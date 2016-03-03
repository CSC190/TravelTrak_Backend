/**
 * ****************************************************************
 * File: 			MacAddressDataProcessor.java
 * Date Created:  	January 16, 2014
 * Programmer:		Dale Reed
 * 
 * Purpose:			This thread takes the raw mac address data and 
 * 					breaks it apart into Location/Time/Mac objects
 * 					so it can be passed ahead for more processing
 * 
 * ****************************************************************
 */
package threads.processing.mac_address_core;

import java.util.Calendar;
import java.util.Vector;

import objects.LogItem;
import objects.mac_address.MacAddressData;
import objects.mac_address.RawMacData;
import statics.Globals;
import threads.LoggerThread;

public class MacAddressDataProcessor extends Thread
{
	// Indicates how long the thread is to sleep between runs
	private int runInterval = 5000;

	// Indicates if the thread is to continue running
	private boolean threadAlive;

	// Maintains a list containing all of the RawMacData and MacAddressData that has or needs to be processed
	private Vector<RawMacData>rawMacData;
	private Vector<MacAddressData> processedMacData;
	
	// Provides a reference back to the LoggerThread so that log files may be written
	private LoggerThread lt;
	
	// Provides a reference back to the MacAddressUniqueIDProcessor so that each mac can be uniquely identified
	private MacAddressUniqueIDProcessor mauip;
	
	/**
	 * Creates the MacAddressDataProcessor Thread to facilitiate breaking apart all of the data and sending it off for processing
	 * 
	 * @param threadName 	- The name of the thread
	 * @param lt			- Reference to the LoggerThread
	 * @param mauip			- Reference to the MacAddressUniqueIDProcessor
	 */	
	public MacAddressDataProcessor(String threadName, LoggerThread lt, MacAddressUniqueIDProcessor mauip)
	{
		// Sets the name of the thread for use with the DataProcessingDaemon	
		this.setName(threadName);
		
		// Sets the LoggerThread reference
		this.lt = lt;

		// Sets the MacAddressUniqueIDProcessor reference
		this.mauip = mauip;
		
		// Initializes the rawMacData and processedMacData arrays
		rawMacData = new Vector<RawMacData>();
		processedMacData = new Vector<MacAddressData>();
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- MacAddressDataProcessor Threading Methods
	// -- Methods Contained here:
	//		-- run()
	// 		-- addData()
	//		-- processData()
	//		-- convertToUnixTime()
	// 		-- createAndSendLogData()
	
	/**
	 * Continually runs as long as the thread's threadAlive status is true. This monitors the rawMacData array for any data
	 * that needs to be processed, and if there is any, processes it and sends it off for more processing.
	 */
	public void run()
	{
		// Since the thread is starting, set its threadAlive status to true
		this.threadAlive = true;
		
		// Begin the core loop of the MacAddressDataProcessor thread
		while (this.threadAlive)
		{
			// Check and see if the thread has been interrupted. This is a built in Thread method.
			MacAddressDataProcessor.interrupted();
			
			try 
			{
				// If there are elements in the rawMacData to be processed, then begin processing them.
				if (this.rawMacData.size() > 0)
				{
					// Write a log file indicating that processing has started.
					this.createAndSendLogData(false, "Info", "\n" + Calendar.getInstance().getTime().toString() + " -- Running " + this.getName());
					
					// Retrieve the start time so that the duration of processing can be analyzed
					long threadStartTime = Calendar.getInstance().getTimeInMillis();

					// Loop as long as there is data remaining to be processed.
					while (rawMacData.size() > 0)
					{
						// Remove the first element in the array and process it.
						this.processData(rawMacData.remove(0));
					}
					
					// Write a log file indicating the elapsed processing time of the thread.
					this.createAndSendLogData(false, "Info", "Elapsed processing time: " + (Calendar.getInstance().getTimeInMillis() - threadStartTime) + " milliseconds. Sleeping for " + this.runInterval + " milliseconds.");
				}
				
				// If there are no elements to be processed, make the thread sleep.
				if (this.rawMacData.size() == 0)
				{
					MacAddressDataProcessor.sleep(this.runInterval);
				}
			} 
			catch (InterruptedException e) 
			{
				if (e.getMessage().equalsIgnoreCase(Globals.SLEEP_INTERRUPTED))
				{

				}
				else
				{
					e.printStackTrace();
				}	
			}
		}
	}
	
	public void addData(RawMacData macRawData)
	{
		this.rawMacData.add(macRawData);
	}

	/**
	 * Processes the RawData into a formatted version that can be quickly analyzed by the next stage in the processing
	 * 
	 * @param macRawData
	 */
	private void processData(RawMacData macRawData)
	{		
		// Split the RawData device data line by all commas
		String lineElements[] = macRawData.getDeviceData().split(",");
		
		// Write a log message indicating how many records are going to be processed 
		this.createAndSendLogData(false, "Processing", "Processing " + (lineElements.length - 2) + " events from data '" + macRawData.getDeviceData() + "'");

		try 
		{		
			// Loop through all of the elements stored in the lineElements array
			for (String s : lineElements)
			{
				// Make sure that the length of the string is greater than 3. This is usually due to no data being returned (null), and
				// only a prefix/suffix being received.
				if (s.length() > 3)
				{
					// Split the data based on an '*' between the Mac & Time
					int dataSplitIndex = s.indexOf('*');
					
					// Find where GPS info comes in 
					int gpIndex = s.indexOf('#');
					
					// Find where voltage comes in
					int vIndex = s.indexOf('$');
					
					// Find where temperature comes in
					int tIndex = s.indexOf('!');
					
					// Split between latitude and longitude
					int gpSplit = s.indexOf(';');
					
					// Retrieve the mac and time elements of the string
					String mac = s.substring(0, dataSplitIndex);
					String time = s.substring(dataSplitIndex + 1);
					
					// Retrieve GPS information if present
					//String latitude = s.substring(gpIndex + 1,gpSplit);
					//String longitude = s.substring(gpSplit + 1);
					
					// Convert the time into a unix timestamp
					long timestamp = convertToUnixTime(time, -1, -1, -1);
					
					// If the mac & time lengths are not 12 and 6 respectively, then there is an error with this data. 
					// Write the data to an error log file so it can be analyzed as to why its invalid.
					if (mac.length() != 12 && time.length() != 6)
					{
						this.createAndSendLogData(false, "ERROR", "Received element '" + s + "' has a format error and cannot be processed");
						this.createAndSendLogData(false, "Processing", "Element '" + s + "' has a format error and cannot be processed");
					}
					else
					{								
						// Create a new MacAddressData element based off the deviceID, timestamp, and mac address
						MacAddressData mad = new MacAddressData(macRawData.getDeviceID(), timestamp, mac);

						// Write the new MacAddressData element to a log file
						this.createAndSendLogData(false, "Processing", String.format("Data: %s processed into %s", s, mad));
						
						// Add the MacAddressData element to the processedMacArray so it can be forwarded to the next stage
						processedMacData.add(mad);
					}
				}
				// This will run if erroneous data is received from the device.
				else if (!s.equals("WF") && !s.equals("BT") && !s.equals("E"))
				{
					this.createAndSendLogData(false, "Processing", "Unable to process line data '" + s + "'");
				}
			}
			
			// Write a log file based on how many records were successfully processed.
			this.createAndSendLogData(false, "Info", "Successfully added " + processedMacData.size() + " to the MacAddressUniqueIDProcessor from device " + macRawData.getDeviceID());
			this.createAndSendLogData(false, "Processing", "Successfully added " + processedMacData.size() + " events to the MacAddressUniqueIDProcessor from device " + macRawData.getDeviceID());

			// Add all the processedMacData to the MacAddressUniqueIDProcessor for further processing.
			this.mauip.addData(processedMacData);
			
			// Interrupt the MacAddressUniqueIDProcessor so it immediately begins processing data
			this.mauip.interrupt();

			// Write a log file indicating how many records were sent off for processing
			this.createAndSendLogData(false, "Info", "Sent " + processedMacData.size() + " to MacAddressUniqueIDProcessor");

			// Empty out the processedMacData array to avoid duplicate records being sent for processing
			processedMacData.clear();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Converts a String representation of a date into unix time
	 * 
	 * @param item 	- The time being converted
	 * @param yr	- The year to set the date to
	 * @param mn	- The month to set the date to
	 * @param dy	- The day to set the date to
	 * 
	 * @return unixtime - The timestamp in unix time
	 */
	public static long convertToUnixTime(String item, int yr, int mn, int dy) 
	{
		// Get a calendar instance for use in converting the string date to unix time
		Calendar rightNow = Calendar.getInstance();

		// If year, month and day have values over 0, then set the year/month/day
		if (yr > -1 && mn > -1 && dy > -1) 
		{
			rightNow.set(Calendar.YEAR, yr);
			rightNow.set(Calendar.MONTH, mn - 1);
			rightNow.set(Calendar.DATE, dy);
		}

		// Remove all leading/trailing whitespace from the time string
		item = item.trim();
		
		// Ensure that the time is of proper length (must be 6 characters). If it is, then update the calendar object and return the timestamp
		if (item.length() == 6) 
		{
			rightNow.set(Calendar.HOUR_OF_DAY, Short.parseShort(item.substring(0, 2)));
			rightNow.set(Calendar.MINUTE, Short.parseShort(item.substring(2, 4)));
			rightNow.set(Calendar.SECOND, Short.parseShort(item.substring(4)));
			
			return (rightNow.getTimeInMillis()/1000)*1000; 			
		}
		
		// If this is reached, there was an error in calculating the timestamp
		return 0;
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
