/**
 * ****************************************************************
 * File: 			MacAddressTravelTimeProcessorSubThread.java
 * Date Created:  	January 16, 2014
 * Programmer:		Dale Reed
 * 
 * Purpose:			This thread manages all of the current trip 
 * 					information stored within the thread and allows
 * 					for quick and efficient trip calculations
 * 
 * ****************************************************************
 */
package threads.processing.mac_address_core;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import objects.LogItem;
import objects.mac_address.MacAddressTripInformation;
import objects.mac_address.MacAddressTravelTimePair;
import objects.mac_address.MacAddressData;
import statics.Globals;
import threads.LoggerThread;

public class MacAddressTravelTimeProcessorSubThread extends Thread
{
	// Indicates how long the thread is to sleep between runs
	private int sleepInterval = 0;

	// Indicates if the thread is to continue running
	private boolean threadAlive = false;

	// Maintains a list containing all of the trip data that neesds sorting, is currently in use, and elements that need to be deleted
	private Vector<MacAddressData> tripDataToSort;
	private Vector<MacAddressTripInformation> tripData;
	private Vector<String> elementsToRemove;

	// Provides a reference back to the LoggerThread so that log files may be written
	private LoggerThread lt;

	// Provides a reference to the MacAddressSensorSegmentProcessor so that the trip segment can be evaluated
	private MacAddressSensorSegmentProcessor massp;
	
	// Provides a reference back to the MacAddressUniqueIDProcessor so that invalid mac addresses can be deleted
	private MacAddressUniqueIDProcessor mauidp;	
	
	// Indicates when the thread became active
	private Date activeDate;
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- MacAddressTravelTimeProcessorSubThread Construction

	/**
	 * Creates the MacAddressTravelTimeProcessorSubThread so that processing of trips can be done
	 * 
	 * @param threadName
	 * @param massp
	 * @param mauip
	 * @param lt
	 * @param ri
	 */
	public MacAddressTravelTimeProcessorSubThread(String threadName, MacAddressSensorSegmentProcessor massp, MacAddressUniqueIDProcessor mauip, LoggerThread lt, int ri)
	{
		// Sets the name of the thread for use with the MacAddressTravelTimeProcessorSubThread	
		this.setName(threadName);
		
		// Sets the MacAddressSensorSegmentProcessor reference
		this.massp = massp;

		// Sets the LoggerThread reference
		this.lt = lt;
	
		// Sets the MacAddressUniqueIDProcessor reference
		this.mauidp = mauip;
		
		// Sets the run interval of the thread
		this.sleepInterval = ri * 1000;

		
		// Initializes the arrays to contain the various states of the trip information
		tripData = new Vector<MacAddressTripInformation>();
		tripDataToSort = new Vector<MacAddressData>();
		elementsToRemove = new Vector<String>();
		
		// Set the time where this thread started running
		this.activeDate = Calendar.getInstance().getTime();
	}
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- MacAddressTravelTimeProcessorSubThread Threading Methods
	// -- Methods contained here:
	//		-- run()
	// 		-- addDataToProcess()
	// 		-- addElementsToBeRemoved()
	// 		-- error()
	// 		-- debug()
	//		-- createAndSendLogData()
	
