/**
 * ****************************************************************
 * File: 			BluetoothDeviceThread.java
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
/*package threads.devices.bluetooth;

import java.io.IOException;

import statics.Globals;
import threads.LoggerThread;
import threads.devices.DeviceDaemon;
import threads.devices.DeviceThread;
import threads.processing.DataProcessingDaemon;

import java.util.Calendar;
 

public class BluetoothDeviceThread extends DeviceThread 
{
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- BluetoothDeviceThread Variable Declarations

	// Command to be sent to the Bluetooth Device for receiving data
	private String COMMAND_DATA = "BT";
	
	// Command to be sent to the Bluetooth Device for receiving GPS location 
	private String COMMAND_GP = "GP";
	
	// Command to be sent to the Bluetooth Device for time sync
	private String COMMAND_RTW = "RTW";
	
	int theHour;
	int theMinute;
	int theSecond;

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- BluetoothDeviceThread Construction

	/**
	 * Creates the BluetoothDeviceThread
	 * 
	 * @param dpd 	- Link to the DataProcessignDaemon
	 * @param lt 	- Link to the LoggerThread
	 * @param did	- The DeviceID id of the device (as stored in the database)
	 * @param unid	- The Unique Device ID for use with the thread's name
	 * @param ip	- The IP Address of the device
	 * @param port	- The port number of the device
	 * @param dm	- Link to the DeviceDaemon
	 */
	/*public BluetoothDeviceThread(DataProcessingDaemon dpd, LoggerThread lt, int did, String unid, String ip, int port, DeviceDaemon d)
	{
		// Call the parent constructor to create the thread
		super(dpd, lt, did, unid, "BT", ip, port, d);

		System.out.println("Creating BluetoothDeviceThread with parameters: " + unid + ", " + ip + ", " + port);
	}
	
	/**
	 * Continually runs as long as the thread's threadAlive status is true. This performs the logic to connect to a device, request data,
	 * close down the connection, and sleep for a pre-determined amount of time.
	 */
	
	/*public void run() 
	{
		// Run as long as the thread is alive
		while (this.threadAlive)
		{
			// Get current calendar time 
			Calendar theTime = Calendar.getInstance();
			//System.out.println("START thread");
			
			// Get current hour
			theHour = theTime.get(Calendar.HOUR_OF_DAY);
			// Get current minute
			theMinute = theTime.get(Calendar.MINUTE);
			// Get current second
			theSecond = theTime.get(Calendar.SECOND);
			
			//System.out.println("the second is: " + theTime.get(Calendar.SECOND));
			try
			{
				// Attempt to connect to the device.
				if (super.connectToDevice())
				{
					// Send the data command to the device
					out.println(COMMAND_DATA);
					
					// Read and store the data returned from the device
					String data = in.readLine();
					
					// Send the data off to be processed
					sendDataForProcessing(data);
					
					// Disconnect from the device
					super.disconnectFromDevice();
				}
				else
				{
					// Indicate that a connection was not able to be made
					createAndSendLogData("Info", "Failed to establish connection to device. Failed Consecutive Attempt #" + this.consecutiveFailedConnectionAttempts);
				}
				
				// Have the thread sleep for a pre-determined amount of time.
				BluetoothDeviceThread.sleep(Globals.BLUETOOTH_SLEEP_TIME);
			}
			catch (IOException e)
			{
				createAndSendLogData("Error", "Failed to read data stream from " + this.ipAddress + ":" + this.port);
			}
			catch (InterruptedException e) 
			{
				createAndSendLogData("Error", "Device " + this.ipAddress + ":" + this.port + " failed to go to sleep");
			}
		}
	}
}*/
			//try
			//{
				// If time is midnight, request GPS location
				//if (theHour == 16 && theMinute == 02 && (theSecond >= 10 || theSecond<=12) )
				//{
					//System.out.println("In the loop");
					
					//theHour = 12;
					//theMinute = 30;
					//theSecond = 30;
					
					//if (super.connectToDevice())
					//{
						// Send the data command to the device
						//out.println(COMMAND_RTW + theHour + theMinute + theSecond);
						
						// Send the current time to device for time synchronization
						//out.println(theHour + ":" + theMinute + ":" + theSecond);
						
						//System.out.println("Simulation of midnight is successful");
						
						// Read and store the data returned from the device
						//String data = in.readLine();
						
						// Sleeps for 2 seconds to allow time for reply 
						//Thread.sleep(2000);
						
						// The expected string to be received from bluetooth
						//String dataIn = "Ok";
						
						// If we don't receive expected string
						/*while (data != dataIn) {
							// Send command again
							out.println(COMMAND_GP);
							// Sleep for 2 more seconds
							Thread.sleep(2000);
							// Retrieve the reply 
							data = in.readLine();
						}*/
						
						// Send the data off to be processed 
						//sendDataForProcessing(data);
						
						// Disconnect from the device 
		//				super.disconnectFromDevice();
	//				}
					
			//	}
			
			//}
			//catch (IOException e)
			//{
				//createAndSendLogData("Error", "Failed to read data stream from " + this.ipAddress + ":" + this.port);
			//}
			//catch (InterruptedException ex) {
				//Thread.currentThread().interrupt();
			//}
		//}
//	}
//}
