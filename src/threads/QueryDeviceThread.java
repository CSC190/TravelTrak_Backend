/**
 * ****************************************************************
 * File: 			QueryDeviceThread.java
 * Date Created:  	January 10, 2014
 * Programmer:		Dale Reed
 * 
 * Purpose:			This thread is called by the DeviceDaemon at a 
 * 					predefined interval to scan the database for any
 * 					possible device changes. This is primarily for 
 * 					cases where the device lost its network connection,
 * 					and doesn't have a mechanism to notify the server
 * 					that it is now active again. This thread will not
 * 					check any devices that have been deleted by an 
 * 					administrator.
 * 
 * ****************************************************************
 */
package threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Vector;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import objects.Database;
import objects.LogItem;
import statics.PreparedStatementStatics;



public class QueryDeviceThread extends Thread
{
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- QueryDeviceThread Variable Declarations
	
	/**
	 * Array to store a list of objects containing the device information for querying
	 */
	private Vector<Object[]> deviceInfo;
	
	/**
	 * Socket used for establishing a connection to a particular device
	 */
	private Socket socket;
	
	/**
	 * Used for transmitting data through the socket
	 */
	protected PrintWriter out;
	
	/**
	 * Used for receiving and storing data from the socket
	 */
	protected BufferedReader in;
	
	/**
	 * Provides access to the database for updating records as necessary.
	 */
	private Database db;
	
	/**
	 * Provides access to the LoggerThread so that any debug information for this thread can be stored.
	 */
	private LoggerThread lt;
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- QueryDeviceThread Construction & Methods 

	/**
	 * Initializes the QueryDeviceThread with the required information to begin processing
	 * 
	 * @param devices 	- A list of devices to be queried
	 * @param lt		- The LoggerThread that will receive any log files to be stored
	 * @param db		- The database for updating particular records
	 */
	public QueryDeviceThread(Vector<Object[]> devices, LoggerThread lt, Database db)
	{
		// Set the name of the thread
		this.setName(this.getClass().getSimpleName());
		
		// Set the LoggerThread object
		this.lt = lt;
		
		// Set the list of devices that are to be queried
		deviceInfo = devices;
		
		// Set the database object
		this.db = db;
	}
	