	/**
	 * Continually runs as long as the thread's threadAlive status is true. This processes all of the sent mac address information
	 * and continuously looks to see if any of the incoming mac addresses can be a part of a trip. 
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
				MacAddressTravelTimeProcessorSubThread.interrupted();

				long threadStartTime = Calendar.getInstance().getTimeInMillis();

				this.createAndSendLogData(false, "Info", "\n" + Calendar.getInstance().getTime().toString() + " -- Running " + this.getName());
				
				// Ensure there are records in tripDataToSort to be processed
				if (tripDataToSort.size() > 0)
				{	
					this.createAndSendLogData(false, "Info", "Number of elements to sort: " + this.tripDataToSort.size());
					this.createAndSendLogData(false, "Info", "Number of Mac Addresses in system: " + this.tripData.size());
				
					// Retrieve the current size of the tripDataToSort so that we don't inadvertently throw away valid records
					int maxIndex = tripDataToSort.size();
					
					this.debug("Looping through the first " + maxIndex + " records");
					
					// Loop through all of the records leading up to the index position saved when the thread began processing					
					for (int i = 0; i < maxIndex; i++)
					{
						// Retrieve the MacAddressData element at the specified index
						MacAddressData mad = tripDataToSort.elementAt(i);
						
						// Indicate that a trip initially has not been found
						boolean found = false;
						
						// Loop through all current Trip information
						for (MacAddressTripInformation matti : tripData)
						{
							// Check to see if the MacAddressTripInformation's Mac Address and the MacAddressData's Mac Address match
							// If a match is found, check to see if the MacAddress is persisting at a fixed location
							if (matti.getMacAddress().equals(mad.getMacAddress()))
							{
								this.createAndSendLogData(false, "Info", "Found existing trip for " + mad);

								// Loop through all MacAddressData events in the MacAddressTripInformation's event array and see if 
								// that mac was seen previously at the same location
								for (MacAddressData mad2 : matti.returnBluetoothEvents())
								{
									// Check and see if the two nodeID's match
									// If so, indicate the mac was previously found and increase the sessionHitCount before breaking out
									if (mad2.getNodeID() == mad.getNodeID())
									{
										found = true;
										mad2.increaseSessionHitCount();
										
										this.createAndSendLogData(false, "Info", "Increasing Session Counter for " + mad2);

										break;
									}
								}
								
								// If a match was not found, add a new BluetoothEvent to the matti array								
								if (!found)
								{
									this.createAndSendLogData(false, "Info", "Adding New Event for " + mad);

									matti.addBluetoothEvent(mad);
									found = true;
								}
							}
						}
						
						// If a match was not found, create and add a new MacAddressTripInformation object to the tripData array								
						if (!found)
						{
							this.createAndSendLogData(false, "Info", "Created a new trip for " + mad);
							tripData.add(new MacAddressTripInformation(mad.getMacAddress(), mad));
						}
					}
					
					// Remove all of the processed MacAddressData elements from the tripDataToSort array so they are not processed in the future.
					for (int i = 0; i < maxIndex; i++)
					{
						tripDataToSort.remove(0);
					}

					// Analyze all MacAddressTripInformation stored in the tripData array and process any new events that were found
					for (MacAddressTripInformation matte : tripData)
					{
						// Retrieve all sequence events stored in the MacAddressTripInformation event
						Vector<MacAddressData> events = matte.returnBluetoothEvents();
					
						// Loop through all of the events and check to see if the y were previously processed
						for (int i = 0; i < events.size() - 1; i++)
						{
							// Retrieve the first two events at indexes i and i+1
							MacAddressData event1 = events.elementAt(i);
							MacAddressData event2 = events.elementAt(i + 1);
							
							// If both event1 and event2 were NOT sent to the outlier processer, then process them
							if (!event1.wasSentToOutlierProcessor() && !event2.wasSentToOutlierProcessor())
							{
								// Indicate that event1 and event2 were sent to the outlier processer
								event1.sentToOutlierProcessor();
								event2.sentToOutlierProcessor();
								
								// Create a new MacAddressTravelTimePair and send them off to the MacAddressSensorSegmentProcessor for processing
								massp.addData(new MacAddressTravelTimePair(matte.returnBluetoothEvents().elementAt(matte.getBluetoothEventCount() - 2), matte.returnBluetoothEvents().elementAt(matte.getBluetoothEventCount() - 1), this));
								massp.interrupt();
							}
						}
					}
					
					this.createAndSendLogData(false, "Info", "Elapsed processing time: " + (Calendar.getInstance().getTimeInMillis() - threadStartTime) + " milliseconds. Sleeping for " + sleepInterval + " milliseconds.");
				}
				
				// If the MacAddressSensorSegmentProcessor has returned any elements that need to be removed, then remove them from
				// the trip data at this point
				if (this.elementsToRemove.size() > 0)
				{
					// Retrieve the current size of the elementsToRemove array so that we don't get remove elements that are still being added by external threads
					int currentNoOfElements = elementsToRemove.size();
					
					// Create a new array to hold all of the elements that need to be removed.
					Vector<MacAddressTripInformation> toRemove = new Vector<MacAddressTripInformation>();
					
					// Loop through all of the records leading up to the index position saved when the thread began processing
					for (int i = 0; i < currentNoOfElements; i++)
					{
						// Loop through all of the MacAddressTripInformation elements
						for (MacAddressTripInformation mati : tripData)
						{
							// If the element being removed is the same as the macAddress in mati, then add it to the toRemove array
							if (mati.getMacAddress().equals(elementsToRemove.elementAt(i)))
							{
								toRemove.add(mati);
							}
						}
					}
					
					
					int counter = 0;
					this.createAndSendLogData(false, this.getName() + "_Deleted_Trips", String.format("\n"));
					this.createAndSendLogData(false, this.getName() + "_Deleted_Trips", String.format("tripData currently has %d elements", this.tripData.size()));
					this.createAndSendLogData(false, this.getName() + "_Deleted_Trips", String.format("removing %d elements from tripData", toRemove.size()));
					
					// Loop through all elements that need to be removed
					for (MacAddressTripInformation m : toRemove)
					{
						// Remove the MacAddressTripInformation element from tripData
						tripData.remove(m);
						
						// Remove the MacAddressTripInformation's Mac Address from the MacAddressUniqueIDProcessor
						this.mauidp.removeMacFromArray(m.getMacAddress());
						
						// Log the deleted trip information
						this.createAndSendLogData(false, "Deleted_Trips", String.format("%s", m));
						counter++;
					}
					
					this.createAndSendLogData(false, this.getName() + "_Deleted_Trips", String.format("tripData now has %d elements - removed %d", this.tripData.size(), counter));

					this.createAndSendLogData(false, this.getName() + "_Deleted_Trips", "Removed " + counter + " trips from " + this.getName());
					
					// Loop through all the Mac Addresses that were removed in the deletion process
					for (int i = 0; i < currentNoOfElements; i++)
					{
						elementsToRemove.remove(0);
					}
				}
				
				this.debug("Elapsed processing time: " + (Calendar.getInstance().getTimeInMillis() - threadStartTime) + " milliseconds. Sleeping for " + sleepInterval + " milliseconds.");

				// If there are elements remaining the tripDataToSort array, then run the thread again and attemp to sleep later
				if (this.tripDataToSort.size() == 0)
					MacAddressTravelTimeProcessorSubThread.sleep(sleepInterval);
			}
			catch (Exception e)
			{
				if (e.getMessage().equalsIgnoreCase(Globals.SLEEP_INTERRUPTED))
				{

				}
				else
				{
					e.printStackTrace();
					this.error(e.getMessage());
				}	
			}
		}
	}
	
	/**
	 *  Adds a Mac Address from the MacAddressTravelTimeProcessor to the sub thread for processing
	 *  
	 * @param mad
	 */
	public void addDataToProcess(MacAddressData mad)
	{
		tripDataToSort.add(mad);		
	}
	
	/**
	 * Prepares all invalid mac addresses for removal
	 */
	public void addElementsToBeRemoved(String mac)
	{
		this.elementsToRemove.add(mac);
	}
		
	/**
	 * Simple method to easily write error messages to a Log File
	 * 
	 * @param message
	 */
	private void error(String message)
	{
		this.createAndSendLogData(false, "ERROR", message);
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

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- MacAddressTravelTimeProcessorSubThread Getters
	
	/**
	 * Returns the date for when this thread became active.
	 * 
	 * @return activeDate - The date the thread became active
	 */
	public Date getActiveDate()
	{
		return this.activeDate;
	}
	
	/**
	 * Returns a string representation of the thread 
	 */
	public String toString()
	{
		return String.format("[Name: %s, Current Trips: %d]", this.getName(), this.tripData.size());
	}
	
	/**
	 * Returns the array of macData for the sub-thread
	 * 
	 * @return tripData.
	 */
	public Vector<MacAddressTripInformation> getMacData()
	{
		return this.tripData;
	}
}