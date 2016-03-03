/**
 * ****************************************************************
 * File: 			LogFileManager.java
 * Date Created:  	Jan 9, 2013
 * Programmer:		Dale Reed
 * 
 * Purpose:			To maintain all appropriate log files associated
 * 					with each of the threads that may be running at
 * 					any given time. This allows for more detailed
 * 					information to be analyzed without having to 
 * 					worry about the console getting all cluttered.
 * 					It uses a FIFO buffer
 * 
 * ****************************************************************
 */

package threads;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

import objects.LogFile;
import objects.LogItem;

public class LoggerThread extends Thread
{
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Log File Manager Variable Declarations
	
	/**
	 * Used in keeping track if the thread is to be shutdown or kept alive. This should only be changed
	 * from shutdownThread() which is invoked from the Device Daemon
	 */
	private boolean threadAlive = false;
	
	/**
	 * Used in specifying what directory the log file is going to be stored in.
	 */
	private String baseDirectory;
	
	/**
	 * Used to keep track of all log files that are currently in use by each of the threads as well as
	 * custom log files that are being maintained. 
	 */
	private Vector<LogFile> logFiles = new Vector<LogFile>();
	
	/**
	 * Used in temporarily storing all log messages as they are received from the various threads. They
	 * are one by one taken out and processed by the addToFile() function.
	 */
	private static Vector<LogItem> logList = new Vector<LogItem>();
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Log File Manager Construction 

	public LoggerThread(String logDirectory)
	{
		String logDir = logDirectory;
		baseDirectory = new File(logDir).getAbsolutePath() + File.separator;
	}


