/**
 * ****************************************************************
 * File: 			DeviceDaemon.java
 * Date Created:  	January 13, 2014
 * Programmer:		Dale Reed
 * 
 * Purpose:			This thread manages all of the devices and their
 * 					corresponding threads to ensure they are running
 * 					properly. It periodically re-scans the database
 * 					for any new devices that may have been added
 * 					to the system, after which it starts up a new
 * 					thread for that particular device.
 * 
 * ****************************************************************
 */
package threads.devices;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import objects.Database;
import objects.LogItem;
import objects.ThreadInformation;
import objects.mac_address.MacAddressSensorSegment;
import statics.DatabaseStatics;
import statics.Globals;
import statics.PreparedStatementStatics;
import threads.LoggerThread;
import threads.QueryDeviceThread;
//import threads.devices.acyclica.AcyclicaDeviceThread;
//import threads.devices.bluetooth.BluetoothDeviceThread;
import threads.devices.c1.C1DeviceThread;
import threads.processing.DataProcessingDaemon;
import threads.processing.mac_address_core.MacAddressSensorSegmentProcessor;


public class DeviceDaemon extends Thread
{
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- DeviceDaemon Variable Declarations
	
	// Provides access to the database for querying changes that may occur for any devices
	private Database database;
	
	// Indicates if the thread is to continue running
	private boolean threadAlive;
	
	// Maintains a list of all running device threads.
	private Vector<DeviceThread> activeThreads;
	
	// Used for querying the database once an hour to see if any old devices that may have went
	// off-line automatically have since came back online without any notification.
	private int currentHour = -1;
	
	// Indicates when the thread became active
	private Date activeDate;
	
	// Provides a reference back to the DataProcessingDaemon so that data can be forwarded from the devices for processing.
	private DataProcessingDaemon dpd;
	
	// Provides a reference back to the LoggerThread so that log files may be written
	private LoggerThread lt;
	
	// Provides a reference back to the MacAddressSensorSegmentProcessor so that as new devices come online, Sensor Segments can be added to the
	// processor for use with calculating valid travel time pairs
	private MacAddressSensorSegmentProcessor massp;

	// Used with the Acyclia system as their data is all web based, and can be queried as quickly as desired. 
	// TODO: This may not be used, as it doesn't really provide any benefit in a real-time processing environment.
	private boolean fastQuery = false;


	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- DeviceDaemon Construction
	
