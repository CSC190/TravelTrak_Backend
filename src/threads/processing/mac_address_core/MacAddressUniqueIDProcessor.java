/**
 * ****************************************************************
 * File: 			MacAddressUniqueIDProcessor.java
 * Date Created:  	January 16, 2014
 * Programmer:		Dale Reed
 * 
 * Purpose:			This thread manages all of the unique mac 
 * 					addresses and sends them off for trip processing.
 * 					It also will remove any mac's determined to no 
 * 					longer be valid during trip analysis.
 * 
 * ****************************************************************
 */
package threads.processing.mac_address_core;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import objects.LogItem;
import objects.ThreadInformation;
import objects.mac_address.MacAddressData;
import objects.mac_address.UniqueMacAddress;
import statics.Globals;
import threads.LoggerThread;

public class MacAddressUniqueIDProcessor extends Thread
{
	// Indicates how long the thread is to sleep between runs
	private int runInterval = 0;

	// Indicates if the thread is to continue running
	private boolean threadAlive;
	
	// Maintains a list containing all of the MacAddressData, UniqueMacAddresses, and the array of macs to be removed.
	private Vector<MacAddressData> rawMacData;
	private Vector<UniqueMacAddress> uniqueMacData;	
	private Vector<String> removedElements;
	
	// Provides a reference back to the LoggerThread so that log files may be written
	private LoggerThread lt;
	
	// Provides a reference to the MacAddressTravelTimeProcessor for performing trip analysis
	private MacAddressTravelTimeProcessor mattp;

	// Indicates when the thread became active
	private Date activeDate;
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- MacAddressUniqueIDProcessor Construction

