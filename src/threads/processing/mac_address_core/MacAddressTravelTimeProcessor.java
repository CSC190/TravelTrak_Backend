/**
 * ****************************************************************
 * File: 			MacAddressTravelTimeProcessor.java
 * Date Created:  	January 16, 2014
 * Programmer:		Dale Reed
 * 
 * Purpose:			This thread is essentially a daemon thread to 
 * 					manage all of the MacAddressTravelTimeProcessorSubThread
 * 					threads and passes mac addresses to the 
 * 					appropriate sub thread for more processing.
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
import objects.mac_address.MacAddressTripInformation;
import statics.Globals;
import threads.LoggerThread;

public class MacAddressTravelTimeProcessor extends Thread
{
	// Indicates how long the thread is to sleep between runs
	private int runInterval = 0;

	// Indicates if the thread is to continue running
	private boolean threadAlive = false;

	// Used for temporarily storing macAddressData so it can be forwarded to the appropriate sub thread
	private Vector<MacAddressData> macAddressData;	
	
	// 8 sub-threads that will be used for processing mac addresses
	private MacAddressTravelTimeProcessorSubThread maProcessor01; 	// Processes Macs starting with 0 or 1
	private MacAddressTravelTimeProcessorSubThread maProcessor23;	// Processes Macs starting with 2 or 3
	private MacAddressTravelTimeProcessorSubThread maProcessor45;	// Processes Macs starting with 4 or 5
	private MacAddressTravelTimeProcessorSubThread maProcessor67;	// Processes Macs starting with 6 or 7
	private MacAddressTravelTimeProcessorSubThread maProcessor89;	// Processes Macs starting with 8 or 9
	private MacAddressTravelTimeProcessorSubThread maProcessorAB;	// Processes Macs starting with A or B
	private MacAddressTravelTimeProcessorSubThread maProcessorCD;	// Processes Macs starting with C or D
	private MacAddressTravelTimeProcessorSubThread maProcessorEF;	// Processes Macs starting with E or F

	// Indicates when the thread became active
	private Date activeDate; 
	
	// Provides a reference back to the LoggerThread so that log files may be written
	private LoggerThread lt;
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- MacAddressTravelTimeProcessor Construction

	/**
	 * Creates the MacAddressTravelTimeProcessor thread to manage all subthreads and the data being sent to them.
	 * 
	 * @param threadName 	- The name of the thread
	 * @param lt			- Reference to the LoggerThread
	 * @param ri			- Used for determining how long the thread is to sleep for
	 */
	public MacAddressTravelTimeProcessor(String threadName, LoggerThread lt, int ri)
	{
		// Sets the name of the thread for use with the DataProcessingDaemon	
		this.setName(threadName);

		// Sets the LoggerThread reference
		this.lt = lt;

		// Sets the run interval of the thread
		this.runInterval = ri * 1000;
	
		// Allocates the array to hold the data that needs to be transferred.
		macAddressData = new Vector<MacAddressData>();
		
		// Set the time where this thread started running
		this.activeDate = Calendar.getInstance().getTime();
	}
	
	/**
	 * Called from Startup, this initializes all of the sub-threads with any references that they need to run
	 * 
	 * @param massp	- Reference to the MacAddressSensorSegmentProcessor
	 * @param mauip - Reference to the MacAddressUniqueIDProcessor
	 */
	public void initializeSubThreads(MacAddressSensorSegmentProcessor massp, MacAddressUniqueIDProcessor mauip)
	{
		// Create the 8 Processing threads for travel time analysis.
		maProcessor01 = new MacAddressTravelTimeProcessorSubThread("MAProcessor01", massp, mauip, lt, 10);
		maProcessor23 = new MacAddressTravelTimeProcessorSubThread("MAProcessor23", massp, mauip, lt, 10);
		maProcessor45 = new MacAddressTravelTimeProcessorSubThread("MAProcessor45", massp, mauip, lt, 10);
		maProcessor67 = new MacAddressTravelTimeProcessorSubThread("MAProcessor67", massp, mauip, lt, 10);
		maProcessor89 = new MacAddressTravelTimeProcessorSubThread("MAProcessor89", massp, mauip, lt, 10);
		maProcessorAB = new MacAddressTravelTimeProcessorSubThread("MAProcessorAB", massp, mauip, lt, 10);
		maProcessorCD = new MacAddressTravelTimeProcessorSubThread("MAProcessorCD", massp, mauip, lt, 10);
		maProcessorEF = new MacAddressTravelTimeProcessorSubThread("MAProcessorEF", massp, mauip, lt, 10);
	}
	

	

	
	public void run()
	{		
		// Start up all of the MacAddressProcessor Sub Threads
		maProcessor01.start(); maProcessor23.start(); maProcessor45.start(); maProcessor67.start(); maProcessor89.start(); maProcessorAB.start(); maProcessorCD.start(); maProcessorEF.start();
		
		// Since the thread is starting, set its threadAlive status to true
		this.threadAlive = true;
		
		// Begin the core loop of the DataProcessingDaemon thread
		while (this.threadAlive)
		{
			try
			{
				// Check and see if the thread has been interrupted. This is a built in Thread method.
				MacAddressTravelTimeProcessor.interrupted();
				
				// Ensure there are records in macAddressData to be forwarded for processing
				if (macAddressData.size() > 0)
				{
					this.debug("\n");
					this.createAndSendLogData(false, "Info", "\n" + Calendar.getInstance().getTime().toString() + " -- Running " + this.getName());
					
					long threadStartTime = Calendar.getInstance().getTimeInMillis();
		
					// Used in debugging to make sure that the sub threads receive the correct number of elements
					int bt01Count = 0, bt23Count = 0, bt45Count = 0, bt67Count = 0, bt89Count = 0, btABCount = 0, btCDCount = 0, btEFCount = 0;
		
					this.createAndSendLogData(false, "Info", "Sending " + macAddressData.size() + " elements to their respective processing threads");
					
					// Retrieve the current size of the rawMacData so that we don't inadvertently throw away valid records
					int maxIndex = macAddressData.size();
					
					this.debug("Looping through the first " + maxIndex + " records");
					
					// Loop through all of the records leading up to the index position saved when the thread began processing
					for (int i = 0; i < maxIndex; i++)
					{
						// Retrieve the MacAddressData element at the specified index
						MacAddressData mad = macAddressData.elementAt(i);
						
						// Extract the first byte of the MacAddress so the MacAddressData can be forwarded to the appropriate sub-thread
						String firstByte = mad.getMacAddress().substring(0, 1);
							
						if (firstByte.equals("0") || firstByte.equals("1"))
						{
							maProcessor01.addDataToProcess(mad);
							maProcessor01.interrupt();
							this.createAndSendLogData(false, "Info", String.format("Sending %s to %s", mad, maProcessor01));
							bt01Count++;
						}
						else if (firstByte.equals("2") || firstByte.equals("3"))
						{
							maProcessor23.addDataToProcess(mad);
							maProcessor23.interrupt();
							this.createAndSendLogData(false, "Info", String.format("Sending %s to %s", mad, maProcessor23));
							bt23Count++;
						}
						else if (firstByte.equals("4") || firstByte.equals("5"))
						{
							maProcessor45.addDataToProcess(mad);
							maProcessor45.interrupt();
							this.createAndSendLogData(false, "Info", String.format("Sending %s to %s", mad, maProcessor45));
							bt45Count++;
						}
						else if (firstByte.equals("6") || firstByte.equals("7"))
						{
							maProcessor67.addDataToProcess(mad);
							maProcessor67.interrupt();
							this.createAndSendLogData(false, "Info", String.format("Sending %s to %s", mad, maProcessor67));
							bt67Count++;
						}
						else if (firstByte.equals("8") || firstByte.equals("9"))
						{
							maProcessor89.addDataToProcess(mad);
							maProcessor89.interrupt();
							this.createAndSendLogData(false, "Info", String.format("Sending %s to %s", mad, maProcessor89));
							bt89Count++;
						}
						else if (firstByte.equals("A") || firstByte.equals("B"))
						{
							maProcessorAB.addDataToProcess(mad);
							maProcessorAB.interrupt();
							this.createAndSendLogData(false, "Info", String.format("Sending %s to %s", mad, maProcessorAB));
							btABCount++;
						}
						else if (firstByte.equals("C") || firstByte.equals("D"))
						{
							maProcessorCD.addDataToProcess(mad);
							maProcessorCD.interrupt();
							this.createAndSendLogData(false, "Info", String.format("Sending %s to %s", mad, maProcessorCD));
							btCDCount++;
						}
						else if (firstByte.equals("E") || firstByte.equals("F"))
						{
							maProcessorEF.addDataToProcess(mad);
							maProcessorEF.interrupt();
							this.createAndSendLogData(false, "Info", String.format("Sending %s to %s", mad, maProcessorEF));
							btEFCount++;
						}
						else
						{
							this.debug("Unable to send " + firstByte + " with mac addres " + mad.getMacAddress() + " to a MAProcessor Thread");
						}
					}
					
					// Log how many were sent to the sub-threads for further processing
					this.debug(String.format("Elements sent to Mac Address Sub Processors -- BT01: %5d  -  BT23: %5d  -  BT45: %5d  -  BT67: %5d  -  BT89: %5d  -  BTAB: %5d  -  BTCD: %5d  -  BTEF: %5d", 	bt01Count, bt23Count, bt45Count, bt67Count, bt89Count, btABCount, btCDCount, btEFCount));
					
					this.debug("macAddressData element size before removing data: " + macAddressData.size());

					// Remove all of the processed MacAddressData elements from the macAddressData array so they are not processed in the future.
					for (int i = 0; i < maxIndex; i++)
					{
						macAddressData.remove(0);
					}
					
					// Log how many address still need to be processed.
					this.debug("Final macAddressData element size after remove: " + macAddressData.size());

					// Write to a log file information on the elapsed processing time for the data
					this.createAndSendLogData(false, "Info", "Elapsed processing time: " + (Calendar.getInstance().getTimeInMillis() - threadStartTime) + " milliseconds. Sleeping for " + runInterval + " milliseconds.");
				}
				else
				{
					// Write to a log file that there were no elements to be processed.
					this.createAndSendLogData(false, "Info", "No Mac Addresses available for processing");
				}
	
				
				// If there are no elements in the rawMacData array, then put the thread to sleep. 
				// Otherwise run the thread again
				if (this.macAddressData.size() == 0)
				{
					MacAddressUniqueIDProcessor.sleep(runInterval);
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
	
	
	/**
	 * Accepts data from the MacAddressUniqueIDProcessor and adds it to the array for processing
	 * 
	 * @param mad - A MacAddressData element that needs to be processed.
	 */
	public void addData(MacAddressData mad)
	{
		this.createAndSendLogData(false, "AddedData", String.format("Added '%s' to MacAddressTravelTimeProcessor for processing", mad));
		
		// Add the 'mad' element to the macAddressData for processing.
		macAddressData.add(mad);
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
		
		threads.add(new ThreadInformation("Self", this.getName(), "Mac Address Processor Daemon", this.activeDate));
		threads.add(new ThreadInformation(this.getName(), maProcessor01.getName(), "Mac Address Processor", maProcessor01.getActiveDate()));
		threads.add(new ThreadInformation(this.getName(), maProcessor23.getName(), "Mac Address Processor", maProcessor23.getActiveDate()));
		threads.add(new ThreadInformation(this.getName(), maProcessor45.getName(), "Mac Address Processor", maProcessor45.getActiveDate()));
		threads.add(new ThreadInformation(this.getName(), maProcessor67.getName(), "Mac Address Processor", maProcessor67.getActiveDate()));
		threads.add(new ThreadInformation(this.getName(), maProcessor89.getName(), "Mac Address Processor", maProcessor89.getActiveDate()));
		threads.add(new ThreadInformation(this.getName(), maProcessorAB.getName(), "Mac Address Processor", maProcessorAB.getActiveDate()));
		threads.add(new ThreadInformation(this.getName(), maProcessorCD.getName(), "Mac Address Processor", maProcessorCD.getActiveDate()));
		threads.add(new ThreadInformation(this.getName(), maProcessorEF.getName(), "Mac Address Processor", maProcessorEF.getActiveDate()));

		return threads;
	}
	
	/**
	 * Returns the information pertaining to a particular thread. This method is used in conjunction with the GUI interface.
	 * 
	 * @return macData
	 */	
	public Vector<MacAddressTripInformation> returnThreadData(String threadName)
	{
		if (maProcessor01.getName().equals(threadName))
		{
			return maProcessor01.getMacData();
		}
		else if (maProcessor23.getName().equals(threadName))
		{
			return maProcessorEF.getMacData();
		}
		else if (maProcessor45.getName().equals(threadName))
		{
			return maProcessor45.getMacData();
		}
		else if (maProcessor67.getName().equals(threadName))
		{
			return maProcessor67.getMacData();
		}
		else if (maProcessor89.getName().equals(threadName))
		{
			return maProcessor89.getMacData();
		}
		else if (maProcessorAB.getName().equals(threadName))
		{
			return maProcessorAB.getMacData();
		}
		else if (maProcessorCD.getName().equals(threadName))
		{
			return maProcessorCD.getMacData();
		}
		else if (maProcessorEF.getName().equals(threadName))
		{
			return maProcessorEF.getMacData();
		}
		
		return null;
	}
}