	/**
	 * Runs the thread until all processing has been completed
	 */
	public void run()
	{
		// Send a log file message indicating how many devices are going to be queried
		createAndSendLogData("Info", "Querying " + deviceInfo.size() + " inactive devices");

		try 
		{
			// Initialize a PreparedStatement so that it is ready to be used when a record needs to be updated.
			PreparedStatement ps = db.getConnection().prepareStatement(PreparedStatementStatics.DEVICE_NOW_ACTIVE_UPDATE);
			
			// Counts how many of the devices were successfully queried.
			int successes = 0;
			
			// Loop through all of the Device Objects in the deviceInfo array
			for (Object[] o : deviceInfo)
			{
				// Send a log file message indicating the device we are attempting to connect to.
				createAndSendLogData("Info", "\nAttempting to connect to " + (String)o[0] + ":" + (Integer)o[1]);
				
				//System.out.println(o[0]);
				//System.out.println(o[1]);
				//System.out.println(o[2]);
				// Allocate a new socket to be used in the connection
				socket = new Socket();
				try
				{						
					// Establish a connection to the socket, setting a socket connection timeout to occur if it fails to connect.
					socket.setSoTimeout(15000);
					socket.connect(new InetSocketAddress((String)o[0], (Integer)o[1]), 30000);
	
					// If the connection was successfully established
					if (socket.isConnected())
					{
						// Send a log file indicating that a connection was successfully established to the device.
						createAndSendLogData("Connection", "Connection established to " + (String)o[0] + ":" + (Integer)o[1]);
	
						// Set the variables to be used with the buffers for use with the socket streams
						out = new PrintWriter(socket.getOutputStream(), true);
						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	
						// Send the appropriate command to the device via the socket.
						out.println((String)o[2]);
						
						// Attempt to read in the response from the queried device.
						String data = in.readLine();
	
						// If the data returned from the device has at least one character in the string, then update the database indicating
						// that the device was successfully queried. Otherwise do nothing.
						if (data.length() > 0)
						{
							// Send a log file indicating that we were able to successfully read data from the device and it is now ready to be queried.
							createAndSendLogData("Connection", "Successfully read data from " + (String)o[0] + ":" + (Integer)o[1]);

							// Set the device ID in the PreparedStatement
							ps.setInt(1, (Integer)o[3]);
							
							// Add the query as a batch to the PreparedStatment.
							ps.addBatch();
							
							// Increase our success counter since a valid device was found
							successes++;
						}
					}
					else
					{	
						// Indicate that we were unsuccessful in connecting to the device.
						createAndSendLogData("Connection", "Socket Failed to connect to " + (String)o[0] + ":" + (Integer)o[1]);
					
					
						// Indicate that we are attempting to close the socket connection after receiving data from the device.
						createAndSendLogData("Connection", "Attempting to close connection to " + (String)o[0] + ":" + (Integer)o[1]);
			
						// Surround the methods with a try/catch to catch any IOException that is thrown by the Socket or Buffered Reader.
						try 
						{			
							// Make sure that socket is not null before trying to destroy it
							if (socket != null)
							{				
								// Close the socket 
								if (!socket.isClosed() && socket != null)
									socket.close();
								
								// Close the PrintWriter
								if (out != null)
									out.close();
								
								// Close the BufferedReader
								if (in != null)
									in.close();
								
								// Destroy the Socket, PrintWriter and BufferedReader objects
								socket = null;
								out = null;
								in = null;
								
								// If the Socket still has not been destroyed, log an error message
								if (socket != null)
								{
									createAndSendLogData("Error","An Error occured while destroying the 'socket' object");
								}
								
								// If the PrintWriter still has not been destroyed, log an error message
								if (out != null)
								{
									createAndSendLogData("Error","An Error occured while destroying the 'output' object");
								}
					
								// If the BufferedReader still has not been destroyed, log an error message
								if (in != null)
								{
									createAndSendLogData("Error","An Error occured while destroying the 'input' object");
								}
							}
							
							createAndSendLogData("Connection", "Connection successfully closed");
			
							// Indicate that we successfully closed down the socket connection
						} 
						catch (IOException ioE) 
						{
							// Catch any errors and write the stack trace to a string so it can be written to a log file
							StringWriter errors = new StringWriter();
							ioE.printStackTrace(new PrintWriter(errors));
			
							// Write the error to the log file
							createAndSendLogData("Error", "Socket Disconnect Error - IOException: " + ioE.getMessage());
						}	
					}
				}
				catch (IOException e)
				{			
					// Indicate that we were unsuccessful in reading data from the device.
					createAndSendLogData("Connection", "Socket Failed to read data stream to " + (String)o[0] + ":" + (Integer)o[1]);
	
					// An error occurred while trying to connect connect to the device
					try
					{
						// Close down the socket. However if there was a problem with connecting the socket,
						// this will most likely throw a IOException
						if (socket != null)
							socket.close();
					}
					catch (IOException e1)
					{
						// Generate a string representation of the stack trace to be written to the log file
						StringWriter errors = new StringWriter();
						e1.printStackTrace(new PrintWriter(errors));
	
						createAndSendLogData("Error", "Socket Disconnect Error - Failed to disconnect from device. IOException: " + e1.getMessage());
					}
					catch (NullPointerException npe)
					{
						// Generate a string representation of the stack trace to be written to the log file
						StringWriter errors = new StringWriter();
						npe.printStackTrace(new PrintWriter(errors));
	
						// Send the stack trace to the log file
						createAndSendLogData("Error", "Socket Connection Error - NullPointerException: " + npe.getMessage());
					}
				}
			}		

			// If there were any successful connections, then perform the SQL operations on the database.
			if (successes > 0)
			{
				ps.execute();
			}

			// Close and release the PreparedStatement. Failing to do so will result in memory leaks.
			ps.close();
			ps = null;
		}
		catch (SQLException e2) 
		{
			e2.printStackTrace();
		}
		
		try
		{
			//If the device is active, it will receive a time stamp 
			PreparedStatement ps = db.getConnection().prepareStatement(PreparedStatementStatics.DEVICE_LAST_SEEN);
			ps.setTimestamp(1, PreparedStatementStatics.getCurrentTimeStamp());
			ps.addBatch();
			ps.execute();
			ps.close();
			ps = null;
		}
		catch (SQLException e2)
		{
			e2.getStackTrace();
		}

		// Indicate that the thread is shutting down.
		createAndSendLogData("Info", "Thread Shutting Down");
	}

	/**
	 * Sends a log message to the LoggerThread 
	 * 
	 * @param type
	 * @param message
	 */
	private void createAndSendLogData(String type, String message)
	{
		// Create a new LogItem using the thread name, message, log type, and the timestamp
		LogItem li = new LogItem(this.getName(), message, type, Calendar.getInstance());
		
		// Add the log item to the LoggerThread
		lt.addToList(li);
	}
}
