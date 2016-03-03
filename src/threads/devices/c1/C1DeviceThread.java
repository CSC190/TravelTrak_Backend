  package threads.devices.c1;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import statics.*;
import threads.LoggerThread;
import threads.devices.DeviceDaemon;
import threads.devices.DeviceThread;
import threads.processing.DataProcessingDaemon;
import objects.Database;

import java.sql.*;
import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.JFileChooser;

import org.apache.commons.lang3.StringUtils;


public class C1DeviceThread extends DeviceThread 
{
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- C1DeviceThread Variable Declarations
	
	String criteria = " ";
	String [] dataIn =  new String[100];
	String schipNum = "-1";
	String schannel = "-1";
	String state, milliDay, log170chan;
	int chipNum, channel;
	
	//FileWriter dir;
	//BufferedWriter buff;
	
	double lMillis, unixTime;
	long tTime;
	
	int count, numTick, secDay;
	int Milli;
	//int count10 = 0;
	
	//String [] test = new String[6000];
	String cNum, cChan, cState;
	//int tCount = 10;
	
	String combo, scomb;
	String [] comb = {"-1", "-1"};
	String [] comboPin = {"-1", "-1"};
	String floatTime;
	
	String hour, minute, second;
	
	
	private Database db;
	
	
	// Command to receive data from the device
	private String COMMAND_DATA = "C1";
	
	// Command to update clock 
	private String COMMAND_RENEW = "RTW";
	
	// Command to renew clock 
	private String COMMAND_READ = "RTR";
	
	// Command to receive the time from the device
	private String COMMAND_TIME = "T";

	// Indicates how many records can be stored in the buffer. 
	// TODO: Functionality needs to be added to run() so that depending on the number of records received, the sleep timer will adjust accordingly to prevent the 
	// 		 buffer on the device from becoming full. Ideally the timer should be a moving average of the last 5 queries and should attempt to keep the buffer 
	//		 around 50% full, and should wait a maximum of around 30 seconds between queries.
	//private int bufferSize = 0;
	
	// Indicates if the device is being used as a testing platform.
	private boolean testDevice;
	


	/**
	 * Creates the C1DeviceThread
	 * 
	 * @param dpd 			- Link to the DataProcessignDaemon
	 * @param lt 			- Link to the LoggerThread
	 * @param did			- The DeviceID id of the device (as stored in the database)
	 * @param unid			- The Unique Device ID for use with the thread's name
	 * @param ip			- The IP Address of the device
	 * @param port			- The port number of the device
	 * @param bSize			- The number of records that can be stored in the buffer
	 * @param d				- Link to the DeviceDaemon
	 * @param testDevice
	 */
	public C1DeviceThread(DataProcessingDaemon dpd, LoggerThread lt, int did, String unid, String ip, int port, int bSize, DeviceDaemon d, boolean testDevice, Database db)
	{
		// Call the parent constructor to create the thread
		super(dpd, lt, did, unid, "C1", ip, port, d);
		
		// Sets database reference
		this.db = db;
		
		 /* NOTE: currently, there is no need to store this value. Until a reason arrives, keep this commented*/
		// Sets the number of records that can be stored in the buffer
		//this.bufferSize = bSize;

		// Sets if the device is being used for testing, in whcih case it uses the COMMAND_TIME parameter when querying data
		this.testDevice = testDevice;
		
		System.out.println("Creating C1DeviceThread with parameters: " + unid + ", " + ip + ", " + port + ", " + bSize + ", " + testDevice);
	}
	
