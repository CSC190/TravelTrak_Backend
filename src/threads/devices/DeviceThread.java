/**
 * ****************************************************************
 * File: 			DeviceThread.java
 * Date Created:  	January 13, 2014
 * Programmer:		Dale Reed
 * 
 * Purpose:			This thread contains a master set of functions
 * 					that all devices threads inherit and utilize,
 * 					such as socket connection management, and data 
 * 					passing to other threads
 * 					
 * 
 * ****************************************************************
 */
package threads.devices;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;

import objects.LogItem;
import objects.mac_address.RawMacData;
import threads.LoggerThread;
import threads.processing.DataProcessingDaemon;

public abstract class DeviceThread extends Thread
{
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- DeviceThread Variable Declarations	
	
	// Provides a reference back to the DeviceDaemon for use with auto-shutdown management
	private DeviceDaemon daemon;
	
	// Provides a reference back to the DataProcessingDaemon so that the data can be processed
	private DataProcessingDaemon dpd;
	
	// Provides a reference back to the LoggerThread so that log files may be written
	private LoggerThread lt;	

	
	// Indicates how many failed connection attempts have been performed for the device. 
	// This is used for self-shutdown of the thread
	protected int consecutiveFailedConnectionAttempts = 0;
	
	// Indicates how many connection attempts have been made since the thread was created
	private int totalConnectionAttempts = 0;
	
	// Indicates how many of those attempts were successful
	private int totalSuccessfulConnectionAttempts = 0;

	// Indicates if the thread is to continue running
	protected boolean threadAlive = true;
	
	// Used for sending out data to be sent to the devices via a socket
	protected PrintWriter out;
	
	// Used for reading in data being returned from the devices via a socket
	protected BufferedReader in;

	// Socket to contain the communication between the server and devices
	private Socket socket;
	
	// Indicates how long the server should attempt to connect to the device
	private int CONN_TIMEOUT = 5000;
	
	// Indicates how long the server should wait to receive data from the device
	private int READ_TIMEOUT = 10000;

	// Unique ID of the device as stored in the database
	private int deviceID;
	
	// Indicates what the device type is.
	private String deviceType;

	
	// IP Addresss of the device being communicated with
	protected String ipAddress;
	
	// Port of the device being communicated with
	protected int port;

	// Unique Identifier for the thread, based on the device type, IP address, and Port number
	private String uniqueId;

	// Indicates what time the thread became active
	private Date activeDate;

	
	// -- C1 Reader Specific Variables

	// Used for determining how long a device is to sleep for. 
	//This is specific to the C1 Reader as the duration that it sleeps is largely dependent on how full its buffers are
	protected long sleep=1;
	

	// -- Bluetooth Reader Specific Variables
	
	
	// -- Acyclia Specific Variables
	
	// Unique Serial Number of the Acyclia Device
	private int deviceSerialID;
	
	// URL to open for the Acyclia Device
	private String deviceURL;
	
	// API Key needed to query data from the Acyclia Servers
	private String apiKey;
	
	// Used for querying the Acyclia servers very quickly, similar to a batch mode.
	protected boolean fastQuery;

	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- DeviceThead Construction

	/**
	 * Creates a new DeviceThread for a device using sockets as the communication protocol
	 * 
	 * @param dpd 	- Link to the DataProcessignDaemon
	 * @param lt 	- Link to the LoggerThread
	 * @param did	- The DeviceID id of the device (as stored in the database)
	 * @param unid	- The Unique Device ID for use with the thread's name
	 * @param type	- Indicates the type of the device (C1, BT)
	 * @param ip	- The IP Address of the device
	 * @param p		- The port number of the device
	 * @param dm	- Link to the DeviceDaemon
	 */
	public DeviceThread(DataProcessingDaemon dpd, LoggerThread lt, int did, String unid, String type, String ip, int p, DeviceDaemon dm)
	{
				
		// Set the DataProcessingDaemon Link
		this.dpd = dpd;
		
		// Set the LoggerThread link
		this.lt = lt;
		
		// Set teh DeviceDaemon Link
		this.daemon = dm;

		
		// Set the DeviceID
		this.deviceID = did;
		
		// Set the Unique DeviceID
		this.uniqueId = unid;
		
		
		// Set the DeviceType
		this.deviceType = type;
		
		// Set the IP Address
		this.ipAddress = ip;
		
		// Set the Port Number
		this.port = p;
		
		// Set the date for when the thread came online
		this.activeDate = Calendar.getInstance().getTime();
		
		// Set the name of the thread to the UniqueID passed in the constructor
		setName(unid);
	}
	
