/*package threads.processing;


import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import statics.Globals;
import threads.devices.bluetooth.BluetoothDeviceThread;
import threads.LoggerThread;
import threads.devices.DeviceDaemon;
import threads.devices.DeviceThread;
import threads.processing.DataProcessingDaemon;

public final class GPSThread extends TimerTask  {
	
	
	
	public void getGPS (){
		TimerTask gps = new GPSThread();
	
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(gps, getTomorrowMorning1am() , once_per_day);
	}
	
	public void run() {
		
		System.out.println("Fetching gps...");
		
	/*	try
		{
			// Attempt to connect to the device.
			if (super.connectToDevice())
			{
				theSecond = theTime.get(Calendar.SECOND);
				// Send the data command to the device
				out.println(COMMAND_DATA);
				
				//System.out.println(theSecond);
				//System.out.println("Checkpoint 1");
				
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
	
	// PRIVATE
	
	//expressed in milliseconds
	private final static long once_per_day = 1000*60*60*24;
	
	private final static int one_day = 1;
	private final static int one_am  = 1;
	private final static int zero_min = 0;
	
	private static Date getTomorrowMorning1am(){
		Calendar tomorrow = new GregorianCalendar();
		tomorrow.add(Calendar.DATE, one_day);
		Calendar result = new GregorianCalendar(
			tomorrow.get(Calendar.YEAR),
			tomorrow.get(Calendar.MONTH),
			tomorrow.get(Calendar.DATE),
			one_am,
			zero_min
		);
		return result.getTime();
	}

}*/
