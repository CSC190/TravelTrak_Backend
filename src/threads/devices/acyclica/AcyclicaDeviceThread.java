/*package threads.devices.acyclica;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import statics.Globals;
import threads.LoggerThread;
import threads.devices.DeviceDaemon;
import threads.devices.DeviceThread;
import threads.processing.DataProcessingDaemon;

public class AcyclicaDeviceThread extends DeviceThread 
{
	private long startTime;
	private long endTime;
	
	private int hourAmount = 0;
	
	public AcyclicaDeviceThread(DataProcessingDaemon dpd, LoggerThread lt, int did, String unid, String url, int serialID, DeviceDaemon d, String api, boolean fastQuery)
	{
		super(dpd, lt, did, unid, url, serialID, d, api, fastQuery);
		
		startTime = (System.currentTimeMillis() / 1000) - (hourAmount + 300); // 5 minutes in the past
	}
	
	public void run() 
	{
		while (this.threadAlive)
		{
			try
			{
				if (super.fastQuery)
				{
					endTime = startTime + 10;
				}
				else
				{
					endTime = (System.currentTimeMillis() / 1000) - hourAmount;
				}
				
				File xmlFile = super.connectToServer(startTime, endTime);
				
				if (xmlFile == null)
				{
					createAndSendLogData("Info", "Failed to establish connection to Acyclica Server. Failed Consecutive Attempt #" + this.consecutiveFailedConnectionAttempts);
				}
				else
				{
					Document doc;
					try {
						
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						doc = dBuilder.parse(xmlFile);
						doc.getDocumentElement().normalize();
				 		 
						NodeList nList = doc.getElementsByTagName("Data");
				 		
						String formattedData = "WF";
												
						for (int temp = 0; temp < nList.getLength(); temp++) 
						{
							Node nNode = nList.item(temp);
							 
							if (nNode.getNodeType() == Node.ELEMENT_NODE)
							{
								Element eElement = (Element) nNode;
								 
								String macArray[] = eElement.getElementsByTagName("Mac").item(0).getTextContent().split(":");
								String time = eElement.getElementsByTagName("Time").item(0).getTextContent();
								Long timestamp = Long.parseLong(time.substring(0, time.indexOf(".")));
								
								String mac = String.format("%s%s%s%s%s%s", macArray[0], macArray[1], macArray[2], macArray[3], macArray[4], macArray[5]);
								
								Calendar cal = Calendar.getInstance();
								cal.setTimeInMillis(timestamp * 1000);
								
								String finTime = String.format("%02d%02d%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
																
								formattedData += String.format(",%s*%s", mac.toUpperCase(), finTime);
							}	
						}
						
						formattedData += ",E";
						
						sendDataForProcessing(formattedData);
						
						if (!xmlFile.delete())
						{
							System.err.println("Failed to delete XML file...");
						}
					} 
					catch (SAXException e) 
					{
						e.printStackTrace();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					} catch (ParserConfigurationException e) 
					{
						e.printStackTrace();
					}
					
				}
				
				startTime = endTime;
				
				
				if (super.fastQuery)
				{
					AcyclicaDeviceThread.sleep(5000);
				}
				else
				{
					AcyclicaDeviceThread.sleep(Globals.BLUETOOTH_SLEEP_TIME);
				}
			}
			catch (InterruptedException e) 
			{
				createAndSendLogData("Error", "Device " + super.getUniqueID() + " failed to go to sleep");
			}			
		}
	}
}*/