	/**
	 * Creates the DeviceDaemon Object to initialize and start up the daemon for running all devices.
	 */
	public DeviceDaemon(DataProcessingDaemon dpd, MacAddressSensorSegmentProcessor massp, LoggerThread lt, boolean fastQuery) 
	{
		// Sets the name of the thread for use with the LoggerThread
		this.setName("Device Daemon Thread");
		
		// Set the DataProcessingDaemon link
		this.dpd = dpd;
		
		// Set the LoggerThread link
		this.lt = lt;
		
		// Set the MacAddressSensorSegmentProcessor link
		this.massp = massp;
		
		// Creates a new array to hold all of the currently running DeviceThreads
		activeThreads = new Vector<DeviceThread>();
		
		// Creates a new Database for use with querying records for processing
		database = new Database();
		
		// Set the time where this thread started running
		this.activeDate = Calendar.getInstance().getTime();
		
		// Set the fastQuery command for use with Acyclias
		this.fastQuery = fastQuery;
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Device Daemon Threading Methods 
	// -- Methods contained here:
	// 		-- run()
	// 		-- removeThreadFromList()
	//		-- checkForActiveDevices()
	// 		-- queryInactiveDevices()
	// 		-- checkForNewDevices()
	// 		-- createAndSengLogData()
	// 		-- returnThreadList()
	// 		-- initializeShutdownProcess()
	// 		-- shutdownThread
	// TODO: Some of these functions (like checkForActiveDevices/checkforNewDevices could be combined into one method, simplifying the program flow

	/**
	 * Continually runs as long as the thread's threadAlive status is true. This handles the checking to see if any new devices have came online
	 * recently, and if so starts them up. It also scans for old devices to see if they have recently came back online.
	 */
	public void run()
	{
		// Since the thread is starting, set its threadAlive status to true
		this.threadAlive = true;

		// Query the database and return all active devices that have been found upon starting up the program.
		this.checkForActiveDevices();
				
		// Begin the core loop of the DeviceDaemon thread
		while (this.threadAlive)
		{
			try 
			{
				// Check to see if the stored currentHour does not match the Hour returned from the System Calendar
				// If it does not, then query for all inactive devices.
				// Otherwise do nothing.
				if (currentHour != Calendar.getInstance().get(Calendar.HOUR))
				{
					// Query all inactive devices and update the currentHour variable
					this.queryInactiveDevices();
					currentHour = Calendar.getInstance().get(Calendar.HOUR);
				}

				// Chec for any new devies that may have been added recently
				this.checkForNewDevices();
				
				// Have the thread sleep for a pre-defined interval. Since this thread is not hugely critical in the transfer of data, 
				// it can have a much longer duration between runs
				DeviceDaemon.sleep(Globals.DEVICE_DAEMON_SLEEP_TIME);
			} 
			// Catch any errors associated with the thread sleeping
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Receives a notification from one of the device threads indicating that the thread is shutting down and needs to be removed
	 * from the list of active threads and then shut down
	 * 
	 * @param uid - The thread that is to be removed
	 */
	public void removeThreadFromList(String uid)
	{
		// Loop through all of the active DeviceThread elements and find the one that is requesting to be shutdown
		for (DeviceThread dt : activeThreads)
		{
			// If the DeviceThread has  matching uniqueID, then being shutting it down
			// Otherwise loop through again and check the next thread
			if (dt.getUniqueID().equals(uid))
			{
				// Create a log file to indicate the thread has been removed
				createAndSendLogData("Thread Shutdown", "Removing thread " + uid + " from Device Daemon");

				// Remove the thread from the activeThreads array
				activeThreads.remove(dt);
				
				try 
				{
					// Update the status of the device thread to indicate that it has gone offline
					PreparedStatement ps = database.getConnection().prepareStatement(PreparedStatementStatics.DEVICE_STATUS_CHANGED);
					ps.setInt(1, 0);
					ps.setInt(2, dt.getDeviceID());
					
					// Execute the MySQL Statement on the database. If the query fails, then log it to the database
					if (!ps.execute())
					{
						createAndSendLogData("SQL Error", "Failed to update device status in database. MySQL Query: " + ps.toString());
					}
					
					// Close the PreparedStatment and deallocate it. Failing to do so will cause memory leaks.
					ps.close();
					ps = null;
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
				
				// Finally shutdown the DeviceThread and then break out of the loop as there is nothing left to process at this point.
				dt.shutdownThread();
				break;
			}
		}
	}

	/**
	 * Checks the database for any active devices upon startup and adds them to the activeThreads list
	 */
	private void checkForActiveDevices()
	{
		try 
		{
			// Retrieve all active devices from the database so that the appropriate device thread may be started up
			PreparedStatement ps = database.getConnection().prepareStatement(PreparedStatementStatics.DEVICE_ACTIVE_QUERY);
		
			// Store the results of the query into a ResultSet for processing
			ResultSet query = ps.executeQuery();

			// Ensure that there is at least 1 record that has been returned from the database.
			if (database.countResults(query) > 0)
			{
				// Loop through all of the returned elements from the database
				while (query.next())
				{
					// Return the Device's ID, Type, Primary & Secondary IP Ports, and IP Address
					int deviceID = query.getInt(DatabaseStatics.DEVICE_ID);
					String deviceType = query.getString(DatabaseStatics.DEVICE_TYPE);
					String ip = query.getString(DatabaseStatics.DEVICE_IP);
					int port1 = query.getInt(DatabaseStatics.DEVICE_PRIMARY_PORT);
					int port2 = query.getInt(DatabaseStatics.DEVICE_SECONDARY_PORT);
					
					//int port1 = query.getInt(DatabaseStatics.DEVICE_PORT_1);
					//int port2 = query.getInt(DatabaseStatics.DEVICE_PORT_2);
					//int port3 = query.getInt(DatabaseStatics.DEVICE_PORT_3);
					//int port4 = query.getInt(DatabaseStatics.DEVICE_PORT_4);
					
					// If we are running this on a test program (indicated by 'true'), then we will be using the Secondary Port for the socket communication
					boolean testDevice = query.getBoolean(DatabaseStatics.DEVICE_TEST);

					// If the device type is BT (indicating Bluetooth), then start up its thread
					if (deviceType.equals("BT"))
					{
						// Set the UID of the device to match the type, as well as its IP and Port information
						String uid = deviceType + "_" + ip + ":" + port1;
						
						// Create the new Bluetooth Thread Object
						//BluetoothDeviceThread bdt = new BluetoothDeviceThread(this.dpd, this.lt, deviceID, uid, ip, (!testDevice) ? port1 : port2, this);
						
						//*****IN PROGRESS working to make 4 active ports*****\\
						//BluetoothDeviceThread bdt = new BluetoothDeviceThread(this.dpd, this.lt, deviceID, uid, ip, port1, port2, port3, port4, this);

						// Add the newly created Bluetooth Thread object to the activeThreads array
						//activeThreads.add(bdt);
						
						// Start the Bluetooth thread
						//bdt.start();
					}
					// If the device type is C1 (indicating a C1 Reader), then start up its thread
					else if (deviceType.equals("C1"))
					{
						// Set the UID of the device to match the type, as well as its IP and Port information
						// in fat32 : colon not recognized changed to @ symbol instead
						String uid = deviceType + "_" + ip + ":" + port1;
						
						// Retrieve the number of records that can be stored in the Device's buffer.
						// -- More information on this and what it does is in @C1DeviceThread
						//int bsize = query.getInt(DatabaseStatics.DEVICE_BUFFER_SIZE);
						int bsize = 0;
						
								
						// Create the new C1 Device Thread Object
						C1DeviceThread cdt = new C1DeviceThread(this.dpd, this.lt, deviceID, uid, ip, port1, bsize, this, testDevice, database);
						//C1DeviceThread cdt = new C1DeviceThread(this.dpd, this.lt, deviceID, uid, ip, port1, bsize, this, testDevice);
						
						// Add the newly created C1 Device Thread object to the activeThreads array
						activeThreads.add(cdt);
						
						// Start the C1 thread
						cdt.start();
					}
					// If the device type is AW (indicating Acyclia WIFI), then start up its thread
					else if (deviceType.equals("AW"))
					{
						// Retrieve the device's Serial ID and API key's from the database
						//int deviceSerialID = query.getInt(DatabaseStatics.DEVICE_SERIAL_ID);
						//String deviceAPIKey = query.getString(DatabaseStatics.DEVICE_API_KEY);
						
						// Set the UID of the device to match the type, as well as its SerialID
						//String uid = deviceType + "_" + deviceSerialID;

						// Create the new Acyclica Device Thread Object
						//AcyclicaDeviceThread adt = new AcyclicaDeviceThread(this.dpd, this.lt, deviceID, uid, Globals.ACYCLICA_DEVICE_URL, deviceSerialID, this, deviceAPIKey, fastQuery);
						
						// Add the newly created Acyclica Device Thread object to the activeThreads array
						//activeThreads.add(adt);
						
						// Start the Acyclica thread
						//adt.start();
					}
				}
			}
			
			// Close & deallocate the ResultSet query to the database. Failure to do so will result in memory leaks.
			query.close();
			query = null;

			// Close & deallocate the PreparedStatement. Failure to do so will result in memory leaks.
			ps.close();
			ps = null;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 *	Queries the database for any inactive devices that may have gone off-line due to uncontrollable situations
	 * 
	 * 	TODO: 	This needs to incorporate a true IP based ping. The IP may be alive, but the device may have failed. 
	 * 			Another situation is that the ip may have gone down and all devices on that IP are now inactive, or the port
	 * 			forwarding may have been messed up and need to be resolved.
	 */
	private void queryInactiveDevices()
	{
		// Write to a log file indicating that the daemon is going to ping for inactive devices
		createAndSendLogData("Info", "Pinging for inactive devices.");
		
		// Create an object array to hold all of the devices that are going to be pinged
		Vector<Object[]> toPing = new Vector<Object[]>();
		try 
		{
			// Retrieve all inactive devices from the database that need to be checked to see if they have came back online
			PreparedStatement ps = database.getConnection().prepareStatement(PreparedStatementStatics.DEVICE_INACTIVE_QUERY);
			
			// Store the results of the query into a ResultSet for processing
			ResultSet query = ps.executeQuery();

			// Ensure that there is at least 1 record that has been returned from the database.
			if (database.countResults(query) > 0)
			{
				// Loop through all of the returned elements from the database
				while (query.next())
				{
					// Return the Device's ID, Type, Primary & Secondary IP Ports, and IP Address
					String deviceType = query.getString(DatabaseStatics.DEVICE_TYPE);
					String ip = query.getString(DatabaseStatics.DEVICE_IP);
					int port1 = query.getInt(DatabaseStatics.DEVICE_PRIMARY_PORT);
					int port2 = query.getInt(DatabaseStatics.DEVICE_SECONDARY_PORT);
					
					//int port1 = query.getInt(DatabaseStatics.DEVICE_PORT_1);
					//int port2 = query.getInt(DatabaseStatics.DEVICE_PORT_2);
					//int port3 = query.getInt(DatabaseStatics.DEVICE_PORT_3);
					//int port4 = query.getInt(DatabaseStatics.DEVICE_PORT_4);

					// If we are running this on a test program (indicated by 'true'), then we will be using the Secondary Port for the socket communication
					boolean testDevice = query.getBoolean(DatabaseStatics.DEVICE_TEST);
					
					// If the device type is BT (indicating Bluetooth), then start up its thread
					if (deviceType.equals("BT"))
					{
						// Create the object with 4 elements to it
						Object[] o = new Object[4];	
						// Set the first element to the ip
						o[0] = ip;
						
						// Set the second element to the port
						o[1] = (!testDevice) ? port1 : port2;
						
						// Set the third element to the Command
						o[2] = "RTR";
						
						// Set the last element to the device's id
						o[3] = query.getInt(DatabaseStatics.DEVICE_ID);
						
						// Add the object to the toPing array
						toPing.add(o);
					}
					// If the device type is C1 (indicating a C1 Reader), then start up its thread
					else if (deviceType.equals("C1"))
					{
						// Create the object with 4 elements to it
						Object[] o = new Object[4];
						
						// Set the first element to the ip
						o[0] = ip;
						
						// Set the second element to the port
						o[1] = port1;
						
						// Set the third element to the Command
						o[2] = "T";
						
						// Set the last element tot the device's id
						o[3] = query.getInt(DatabaseStatics.DEVICE_ID);
						
						// Add the object to the toPing array
						toPing.add(o);
					}
				}
			}
			
			// Close & deallocate the ResultSet query to the database. Failure to do so will result in memory leaks.
			query.close();
			query = null;

			// Close & deallocate the PreparedStatement. Failure to do so will result in memory leaks.
			ps.close();
			ps = null;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		// Create the QueryDevice thread and pass it the array of devices to be pinged, the LoggerThread, and a reference to the database
		QueryDeviceThread qdt = new QueryDeviceThread(toPing, lt, database);
		
		// Start the QueryDeviceThread and have it process the inactive devices.
		qdt.start();		
	}
	
	/**
	 * Checks the database to see if any new devices have been added to the system
	 */
	private void checkForNewDevices()
	{
		// Write to a log file indicating that the daemon is going to ping for inactive devices
		createAndSendLogData("Info", "Checking for newly discovered devices.");

		try 
		{
			// Retrieve all active devices from the database so that the appropriate device thread may be started up
			PreparedStatement ps = database.getConnection().prepareStatement(PreparedStatementStatics.DEVICE_CHANGED_QUERY);
			
			// Store the results of the query into a ResultSet for processing
			ResultSet query = ps.executeQuery();

			// Ensure that there is at least 1 record that has been returned from the database.
			if (database.countResults(query) > 0)
			{
				// Loop through all of the returned elements from the database
				while (query.next())
				{
					// Return the Device's ID, Type, Primary & Secondary IP Ports, and IP Address
					int deviceID = query.getInt(DatabaseStatics.DEVICE_ID);
					String deviceType = query.getString(DatabaseStatics.DEVICE_TYPE);
					String ip = query.getString(DatabaseStatics.DEVICE_IP);
					int port1 = query.getInt(DatabaseStatics.DEVICE_PRIMARY_PORT);
					int port2 = query.getInt(DatabaseStatics.DEVICE_SECONDARY_PORT);
					
					// If we are running this on a test program (indicated by 'true'), then we will be using the Secondary Port for the socket communication
					boolean testDevice = query.getBoolean(DatabaseStatics.DEVICE_TEST);
			
					// Retrieve the deviceStatus so we can know if its active or inactive.
					int deviceStatus = query.getInt(DatabaseStatics.DEVICE_STATUS);
					
					// Set the UID of the device to match the type, as well as its IP and Port information
					String uid = deviceType + "_" + ip + ":" + port1;
					
					// If the device status is currently set to 0, then it is inactive and needs to be updated so it isn't inadvertently restarted
					if (deviceStatus == 0)
					{
						// If the device has recently been changed, then update the database and indicate it as not having any changes
						if (query.getInt(DatabaseStatics.DEVICE_CHANGED) == 1)
						{
							// Update the database to reflect that the device no longer has any changes that need to be accounted for
							PreparedStatement p = database.getConnection().prepareStatement(PreparedStatementStatics.DEVICE_UPDATE_CHANGED);
							p.setInt(1, 0);
							p.setInt(2, deviceID);

							// Execute the update statement on the database
							p.execute();
							
							// Close & deallocate the PreparedStatement. Failure to do so will result in memory leaks.
							p.close();
							p = null;
						}
						
						// Loop through all of the active device threads to find a match to the deviceID that needs to be shutdown						
						for (DeviceThread dt : activeThreads)
						{
							// If the DeviceThread's uniqueID matches the uid we calculated above, then shutdown taht thread.
							if (dt.getUniqueID().equals(uid))
							{
								// Update the database to reflect that the device no longer has any changes that need to be accounted for
								PreparedStatement p = database.getConnection().prepareStatement(PreparedStatementStatics.DEVICE_STATUS_CHANGED);
								p.setInt(1, 0);
								p.setInt(2, deviceID);

								// Execute the update statement on the database
								p.execute();
								
								// Close & deallocate the PreparedStatement. Failure to do so will result in memory leaks.
								p.close();
								p = null;

								// Shutdown the device thread
								dt.shutdownThread();
								
								// Remove the device thread from the active threads list
								activeThreads.remove(dt);
								
								// break out of the for loop since we found a match and don't need to loop through any more threads
								break;
							}
						}
					}
					// If the device status is currently set to 1, then it is now active and it needs to be started
					else
					{
						// If the device type is BT (indicating Bluetooth), then start up its thread
					//	if (deviceType.equals("BT"))
					//	{
							// Create the new Bluetooth Thread Object
							//BluetoothDeviceThread bdt = new BluetoothDeviceThread(this.dpd, this.lt, deviceID, uid, ip, (!testDevice) ? port1 : port2, this);

							// Add the newly created Bluetooth Thread object to the activeThreads array
							//activeThreads.add(bdt);
							
							// Start the Bluetooth thread
							//bdt.start();
					//	}
						// If the device type is C1 (indicating a C1 Reader), then start up its thread
						 if (deviceType.equals("C1"))
						{
							// Retrieve the number of records that can be stored in the Device's buffer.
							// -- More information on this and what it does is in @C1DeviceThread
							int bsize = query.getInt(DatabaseStatics.DEVICE_BUFFER_SIZE);
									
							// Create the new C1 Device Thread Object
							C1DeviceThread cdt = new C1DeviceThread(this.dpd, this.lt, deviceID, uid, ip, port1, bsize, this, testDevice, database);
							//C1DeviceThread cdt = new C1DeviceThread(this.dpd, this.lt, deviceID, uid, ip, port1, bsize, this, testDevice);
							
							// Add the newly created C1 Device Thread object to the activeThreads array
							activeThreads.add(cdt);
							
							// Start the C1 thread
							cdt.start();
						}
						// If the device type is AW (indicating Acyclia WIFI), then start up its thread
				//		else if (deviceType.equals("AW"))
				//		{
							// Retrieve the device's Serial ID and API key's from the database
							//int deviceSerialID = query.getInt(DatabaseStatics.DEVICE_SERIAL_ID);
							//String deviceAPIKey = query.getString(DatabaseStatics.DEVICE_API_KEY);
							
							// Set the UID of the device to match the type, as well as its SerialID
							//uid = deviceType + "_" + deviceSerialID;

							// Create the new Acyclica Device Thread Object
							//AcyclicaDeviceThread adt = new AcyclicaDeviceThread(this.dpd, this.lt, deviceID, uid, Globals.ACYCLICA_DEVICE_URL, deviceSerialID, this, deviceAPIKey, fastQuery);
							
							// Add the newly created Acyclica Device Thread object to the activeThreads array
							//activeThreads.add(adt);
							
							// Start the Acyclica thread
							//adt.start();
				//		}

						// Prepare a SQL Statement to update the device's active status in the database
						PreparedStatement p = database.getConnection().prepareStatement(PreparedStatementStatics.DEVICE_UPDATE_CHANGED);
						p.setInt(1, 0);
						p.setInt(2, deviceID);
						
						// Execute the update on the database record
						p.execute();
						
						// Close & deallocate the PreparedStatement query to the database. Failure to do so will result in memory leaks.
						p.close();
						p= null;
						
						// Query for all Adjacent sensors that goes along with that sensor
					//	PreparedStatement anq = database.getConnection().prepareStatement(PreparedStatementStatics.ADJACENT_NODES_QUERY);
					//	anq.setInt(1, deviceID);
					//	anq.setInt(2, deviceID);
						
						
						// Execute the query on the database
					//	ResultSet anqQuery = anq.executeQuery();
						
						// Loop through all of the records returned from the database
					/*	while (anqQuery.next())
						{
							// Retrieve the first and second node ID's and retrieve their distance and baseTravelTime
							int node1ID = anqQuery.getInt(DatabaseStatics.MAC_ADJACENT_NODE_NODE_1);
							int node2ID = anqQuery.getInt(DatabaseStatics.MAC_ADJACENT_NODE_NODE_2);
							float distance = anqQuery.getFloat(DatabaseStatics.MAC_ADJACENT_NODE_DISTANCE);
							int baseTravelTime = anqQuery.getInt(DatabaseStatics.MAC_ADJACENT_BASE_TRAVEL_TIME);

							// Create a new MacAddressSensorSegment and send it to the MacAddressSensorSegmentProcessor
							massp.addNewAdjacentPair(new MacAddressSensorSegment(lt, node1ID, node2ID, distance, baseTravelTime));
						} */

						
						// Close & deallocate the PreparedStatement query to the database. Failure to do so will result in memory leaks.
					//	anq.close();
					//	anqQuery.close();
					//	anq = null;
					}
				}
			}
			
			// Close & deallocate the ResultSet query to the database. Failure to do so will result in memory leaks.
			query.close();
			query = null;

			// Close & deallocate the PreparedStatement query to the database. Failure to do so will result in memory leaks.
			ps.close();
			ps = null;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a log message to the LoggerThread 
	 * 
	 * @param type		- Indicates what type of message is being written
	 * @param message	- The message that is to be written to the log file
	 */
	private void createAndSendLogData(String type, String message)
	{
		// Create a new LogItem using the thread name, message, log type, and the timestamp
		LogItem li = new LogItem(this.getName(), message, type, Calendar.getInstance());

		// Add the log item to the LoggerThread
		lt.addToList(li);
	}
	
	
	/**
	 * Returns the list of device threads currently running. This method is used in conjunction with the GUI interface
	 * 
	 * @return threadList
	 */
	public Vector<ThreadInformation> returnThreadList()
	{
		// Create an array to store a list of the running threads
		Vector<ThreadInformation> threads = new Vector<ThreadInformation>();
		
		// Add the Device Daemon to thread list, using the ThreadInformation class
		threads.add(new ThreadInformation("Self", this.getName(), "Device Daemon Processor", this.activeDate));
		
		// Loop through all of the currently active threads
		for (DeviceThread dt : activeThreads)
		{
			// Add the Device Thread to the list, using the ThreadInformation class
			threads.add(new ThreadInformation(this.getName(), dt.getName(), dt.getThreadDeviceType(), dt.getActiveDate()));
		}
		
		// Return the list of running threads
		return threads;
	}
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Device Daemon Thread Shutdown Management

	//TODO: This needs to be expanded to shut down all of the device threads as well.
	/**
	 * Tells the thread to begin shutting down and release all of its resources
	 */
	public void initializeShutdownProcess() 
	{
		this.threadAlive = false;
		
		shutdownThread();
	}
	
	/**
	 * Begins the shutdown process for the Log File Thread
	 */
	private void shutdownThread()
	{		
		// Generates a string stating what all was shutdown and the status of the objects released.s
		String shutdownThreadString = "\n"
							 + "********************* THREAD SHUTDOWN PROCESS *********************"
							 + "Shutting down thread " + this.getName()
						 	 + "*******************************************************************"
						 	 + "\n";
		
		// Prints out the result of the thread shutdown process
		System.out.println(shutdownThreadString);
				
		// This kills the thread
		this.interrupt();
	}
}