	/**
	 * Creates a new DeviceThread for a http based device
	 * 	
	 * @param dpd 		- Link to the DataProcessignDaemon
	 * @param lt 		- Link to the LoggerThread
	 * @param did		- The DeviceID id of the device (as stored in the database)
	 * @param unid		- The Unique Device ID for use with the thread's name
	 * @param url		- The URL to connect to
	 * @param serialID	- The device's Unique SerailID, used with the URL
	 * @param dm		- Link to the DeviceDaemon
	 * @param api		- The API key needed to access the URL
	 * @param fastQuery	- Indicates if the http server is going to run frequently
	 */
	public DeviceThread(DataProcessingDaemon dpd, LoggerThread lt, int did, String unid, String url, int serialID, DeviceDaemon dm, String api, boolean fastQuery)
	{
		// Set the DataProcessingDaemon Link
		this.dpd = dpd;
		
		// Set the LoggerThread link
		this.lt = lt;
		
		// Set teh DeviceDaemon Link
		this.daemon = dm;

		
		// Set the DeviceID
		this.deviceID = did;
		
		// Set the Unique DeviceID
		this.uniqueId = unid;

		// Set the device's URL 
		this.deviceURL = url;
		
		// Set the device's SerialID number
		this.deviceSerialID = serialID;
		
		// Set the API key needed for accessing the URL
		this.apiKey = api;
		
		// Set the DeviceType
		this.deviceType = "AW";
		
		// Indicate if the http server is going to be queried quickly
		this.fastQuery = fastQuery;
		
		
		// Set the date for when the thread came online
		this.activeDate = Calendar.getInstance().getTime();
		
		// Set the name of the thread to the UniqueID passed in the constructor
		setName(unid);
	}
	

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- DeviceThead Threading Methods
	//		-- run()
	// 		-- sendDataForProcessing()
	//		-- connectToDevice()
	//		-- disconnectFromDevice()

	/**
	 * Abstract Method that must be implemented in all subclasses of DeviceThread. 
	 * This handles all the logic to be performed while the thread is running.
	 */
	abstract public void run();
	
	/**
	 * Sends data from the Subclass and passes it on to the DataProcessingDaemon
	 * 
	 * @param data - The data to be sent for processing
	 */
	protected void sendDataForProcessing(String data)
	{
		// Write the raw data to a log file
		createAndSendLogData("Raw Data", data);		
				
		// If the device type matches bluetooth, then send it off for processing. 
		// TODO: Eventually this could all be concatenated into one call, but as only one devicetype is actively being processed, this prevents
		//		 unnecessary data being sent to the processing daemons
		if (this.deviceType.equals("BT"))
		{	
			dpd.addData(new RawMacData(this.deviceID, data));
		}
		else if (this.deviceType.equals("C1"))
		{
			/*protected void createAndSendLogData(String type, String message)
			{
				LogItem li = new LogItem(this.getName(), message, type, Calendar.getInstance());
				lt.addToList(li);
			}*/
			//dpd.addData(new RawMacData(this.deviceID, data));
		}
		
		

		// Check to see the DataProcessingDaemon is asleep. If it is, then wake it up with an interrupt.
		if (dpd.isSleeping())
			dpd.interrupt();
	}
	