	/*--------------------------------------------------------------------------------------------------*/
	// Gets current time in format HHMMSS. Used to update RTC of Rabbit. 
	public void getClockTime()
	{
		Calendar c = Calendar.getInstance();
		hour = StringUtils.leftPad(Integer.toString(c.get(Calendar.HOUR_OF_DAY)), 2, "0");
		minute = StringUtils.leftPad(Integer.toString(c.get(Calendar.MINUTE)), 2, "0");
		second = StringUtils.leftPad(Integer.toString(c.get(Calendar.SECOND)), 2, "0");
	}
	/*--------------------------------------------------------------------------------------------------*/
	
	
	/*--------------------------------------------------------------------------------------------------*/
	// Gets current unix timestamp in milliseconds
	public long getUnixTime()
	{
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("PST"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long unixTimeStamp = c.getTimeInMillis();
		return unixTimeStamp;
	}
	/*--------------------------------------------------------------------------------------------------*/
	
	
	/*--------------------------------------------------------------------------------------------------*/
	// Takes chip number (1-5) and channel number (01-24) sent from client and maps to C1_Pin and log170channel
	// log170 format has input number(0-3) and bit number(0-7) which maps to suitcase port(1-6) and bit digits(1-8)
	// due to bit retrictions only 4 port out of 6 can be active
	public String[] getComboPin()
	{
		
		chipNum = Integer.parseInt(schipNum);
		channel = Integer.parseInt(schannel);
											  //assigned comboPin "100" to inactive pins since 170 controller only reads 32 channels	
		if ((chipNum == 1) && (channel == 4)) //inactive input until chip number 2 channel number 24
		{
			comboPin[0] = "2";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 5))
		{
			comboPin[0] = "3";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 6))
		{
			comboPin[0] = "4";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 7))
		{
			comboPin[0] = "5";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 8))
		{
			comboPin[0] = "6";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 9))
		{
			comboPin[0] = "7";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 10))
		{
			comboPin[0] = "8";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 11))
		{
			comboPin[0] = "9";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 12))
		{	
			comboPin[0] = "10";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 13))
		{
			comboPin[0] = "11";
			comboPin[1] = "100";
		}
		else if ((chipNum == 1) && (channel == 14))
		{
			comboPin[0] = "12";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 15))
		{
			comboPin[0] = "13";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 16))
		{
			comboPin[0] = "15";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 17))
		{
			comboPin[0] = "16";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 18))
		{
			comboPin[0] = "17";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 19))
		{
			comboPin[0] = "18";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 20))
		{
			comboPin[0] = "19";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 21))
		{
			comboPin[0] = "20";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 22))
		{
			comboPin[0] = "21";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 23))
		{
			comboPin[0] = "22";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 24))
		{
			comboPin[0] = "23";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 25))
		{
			comboPin[0] = "24";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 26)) 
		{
			comboPin[0] = "25";
			comboPin[1] = "100";
		} else if ((chipNum == 1) && (channel == 27))
		{ 
			comboPin[0] = "26";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 4))
		{
			comboPin[0] = "27";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 5))
		{
			comboPin[0] = "28";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 6))
		{
			comboPin[0] = "29";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 7)){
		
			comboPin[0] = "30";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 8))
		{
			comboPin[0] = "31";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 9))
		{
			comboPin[0] = "32";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 10))
		{
			comboPin[0] = "33";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 11))
		{
			comboPin[0] = "34";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 12))
		{	
			comboPin[0] = "35";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 13))
		{
			comboPin[0] = "36";
			comboPin[1] = "100";
		}
		else if ((chipNum == 2) && (channel == 14))
		{
			comboPin[0] = "37";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 15))
		{
			comboPin[0] = "38";
			comboPin[1] = "100";
		} else if ((chipNum == 2) && (channel == 16))
		{
			comboPin[0] = "39";
			comboPin[1] = "0";
		} else if ((chipNum == 2) && (channel == 17))
		{
			comboPin[0] = "40";
			comboPin[1] = "1";
		} else if ((chipNum == 2) && (channel == 18))
		{
			comboPin[0] = "41";
			comboPin[1] = "2";
		} else if ((chipNum == 2) && (channel == 19))
		{
			comboPin[0] = "42";	
			comboPin[1] = "3";
		} else if ((chipNum == 2) && (channel == 20))
		{
			comboPin[0] = "43";
			comboPin[1] = "4";
		} else if ((chipNum == 2) && (channel == 21))
		{
			comboPin[0] = "44";
			comboPin[1] = "5";
		} else if ((chipNum == 2) && (channel == 22))
		{
			comboPin[0] = "45";
			comboPin[1] = "6";
		} else if ((chipNum == 2) && (channel == 23))
		{
			comboPin[0] = "46";
			comboPin[1] = "7";
		} else if ((chipNum == 2) && (channel == 24)) // 32 channels begin here
		{
			comboPin[0] = "47";
			comboPin[1] = "8";
		} else if ((chipNum == 2) && (channel == 25))
		{
			comboPin[0] = "48";
			comboPin[1] = "9";
		} else if ((chipNum == 2) && (channel == 26)) 
		{
			comboPin[0] = "49";
			comboPin[1] = "10";
		} else if ((chipNum == 2) && (channel == 27))
		{
			comboPin[0] = "50";
			comboPin[1] = "11";
		}else if((chipNum == 3) && (channel == 4))
		{
			comboPin[0] = "51";
			comboPin[1] = "12";
		} else if ((chipNum == 3) && (channel == 5))
		{
			comboPin[0] = "52";
			comboPin[1] = "13";
		} else if ((chipNum == 3) && (channel == 6))
		{
			comboPin[0] = "53";
			comboPin[1] = "14";
		} else if ((chipNum == 3) && (channel == 7))
		{
			comboPin[0] = "54";
			comboPin[1] = "15";
		} else if ((chipNum == 3) && (channel == 8))
		{
			comboPin[0] = "55";
			comboPin[1] = "16";
		} else if ((chipNum == 3) && (channel == 9))
		{
			comboPin[0] = "56";
			comboPin[1] = "17";
		} else if ((chipNum == 3) && (channel == 10))
		{
			comboPin[0] = "57";
			comboPin[1] = "18";
		} else if ((chipNum == 3) && (channel == 11))
		{
			comboPin[0] = "58";
			comboPin[1] = "19";
		} else if ((chipNum == 3) && (channel == 12))
		{	
			comboPin[0] = "59";
			comboPin[1] = "20";
		} else if ((chipNum == 3) && (channel == 13))
		{
			comboPin[0] = "60";
			comboPin[1] = "21";
		}
		else if ((chipNum == 3) && (channel == 14))
		{
			comboPin[0] = "61";
			comboPin[1] = "22";
		} else if ((chipNum == 3) && (channel == 15))
		{
			comboPin[0] = "62";
			comboPin[1] = "23";
		} else if ((chipNum == 3) && (channel == 16))  //inactive input
		{
			comboPin[0] = "63";
			comboPin[1] = "100";
		} else if ((chipNum == 3) && (channel == 17))  //inactive input
		{
			comboPin[0] = "64";
			comboPin[1] = "100";
		} else if ((chipNum == 3) && (channel == 18))  //inactive input
		{
			comboPin[0] = "65";
			comboPin[1] = "100";
		} else if ((chipNum == 3) && (channel == 19))  //inactive input
		{
			comboPin[0] = "66";
			comboPin[1] = "100";
		} else if ((chipNum == 3) && (channel == 20))
		{
			comboPin[0] = "67";
			comboPin[1] = "100";
		} else if ((chipNum == 3) && (channel == 21))
		{
			comboPin[0] = "68";
			comboPin[1] = "100";
		} else if ((chipNum == 3) && (channel == 22))
		{
			comboPin[0] = "69";
			comboPin[1] = "100";
		} else if ((chipNum == 3) && (channel == 23))
		{
			comboPin[0] = "70";
			comboPin[1] = "100";
		} else if ((chipNum == 3) && (channel == 24)) 
		{
			comboPin[0] = "71";
			comboPin[1] = "100"; 
		} else if ((chipNum == 3) && (channel == 25)) 
		{
			comboPin[0] = "72";
			comboPin[1] = "100";
		} else if ((chipNum == 3) && (channel == 26))
		{
			comboPin[0] = "73";
			comboPin[1] = "100";
		} else if ((chipNum == 3) && (channel == 27)) 
		{ 
			comboPin[0] = "74";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 4))
		{
			comboPin[0] = "75";
			comboPin[1] = "24";
		} else if ((chipNum == 4) && (channel == 5))
		{
			comboPin[0] = "76";
			comboPin[1] = "25";
		} else if ((chipNum == 4) && (channel == 6))
		{
			comboPin[0] = "77";
			comboPin[1] = "26";
		} else if ((chipNum == 4) && (channel == 7)){
		
			comboPin[0] = "78";
			comboPin[1] = "27";
		} else if ((chipNum == 4) && (channel == 8))
		{
			comboPin[0] = "79";
			comboPin[1] = "28";
		} else if ((chipNum == 4) && (channel == 9))
		{
			comboPin[0] = "80";
			comboPin[1] = "29";
		} else if ((chipNum == 4) && (channel == 10))
		{
			comboPin[0] = "81";
			comboPin[1] = "30";
		} else if ((chipNum == 4) && (channel == 11))
		{
			comboPin[0] = "82";
			comboPin[1] = "31";
		} else if ((chipNum == 4) && (channel == 12)) //inactive input below here
		{	
			comboPin[0] = "83";
			comboPin[1] = "100";	
		} else if ((chipNum == 4) && (channel == 13))
		{
			comboPin[0] = "84";
			comboPin[1] = "100";
		}
		else if ((chipNum == 4) && (channel == 14))
		{
			comboPin[0] = "85";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 15))
		{
			comboPin[0] = "86";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 16))
		{
			comboPin[0] = "87";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 17))
		{
			comboPin[0] = "88";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 18))
		{
			comboPin[0] = "89";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 19))
		{
			comboPin[0] = "90";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 20))
		{
			comboPin[0] = "91";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 21))
		{
			comboPin[0] = "93";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 22))
		{
			comboPin[0] = "94";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 23))
		{
			comboPin[0] = "95";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 24))
		{
			comboPin[0] = "96";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 25))
		{
			comboPin[0] = "97";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 26)) 
		{
			comboPin[0] = "98";
			comboPin[1] = "100";
		} else if ((chipNum == 4) && (channel == 27))
		{
			comboPin[0] = "99";
			comboPin[1] = "100";
		}
		else if ((chipNum == 5) && (channel == 4))
		{
			comboPin[0] = "100";
			comboPin[1] = "100";
		} else if ((chipNum == 5) && (channel == 5))
		{
			comboPin[0] = "101";
			comboPin[1] = "100";
		} else if ((chipNum == 5) && (channel == 6))
		{
			comboPin[0] = "102";
			comboPin[1] = "100";
		} else if ((chipNum == 5) && (channel == 7))
		{
			comboPin[0] = "103";
			comboPin[1] = "100";
		} 
		else
		{ 
			comboPin[0] = "-1";
			comboPin[1] = "100";
		}
		//System.out.println("comboPin[0] is: " + comboPin);
		return comboPin;
	}
	/*--------------------------------------------------------------------------------------------------*/
	
	
	/**
	 * Continually runs as long as the thread's threadAlive status is true. This performs the logic to connect to a device, request data,
	 * close down the connection, and sleep for a the calculated sleep time.
	 */
	public void run() 
	{	
		/*
		try
        {
            JFileChooser log170File = new JFileChooser();   // Pop-up dialog box to name file and save it in desired location
            int retVal = log170File.showSaveDialog(null);

            if (retVal == JFileChooser.APPROVE_OPTION) {
                dir = new FileWriter(log170File.getSelectedFile());
            }
            buff = new BufferedWriter(dir);                 // Buffer in which headers and data is written
        }
        catch (IOException ie) {
            ie.printStackTrace();
        }
		*/
		
		// Run as long as the thread is alive
		while (this.threadAlive)
		{
			try
			{
				// Attempt to connect to the device.
				if (super.connectToDevice())
				{
					// Updates time variables 
					getClockTime();
					// Send the data command to the device. If it is a test device, send the TIME command, otherwise send the DATA command
					if (this.testDevice)
						out.println(COMMAND_TIME);
					else  //Send one of the commands below; be sure to have only one command active a time 
						out.println(COMMAND_DATA);								 // Use this command to retrieve data from Rabbit			
						//out.println(COMMAND_RENEW + hour + minute + second);   // Use this command to update RTC of Rabbit
						//out.println(COMMAND_READ);							 // Use this command to read RTC of Rabbit
					
					// Read and store the data returned from the device
					String data = in.readLine();
					 
			       // buff.write(data);    
			       // buff.newLine();   
					
					/*--------------------------------------------------------------------------------------------------*/	
					// Updates year, month, day to build C1_date so that data has a date for each piece of data
					Calendar cal = Calendar.getInstance();
					int year = cal.get(Calendar.YEAR);					// Acquire current year
					int month = cal.get(Calendar.MONTH) + 1;			// Acquire current month
					int day = cal.get(Calendar.DAY_OF_MONTH);			// Acquire current day
					String C1_date = year + "-" + month + "-" + day;	// YYYY-MM-DD
					/*--------------------------------------------------------------------------------------------------*/
					
					System.out.println("Request sent");
					System.out.println(data);			// Outputs data retrieved from client onto console. Good tool for debugging
					if (data.isEmpty() != true)			// Only if data is received, do work 
					{
						dataIn = data.split(criteria);							// Data goes to an array identifying each element separated by a " "
						
						for (int i = 0; i < dataIn.length-1; i++)				// Loop through all data received
						{
							if (i % 4 == 0)										// Getting chip number
							{
								schipNum = dataIn[i];
								cNum = schipNum;
								System.out.println("chipNum: " + schipNum);
								count++;
							} else if ((i-1) % 4 == 0) {						// Getting channel
								schannel = dataIn[i];
								cChan = schannel;
								System.out.println("channel is: " + schannel);							
								count++;
							} else if ((i-2) % 4 == 0) {						// Getting state
								state = dataIn[i];								// state initially a String
								cState = state;
								System.out.println("state is: " + state);	
								count++;
							} else if ((i-3) % 4 == 0) {						// Getting elapsed milliseconds since previous midnight
								milliDay = dataIn[i];
								Milli = Integer.parseInt(milliDay);
								System.out.println("Time: " + Milli);
								
								// These variables used to compute ticks
								int hourMilli, minMilli, min;
								int secMilli, sec, milliTimer;
								
								// Following calculations broken apart into several lines so that it is easy to follow. NOTE: constants (magic numbers) below represent numbers to convert to different units of time
								hourMilli = Milli / 3600000;
								minMilli = Milli - (hourMilli * 3600000);
								min = minMilli / 60000;
								secMilli = minMilli - (min * 60000);
								sec = secMilli / 1000;
								milliTimer = secMilli - (sec * 1000);
								numTick = milliTimer/17;						// milliseconds elapsed since previous midnight. Every 17ms is a tick; as a result, divide that result by 17
								count++;
							}
							
							if (count == 2)
							{
								comb = getComboPin();
								scomb = comb[0];
								log170chan = comb[1];
							}
							
							try {
								
								if (count == 4)
								{
									lMillis = Double.parseDouble(milliDay);
									//System.out.println("lMilli: " + String.format("%.4f", lMillis) );
									//System.out.println("milliDay: " + milliDay);
									unixTime = getUnixTime() + lMillis;
									//tTime = getUnixTime();
									//String uTime = String.format("%.4f",unixTime);
									
									//uTime.format("%18.0f", uTime);
									//System.out.println("Previous midnight: " + getUnixTime() );
									//System.out.println("uTime " + String.format("%.4f", unixTime) );
									//System.out.println("unixTime: " + String.format("%.4f",unixTime));
									//System.out.println("added: " + uTime);
									
									//floatTime = Float.toString(unixTime);
									floatTime = Double.toString(unixTime);
									
									PreparedStatement addC1 = this.db.getConnection().prepareStatement(PreparedStatementStatics.INSERT_C1_DATA);
								
									addC1.setString(1, schipNum);
									addC1.setString(2, schannel);
									addC1.setString(3, scomb);
									addC1.setString(4, log170chan);
									addC1.setString(5, state);
									addC1.setInt(6, numTick);
									addC1.setString(7, milliDay);
									addC1.setString(8, floatTime);
									addC1.setString(9, C1_date);
								
									addC1.addBatch();
								
									addC1.executeBatch();
									addC1.close();
									addC1 = null;
									count = 0;
									
									
									
								}
							}
							catch (SQLException e)
							{
								System.out.println("Something wrong with SQL");
								e.printStackTrace();
							}
							catch (NullPointerException ne)
							{
								System.out.println("No data");
							}
						}
					}
					
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
				
				// Have the thread sleep
				C1DeviceThread.sleep(sleep * 1000);
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
}
