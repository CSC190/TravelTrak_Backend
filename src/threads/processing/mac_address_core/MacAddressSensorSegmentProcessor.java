/**
 * ****************************************************************
 * File: 			MacAddressSensorSegmentProcessor.java
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Vector;

import objects.Database;
import objects.LogItem;
import objects.mac_address.MacAddressSensorSegment;
import objects.mac_address.MacAddressTravelTimePair;
import statics.DatabaseStatics;
import statics.Globals;
import statics.PreparedStatementStatics;
import threads.LoggerThread;

public class MacAddressSensorSegmentProcessor extends Thread
{
	// Indicates if the thread is to continue running
	private boolean threadAlive = false;
	
	// Used for storing information on the link segments and for temporary storage of travel time pairs that need to be analyzed
	private Vector<MacAddressTravelTimePair> travelTimePairs;
	private Vector<MacAddressSensorSegment> linkSegments;
	
	// Provides a reference to the database for reading/writing data for permanent storage
	private Database db;
	
	// Provides a reference back to the LoggerThread so that log files may be written
	private LoggerThread lt;
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- MacAddressSensorSegmentProcessor Construction
	
	/**
	 * Creates the MacAddressSensorSegmentProcessor to maintain all of the links and their current travel times based on data coming in
	 * 
	 * @param threadName 	- The name of the thread
	 * @param lt			- Reference to the LoggerThread
	 * @param sleepTime		- Used for determining how long the thread is to sleep for
	 * @param db			- Used for referencing the database for writing/reading data
	 */
	public MacAddressSensorSegmentProcessor(String threadName, LoggerThread lt, int sleepTime, Database db)
	{
		// Sets the name of the thread for use with the MacAddressUniqueIDProcessor	
		this.setName(threadName);
		
		// Sets the LoggerThread reference
		this.lt = lt;
		
		// Sets the Database reference
		this.db = db;		

		
		// Initializes the arrays to contain the Travel Time Pairs and the Link Segments
		travelTimePairs = new Vector<MacAddressTravelTimePair>();
		linkSegments = new Vector<MacAddressSensorSegment>();		
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- MacAddressUniqueIDProcessor Threading Methods
	// -- Methods contained here:
	//		-- run()
	//		-- sendNotificationBackToSender()
	// 		-- addNewAdjacentPair()
	// 		-- addData()
	//		-- debug()
	//		-- createAndSendLogData

	/**
	 * Continually runs as long as the thread's threadAlive status is true. This checks all of the MacAddressTravelTimePair's as they
	 * come in and verify that they are valid. If not they are flagged as bad and discarded
	 */
	public void run()
	{
		// Since the thread is starting, set its threadAlive status to true
		this.threadAlive = true;
		
		try 
		{
			// Scan for any new sensor pairs that can have travel times
			// Query Database for initial sensor listing
			PreparedStatement initStatement = this.db.getConnection().prepareStatement(PreparedStatementStatics.GET_MAC_SENSOR_SEGMENTS);
			//ResultSet rs = initStatement.executeQuery();
			
			// Loop through all of the results in teh result set
			/*while (rs.next())
			{
				int node1 = rs.getInt(DatabaseStatics.MAC_ADJACENT_NODE_NODE_1);
				int node2 = rs.getInt(DatabaseStatics.MAC_ADJACENT_NODE_NODE_2);
				float distance = rs.getFloat(DatabaseStatics.MAC_ADJACENT_NODE_DISTANCE);
				int baseTime = rs.getInt(DatabaseStatics.MAC_ADJACENT_BASE_TRAVEL_TIME);
				
				boolean found = false;

				// Loop through all of the current link segments in the array, and check for an existing match.
				for (MacAddressSensorSegment mt : linkSegments)
				{
					// If a match is found, then indicate it was found and break out of the loop.
					if (mt.getSensor1ID() == node1 && mt.getSensor2ID() == node2)
					{
						found = true;
						break;
					}
				}
				
				// If no match was found, then create a new MacAddressSensorSegment object and add it to the linkSegments.
				if (!found)
				{
					MacAddressSensorSegment mass = new MacAddressSensorSegment(lt, node1, node2, distance, baseTime);
					linkSegments.add(mass);
				}
			}*/
			
			// Close the ResultSet & PreparedStatement. Failure to do so will result in memory leaks.
			//rs.close();
			initStatement.close();

			// Deallocate the ResultSet & PreparedStatement
			//rs = null;
			initStatement = null;
		} 
		catch (SQLException e1) 
		{
			e1.printStackTrace();
		}
		
		// Begin the core loop of the DataProcessingDaemon thread
		while (this.threadAlive)
		{
			try 
			{
				// Check and see if the thread has been interrupted. This is a built in Thread method.
				MacAddressSensorSegmentProcessor.interrupted();
				
				this.debug("\n\n");
				createAndSendLogData(false, "Info", "\n" + Calendar.getInstance().getTime().toString() + " -- Running " + this.getName());
				
				// Ensure that there are records that need to be analyzed
				if (travelTimePairs.size() > 0)
				{
					// Create a PreparedStatement for inserting records into the database in batch mode
					PreparedStatement ps = this.db.getConnection().prepareStatement(PreparedStatementStatics.INSERT_NEW_TRAVEL_TIMES);
					
					// Only analyze the what is current at the time of this line of code. 
					// By doing this, it helps prevent ConcurrentModificationErrors later on by limiting how many records are being analyzed
					int currentSize = travelTimePairs.size();
					this.debug(String.format("%s has %d elements to verify", this.getName(), currentSize));
					
					// Loop through the first x elements, where x is 'currentSize' calculated above
					for (int i = 0; i < currentSize; i++)
					{
						// Retrieve the MacAddressTravelTimePair at index i
						MacAddressTravelTimePair mattp = travelTimePairs.elementAt(i);
						
						// Retrieve the two node ID's of the mattp.
						int node1 = mattp.getEvent1().getNodeID();
						int node2 = mattp.getEvent2().getNodeID();
											
						boolean found = false;
						
						// Loop through all of the MacAddressSensorSegment elements and attemp to find one with the matching pairs.
						for (MacAddressSensorSegment mass : linkSegments)
						{
							// Retrieve the two sensor ID's of the mass
							int sensor1ID = mass.getSensor1ID();
							int sensor2ID = mass.getSensor2ID();
							
							// If the sensor's match correctly in either direction, then perform the analysis on that segment.
							if ((sensor1ID == node1 && sensor2ID == node2) || (sensor1ID == node2 && sensor2ID == node1))
							{
								this.debug(mass.toString());
								
								found = true;
								
								// Check to see if the mattp matches the mass's direction 1
								if (sensor1ID == node1 && sensor2ID == node2)
								{
//FIXME: Both this block and the next block need to be evaluated and confirmed function when more than 2 sensors exist out in the field.									
//									mattp = mass.calculateDirection1(mattp);
//									sendNotificationBackToSender(mattp, mattp.isValid());
//									
//									this.debug(String.format("%s -- %s", mattp, ((mattp.isValid()) ? "VALID" : "INVALID")));
								}
								// Otherwise it must be the other direction
								else
								{
//									mattp = mass.calculateDirection2(mattp);
//									sendNotificationBackToSender(mattp, mattp.isValid());
//									
//									this.debug(String.format("%s -- %s", mattp, ((mattp.isValid()) ? "VALID" : "INVALID")));	
								}
							
								// Set the appropriate values for the PreparedStatment and add it for a batch mode processing
								ps.setInt(1, mattp.getEvent1().getNodeID());
								ps.setInt(2, mattp.getEvent2().getNodeID());
								ps.setString(3, mattp.getEvent1().getMacAddress());
								ps.setInt(4, mattp.getDuration());
								ps.setLong(5, mattp.getTimeStamp());
								ps.setDouble(6, mattp.getUpperBound());
								ps.setDouble(7, mattp.getLowerBound());
								ps.setBoolean(8, mattp.isValid());
								
								ps.addBatch();
							}
						}
						
						// If no match was found, then indicate that there was an error.
						if (!found)
						{
							this.createAndSendLogData(false, "NOTICE", "Failed to find segment information for nodes " + node1 + " -> " + node2);
						}	
					}
					
					// Loop through all of the records we have already analyzed and remove them from the system.
					for (int i = 0; i < currentSize; i++)
					{
						travelTimePairs.remove(0);
					}
					
					// Execute the PreparedStatement in batch mode, and then close and deallocate the PreparedStatement.
					// Failure to close the PreparedStatement will result in memory leaks.
					ps.executeBatch();
					ps.close();
					ps = null;

				}
				else
				{
					this.createAndSendLogData(false, "Info", "No travel Time Pairs to analyze.\n");
				}
				
				// If there are no elements in the travelTimePairs array, then put the thread to sleep. 
				// Otherwise run the thread again
				if (travelTimePairs.size() == 0)
					MacAddressSensorSegmentProcessor.sleep(15000);
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
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Notifies the sender of the MacAddressTravelTimePair that the results of the segment have been calculated
	 * 
	 * @param matp 		- MacAddressTravelTimePair that has been processed
	 * @param result	- The result of the MacAddressTravelTimePair processing
	 */
	public void sendNotificationBackToSender(MacAddressTravelTimePair matp, boolean result)
	{
		// If the result is not valid, then let the sender know that this specific mac address needs to be removed
		if (!result)
		{
			matp.getSender().addElementsToBeRemoved(matp.getEvent1().getMacAddress());
			
			// Write to a log file indicating that the log file is being deleted.
			this.createAndSendLogData(false, "DELETED", "Deleting " + matp + " from system....");
		}
	}
	
	/**
	 * Adds a new MacAddressSensorSegment to the array for Trip Segment Processing
	 * @param mass
	 */
	public void addNewAdjacentPair(MacAddressSensorSegment mass)
	{
		linkSegments.add(mass);
	}
	
	/**
	 * Accepts data from the MacAddressTravelTimeProcessorSubThread and adds it to the array for processing
	 * 
	 * @param mattp - A MacAddressTravelTimePair to be analyzed
	 */
	public void addData(MacAddressTravelTimePair mattp)
	{
		travelTimePairs.add(mattp);
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
}