	/**
	 * Construct the Log File Manager with the name of the thread so we can see it be closed when the program terminates
	 * 
	 * @param threadName
	 */
	public LoggerThread(String threadName, String logDirectory)
	{
		// Set the name of this thread.
		this.setName(threadName);
		
		String logDir = logDirectory;
		baseDirectory = new File(logDir).getAbsolutePath() + File.separator;		
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Log File Manager Threading Methods 
	// -- Methods contained here:
	// 		-- run()
 	// 		-- initializeShutdownProcess()

	/**
	 * Every second this thread checks the number of messages to be written and if there is any,
	 * it sends them off to be written and then sleeps.
	 */
	public void run()
	{
		new File(baseDirectory).mkdir();
		
		threadAlive = true;
		
		// This allows the thread to run constantly as long as the thread has not been shutdown.
		while (threadAlive)
		{
			// If the number of log elements in the array has more than 0, add each one to the correct file
			while (logList.size() > 0)
			{				
				// This gets the first element in the list and helps make sure they are written out in sequential order 
				// as they were received from the sending thread.
				try
				{
					addToFile(logList.remove(0));
				}
				catch (Exception e)
				{
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));

					System.out.println(" *** ERROR - Log File Manager - Exception found when getting a log file from the list. Exception Message: '" + e.getMessage() + "' -- " + errors);
				}
			}
						
            // Have the Log File Manager Thread sleep before checking for any new error messages
			try
			{
				LoggerThread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				// The log File Manager Thread does not have its own log file as any errors it produces will most likely cause a crash, so just display the error ot the console.
				System.err.println("__ LogFileManager __ --- An Error while trying to sleep");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Tells the thread to begin shutting down and release all of its resources
	 */
	public void initializeShutdownProcess() 
	{
		this.threadAlive = false;
		
		shutdownThread();
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Log File Manager Access Management 

	/**
	 * Adds a new log element to the list to be written to a log file
	 * 
	 * @param data - A string array containing the log name as well as the message to be written
	 */
	public void addToList(LogItem li)
	{
		logList.add(li);
	}

	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Log File Manager File Writer
	
	/**
	 * Adds the logData to the correct log file
	 * @param logData
	 */
	private void addToFile(LogItem li)
	{		
		// Build up a string representing the file that is to be written based on the LogItem
		// Resulting Format: /logs/__year__/__month__/__day__/__hour__/
		File f = new File(this.baseDirectory + File.separator + li.getYear() + File.separator + li.getMonth() + File.separator + li.getDate() + File.separator + String.format("%02d", li.getHour()) + File.separator + li.getThreadName());
		
		// If the file does not exist, then create the parent directory so the file can be written
		if (!f.exists())
		{			
			// Attempt to make the directory if it does not exist.
			// If the directory is unable to be made, then display an error message
			if (!f.mkdirs())
			{
				System.err.println("Failed to create directory for " + f.getAbsolutePath());
			}
		}
		
		try 
		{
			// Create a @FileWriter object for writing the data to the logs
			FileWriter fw;
			
			// Check the file type being written
			if (li.getType().equals("CSV"))
			{
				// Create the @FileWriter object with the base path and the appropriate file name. 
				// Note that we include the 'true' parameter in the @FileWriter to indicate we are appending the existing file.
				fw = new FileWriter(f.getAbsolutePath() + File.separator + "link-records.csv", true);
				
				// Write the @LogItem message to the file
				fw.write(li.getMessage());
			}
			else
			{
				// Create the @FileWriter object with the base path and the appropriate file name. 
				// Note that we include the 'true' parameter in the @FileWriter to indicate we are appending the existing file.
				fw = new FileWriter(f.getAbsolutePath() + File.separator + li.getType() + ".txt", true); 
				
				// Write the @LogItem message to the file, starting the line with the timestamp of the record
				fw.write(/*li.getCalendar().getTime().toString() + " -- " + */  li.getMessage() + "\n");
				
				/*// Write the @LogItem message to the file, one event per line
				String tempMessage = li.getMessage();
				int eventlen = 15;
				int i = 0;
				if (tempMessage.length()>=2){
					if (tempMessage.charAt(1) == ' '){  // if not null and space present then its a c1 packet
						for (int j=tempMessage.length(); j > 0; j -= eventlen+1){
							fw.write(tempMessage.substring( i*eventlen, (i+1)*eventlen));
							fw.write("\n");
						}
					}
					else{					
						// Write the @LogItem message to the file, starting the line with the timestamp of the record
						fw.write(li.getCalendar().getTime().toString() + " -- " + li.getMessage() + "\n");
					}
				}*/
				/*int z=0;
				while ( z%5==0 ){ // five colums of message per line
					fw.write("\n");
					z++;
				}*/
				//System.out.println("File is being created");
			}
		
			// Close down and deallocate the @FileWriter to prevent memory leaks
			fw.close();
			fw = null;
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Log File Manager Thread Shutdown Management

	/**
	 * Begins the shutdown process for the Log File Thread
	 */
	private void shutdownThread()
	{		
		// Generates a string stating what all was shutdown and the status of the objects released.s
		String shutdownThreadString = "\n"
							 + "********************* THREAD SHUTDOWN PROCESS *********************"
							 + "Shutting down thread " + this.getName()
							 + stopThread()
						 	 + "*******************************************************************"
						 	 + "\n";
		
		// Prints out the result of the thread shutdown process
		System.out.println(shutdownThreadString);
				
		// This kills the thread
		this.interrupt();
	}
	
	/**
	 * Returns a string telling us that the thread has successfully closed down and released all resources
	 * @return
	 */
	private String stopThread()
	{		
		String toReturn = "";
		
		baseDirectory = null;		
		if (baseDirectory == null)
			toReturn += "baseDirectory: null\n";
		else
			toReturn += "baseDirectory: " + baseDirectory.toString() + "\n";
		
		logFiles = null;
		if (logFiles == null)
			toReturn += "logFiles: null\n";
		else
			toReturn += "logFiles: " + logFiles.toString() + "\n";
			
		logList = null;
		if (logList == null)
			toReturn += "logList: null\n";
		else
			toReturn += "logList: " + logList.toString() + "\n";
		
		toReturn +="*******************************************************************"
							 + "\n";
				
		return toReturn;
	}
}