	/**
	 * Creates the MacAddressUniqueIDProcessor Object to maintain all of the unique mac addresses and eventually forward them on for further processing and trip identification
	 * 
	 * @param threadName 	- The name of the thread
	 * @param lt			- Reference to the LoggerThread
	 * @param mattp			- Reference to the MacAddressTravelTimeProcessor
	 * @param ri			- Used for determining how long the thread is to sleep for
	 */
	public MacAddressUniqueIDProcessor(String tn, LoggerThread lt, MacAddressTravelTimeProcessor mattp, int ri)
	{
		// Sets the name of the thread for use with the MacAddressUniqueIDProcessor	
		this.setName(tn);
		
		// Sets the LoggerThread reference
		this.lt = lt;

		// Sets the MacAddressTravelTimeProcessor reference
		this.mattp = mattp;
		
		// Sets the run interval of the thread
		this.runInterval = ri * 1000;
		
		// Set the time where this thread started running
		this.activeDate = Calendar.getInstance().getTime();

		// Initializes the arrays to contain the MacAddressData, UniqueMacAddresses, and the array of macs to be removed
		this.rawMacData = new Vector<MacAddressData>();
		this.uniqueMacData = new Vector<UniqueMacAddress>();
		this.removedElements = new Vector<String>();

	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- MacAddressUniqueIDProcessor Threading Methods
	// -- Methods contained here:
	//		-- run()
	//		-- removeMacFromArray()
	// 		-- addData()
	// 		-- debug()
	// 		-- createAndSendLogData()
	// 		-- returnThreadList()
	// 		-- getUniqueMacData()
	/**
	 * Continually runs as long as the thread's threadAlive status is true. This processes all MacAddressData elements and checks to see if they
	 * are unique. If not, they are assigned a new Unique ID, otherwise they are assigned to an existing one.
	 */
	public void run()
	{
		// Since the thread is starting, set its threadAlive status to true
		this.threadAlive = true;
		
		// Begin the core loop of the DataProcessingDaemon thread
		while (this.threadAlive)
		{
			try 
			{
				this.debug("\n\n");
				createAndSendLogData(false, "Info", "\n" + Calendar.getInstance().getTime().toString() + " -- Running " + this.getName());

				// Check and see if the thread has been interrupted. This is a built in Thread method.
				MacAddressUniqueIDProcessor.interrupted();
				
				// Retrieve the start time so that the duration of processing can be analyzed
				long threadStartTime = Calendar.getInstance().getTimeInMillis();

				// Ensure that there are records that need to be re-identified
				if (rawMacData.size() > 0)
				{
					// Retrieve the current size of the rawMacData so that we don't inadvertently throw away valid records
					int currentIndex = rawMacData.size();
	
					this.debug("Setting current Index to " + currentIndex);
					
					createAndSendLogData(false, "Info", "Processing " + currentIndex + "/" + rawMacData.size() + " elements for unique users");
	
					// Loop through all of the records leading up to the index position saved when the thread began processing
					for (int i = 0; i < currentIndex; i++)
					{
						// Retrieve the MacAddressData element at the specified index
						MacAddressData mad = rawMacData.elementAt(i);

						// Indicate that a result was not found.
						boolean found = false;
						
						// Loop through all of the existing UniqueMacAddress records and check for a match
						for (UniqueMacAddress uma : uniqueMacData)
						{
							// If the MacAddressData's Mac Address matches the UniqueMacAddress's Mac Address, then update the uniqueID and increase the hit count for that mac.
							if (mad.getMacAddress().equals(uma.getMacAddress()))
							{
								// Set the UniqueID of the mac Address
								mad.setUniqueID(uma.getUniqueID());
								
								// Indicate we found a result
								found = true;
								
								// Increase the hit count for the UniqueMacAddressElement
								uma.increaseHitCount();

								createAndSendLogData(false, "Info", "UniqueIDProcessing MacAddressData " + mad + " - Found \t\t UniqueMacAddressData: " + uma);

								// Since a match was found, break out of the for-loop and process the next element.
								break;
							}
						}
						
						// If a result was not found, then create a new UniqueMacAddress element
						if (!found)
						{
							// Create a new UniqueMacAddress element with the MacAddressData's Mac Address
							UniqueMacAddress uma = new UniqueMacAddress(mad.getMacAddress(), 1);
							
							// Add the new UniqueMacAddress to the array
							uniqueMacData.add(uma);
							
							// Set the UniqueID for the MacAddressData element before it is sent off for further processing.
							mad.setUniqueID(uma.getUniqueID());
							
							createAndSendLogData(false, "Info", "UniqueIDProcessing MacAddressData " + mad + " - Not Found \t\t UniqueMacAddressData: " + uma);
						}
					}					
					
					createAndSendLogData(false, "Info", "Unique Mac Data array has " + uniqueMacData.size() + " elements");

					// Remove all of the processed MacAddressData elements from the rawMacData array so they are not processed in the future.
					// This also sends them off for further processing.
					for (int i = currentIndex; i > 0; i--)
					{
						//this.mattp.addData(rawMacData.remove(0));
						this.mattp.interrupt();
					}
	
					createAndSendLogData(false, "Info", "Sent " + currentIndex + " bluetooth events to BluetoothTravelTimeProcessor");

					createAndSendLogData(false, "Info", "Removing " + removedElements.size() + " from uniqueMacData");

					int removeStart = this.removedElements.size();
					
					// Loop through all of the MacAddressElements that have been deemed no longer valid by other processing and remove them from uniqueMacData
					for (int i = 0; i < removeStart; i++)
					{
						String s = this.removedElements.elementAt(i);
						
						// Used for debugging purposes to make sure that matches are being found and deleted properly.
						boolean found = false;
						
						// Loop through all UniqueMacAddress data elements and see if a match can be found
						for (UniqueMacAddress ubm : uniqueMacData)
						{
							// If the MacAddress for the UniqueMacAddress matches the string value, then remove it.
							
							if (ubm.getMacAddress().equals(s))
							{				
								// Remove the UniqueMacAddress from the uniqueMacData array
								uniqueMacData.remove(ubm);
								
								// Indicate that a match was found
								found = true;
								
								// Since a match was found, break out of the for-loop and process the next element.
								break;
							}
						}
						
						this.createAndSendLogData(false, "MacDeletion", "\n");
						
						// If a match was not found, then log it as an error
						if (!found)
						{							
							createAndSendLogData(true, "ERROR", "Unable to locate mac address '" + s + "' in uniqueMacData");
						}
					}
					
					for (int i = removeStart; i > 0; i--)
					{
						this.removedElements.remove(0);
					}
				}
				else
				{
					this.createAndSendLogData(false, "Info", "No new events to process.");
				}
				
				// Write to a log file information on the elapsed processing time for the data
				createAndSendLogData(false, "Info", "Elapsed processing time: " + (Calendar.getInstance().getTimeInMillis() - threadStartTime) + " milliseconds. Sleeping for " + runInterval + " milliseconds.");

				// If there are no elements in the rawMacData array, then put the thread to sleep. 
				// Otherwise run the thread again
				if (this.rawMacData.size() == 0)
					MacAddressUniqueIDProcessor.sleep(runInterval);
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
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Removes an existing mac address from the array as it was determined that it was no longer valid and could be removed.
	 * 
	 * @param macAddress
	 */
	public void removeMacFromArray(String macAddress)
	{
		this.createAndSendLogData(false, "Info", String.format("Preparing mac %s to be removed from MacAddressUniqueIDProcessor", macAddress));
		
		// Add the mac address to the "discard" array. This array is iterated through within run()s
		this.removedElements.add(macAddress);
	}
	
	/**
	 * Accepts data from the MacAddressDataProcessor and adds it to the array for processing
	 * 
	 * @param rawData - An array of MacAddressData elements that need to be processed
	 */
	public void addData(Vector<MacAddressData> rawData)
	{
		// Loop through all of the MacAddressData elements and add each record to the array for processing
		for (MacAddressData md : rawData)
		{
			rawMacData.add(md);
		}
	}

	/**
	 * Simple method to easily write debug messages to a Log File
	 * 
	 * @param message
	 */
	private void debug(String message)
	{
		createAndSendLogData(false, "DEBUG", message);
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
			if (type.equals("ERROR"))
				System.err.println(li.getMessage());
			else
				System.out.println(li.getMessage());
	}
	
	/**
	 * Returns the list of device threads currently running. This method is used in conjunction with the GUI interface
	 * 
	 * @return threadList
	 */
	public Vector<ThreadInformation> returnThreadList()
	{
		Vector<ThreadInformation> threads = new Vector<ThreadInformation>();
		
		threads.add(new ThreadInformation("Self", this.getName(), "Data Processor", this.activeDate));

		return threads;
	}
	
	/**
	 * Returns the array of all UniqueMacAddress's stored in the array. This method is used in conjunction with the GUI interface
	 * 
	 * @return
	 */
	public Vector<UniqueMacAddress> getUniqueMacData()
	{
		return uniqueMacData;
	}

}