	/**
	 * Connects to a device and allocates all necessary memory resources
	 * 
	 * @return true/false - indicates if the socket connection was successfully established.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	
	protected boolean connectToDevice()
	{
		createAndSendLogData("Connection", "------------------------------------------------------------------------");
		createAndSendLogData("Connection", "Establishing connection toEstablishing connection to " + this.ipAddress + ":" + this.port);

		this.totalConnectionAttempts++;
		
		// Allocate a new socket to be used in the connection	
		
		socket = new Socket();
	
			
		try
		{			
			// Establish a connection to the socket, setting a socket connection timeout to occur if it fails to connect.
			socket.setSoTimeout(READ_TIMEOUT);
			socket.connect(new InetSocketAddress(ipAddress, port), CONN_TIMEOUT);

			// If the connection was successfully established
			if (socket.isConnected())
			{
				this.totalSuccessfulConnectionAttempts++;
				createAndSendLogData("Connection", "Connection established to " + this.ipAddress + ":" + this.port);

				if (this.consecutiveFailedConnectionAttempts != 0)
				{
					createAndSendLogData("Connection", "Resetting number of consecutive failed connection attempts from " + this.consecutiveFailedConnectionAttempts + " to 0");
				}
				this.consecutiveFailedConnectionAttempts = 0;
				// Set the variables to be used with the buffers for use with the socket streams
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// Return that we have successfully connected to the device
				return true;
			}
			else
			{	
				// Indicate that we were unsuccessful in connecting to the device.
				return false;
			}
		}
		catch (IOException e)
		{			
			createAndSendLogData("Connection", "Socket Failed to connect to " + this.ipAddress + ":" + this.port + ". Error: " + e.getMessage());

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

				createAndSendLogData("Error", "Socket Connection Error - IOException: " + e1.getMessage());
			}
			catch (NullPointerException npe)
			{
				// Generate a string representation of the stack trace to be written to the log file
				StringWriter errors = new StringWriter();
				npe.printStackTrace(new PrintWriter(errors));

				// Send the stack trace to the log file
				createAndSendLogData("Error", "Socket Connection Error - NullPointerException: " + npe.getMessage());
			}
			
			if (!disconnectFromDevice())
				createAndSendLogData("Error", "Socket Disconnect Error - Failed to disconnect from device");
		
			if (this.consecutiveFailedConnectionAttempts++ > 5)
			{
				createAndSendLogData("Connection", "Number of failed connection attempts exceeded the limit of 5. Shutting down thread.");
				this.daemon.removeThreadFromList(this.uniqueId);
			}
			
			createAndSendLogData("Connection", "Number of consecutive failed connection attempts " + this.consecutiveFailedConnectionAttempts);
			
		}

		// To keep eclipse happy and from throwing an error
		return false;	
	}
	
	/**
	 * Disconnects from a device that is using sockets, and deallocates all variables that were used to reduce memory usage.
	 * 
	 * @return true/false - indicates if the socket was successfully closed.
	 */
	protected boolean disconnectFromDevice()
	{
		createAndSendLogData("Connection", "Attempting to close connection to " + this.ipAddress + ":" + this.port);

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
			
			createAndSendLogData("Connection", "Connection successfully closed\n");

			// Indicate that we successfully closed down the socket connection
			return true;
		} 
		catch (IOException ioE) 
		{
			// Catch any errors and write the stack trace to a string so it can be written to a log file
			StringWriter errors = new StringWriter();
			ioE.printStackTrace(new PrintWriter(errors));

			// Write the error to the log file
			createAndSendLogData("Error", "Socket Disconnect Error - IOException: " + ioE.getMessage());

			// Indicate that we were unsuccessful in closing down the socket connection
        	return false;
		}	
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Log File Methods
	
	/**
	 * Sends a log message to the LoggerThread 
	 * 
	 * @param type
	 * @param message
	 */
	protected void createAndSendLogData(String type, String message)
	{
		LogItem li = new LogItem(this.getName(), message, type, Calendar.getInstance());
		lt.addToList(li);
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- HTTP Specific Methods
	
	/**
	 * Connects to a HTTP server to download an XML file
	 * 
	 * @param startTime	- The start time of the data window
	 * @param endTime	- The ending time of the data window
	 * 
	 * @return file 	- The file to be processed
	 */
	protected File connectToServer(long startTime, long endTime)
	{
		// File to write the xml data do
		File fXmlFile = null;
		try 
		{
			// Create the XML file based off of the device's uniqueID
			String filename = this.uniqueId + ".xml";
			
			// Builds the URL string of the device
			String url = String.format("%s/%s/%d/%d/%d", this.deviceURL, this.apiKey, this.deviceSerialID, startTime, endTime);
		
			// Saves the XML file to the disk
			this.saveXMLFile(filename, url);
			
			// Write a log message indicating that the log file was successfully written
			createAndSendLogData("Connection", "Connection established to " + url + " and file saved at " + filename);

			// Allocate the fXmlFile object with the new file and its contents
			fXmlFile = new File(filename);
	    } 
		catch (Exception e) 
	    {
	    	e.printStackTrace();
	    }

		// Return the XML file to be processed
		return fXmlFile;
	}
	
	/**
	 * Saves the downloaded XML file from an a web server (such as Acyclia) for processing
	 * 
	 * @param filename	- The location to save the file to
	 * @param urlString	- The URL to initiate the download from
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
  	public void saveXMLFile(String filename, String urlString) throws MalformedURLException, IOException
	{
  		// Create the input stream to read the data from the web server
	    BufferedInputStream in = null;
	    
	    // Create the output stream to write the data to a file
	    FileOutputStream fout = null;
	   
	    try
	    {
	    	// Create the input stream and have it read in the contents of the URL
	    	in = new BufferedInputStream(new URL(urlString).openStream());
	    	
	    	// Create the output stream and write to the file specified
	        fout = new FileOutputStream(filename);
	
	        // Create an array to read in the data from the buffer
	        byte data[] = new byte[1024];
	        
	        // Keeps track of how many bytes were read in from the input stream
	        int count;
	        
	        // As long as there is information to be read in from the input stream, continue writing data to the file writer
	        while ((count = in.read(data, 0, 1024)) != -1)
	        {
	        	// Write the data to the file
	            fout.write(data, 0, count);
	        }
	    }
	    catch (Exception e)
	    {
	    	System.err.println("Error saving file..." + e.getMessage());
	    }
	    finally
	    {
	    	// Close down the input buffer
	        if (in != null)
	            in.close();
	        
	        // Close down the output buffer
	        if (fout != null)
	            fout.close();
	    }
	}
  	
  	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Device Thread Shutdown Management

	/**
	 * Begins the shutdown process for the DeviceThread
	 */
	public void shutdownThread()
	{	
		// Generates a string stating what all was shutdown and the status of the objects released.s
		String shutdownThreadString = "\n"
							 + "********************* THREAD SHUTDOWN PROCESS *********************\n"
							 + "Shutting down thread " + this.getName() + "\n"
						 	 + "*******************************************************************\n";

		createAndSendLogData("Info", "Too many failed connetion attempts. Shutting down thread");

		
		// Prints out the result of the thread shutdown process
		System.out.println(shutdownThreadString);
				
		// This will kill the thread
		this.threadAlive = false;
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Device Thread Getters/Setters
	
	public boolean getThreadAlive()
	{
		return this.threadAlive;
	}

	public int getDeviceID()
	{
		return this.deviceID;
	}
	
	public String getUniqueID()
	{
		return this.uniqueId;
	}

	public String getThreadDeviceType()
	{
		return this.deviceType;
	}

	public Date getActiveDate()
	{
		return this.activeDate;
	}
	
	public int getTotalConnectionAttempts()
	{
		return this.totalConnectionAttempts;
	}
	
	public int getTotalSuccessfulConnectionAttempts()
	{
		return this.totalSuccessfulConnectionAttempts;
	}

  	public String getDeviceURL()
	{
		return this.deviceURL;
	}
}
