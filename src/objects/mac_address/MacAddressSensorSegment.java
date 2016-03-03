package objects.mac_address;

import java.util.Calendar;
import java.util.Collections;
import java.util.Vector;

import objects.LogItem;
import threads.LoggerThread;

public class MacAddressSensorSegment 
{	
	private int sensor1ID;
	private int sensor2ID;
	private float distance;
	private int freeFlowTravelTime;
	private int currentTravelTimeDirection1;
	private int currentTravelTimeDirection2;
	private LoggerThread lt;
	
	public MacAddressSensorSegment(LoggerThread lt, int sID, int dID, float dist, int fftt)
	{
		this.lt = lt;
		
		this.sensor1ID = sID;
		this.sensor2ID = dID;
		this.distance = dist;
		this.freeFlowTravelTime = fftt;
		this.currentTravelTimeDirection1 = fftt;
		this.currentTravelTimeDirection2 = fftt;
	
		direction1Data = new Vector<MacAddressTravelTimePair>();
		direction2Data = new Vector<MacAddressTravelTimePair>();
		
	}
	
	public void updateCurrentTravelTimeDirection1(int cTT)
	{
		this.currentTravelTimeDirection1 = cTT;
	}
	
	public int getCurrentTravelTimeDirection1()
	{
		return this.currentTravelTimeDirection1;
	}

	public void updateCurrentTravelTimeDirection2(int cTT)
	{
		this.currentTravelTimeDirection2 = cTT;
	}
	
	public int getCurrentTravelTimeDirection2()
	{
		return this.currentTravelTimeDirection2;
	}
	
	public int getSensor1ID()
	{
		return this.sensor1ID;
	}
	
	public int getSensor2ID()
	{
		return this.sensor2ID;
	}
	
	public float getDistance()
	{
		return this.distance;
	}
	
	public int getFreeFlowTravelTime()
	{
		return this.freeFlowTravelTime;
	}
	
	public String toString()
	{
		return String.format("Sensor Segment: %d -> %d, Distance: %.2fmi, Free Flow Travel Time: %ds, \n\t\t[%d -> %d Current Time: %ds] \t[%d -> %d Current Time: %ds]", sensor1ID, sensor2ID, distance, freeFlowTravelTime, sensor1ID, sensor2ID, currentTravelTimeDirection1, sensor2ID, sensor1ID, currentTravelTimeDirection2);
	}
	
	// Added in to support more efficient real time Calculations of data....
	private double upperRangeValue = 1.7;
	private double lowerRangeValue = .7;
	
	private Vector<MacAddressTravelTimePair> direction1Data;
	private Vector<MacAddressTravelTimePair> direction2Data;
	
	public MacAddressTravelTimePair calculateDirection1(MacAddressTravelTimePair mattp)
	{
		String logMessage = "\n------------------------------------------------------------------------\n";
		logMessage += String.format("Direction %d -> %d Calculation\n", sensor1ID, sensor2ID);
		
		Vector<MacAddressTravelTimePair> toRemove = new Vector<MacAddressTravelTimePair>();
		
		logMessage += String.format("Current Elements in data array (current size is %d)\n", direction1Data.size());

		Collections.sort(direction1Data);

		if (direction1Data.size() > 5)
		{
			long oldestTimestampInData = direction1Data.elementAt(direction1Data.size() - 1).getTimeStamp();
			
			int timeSum = this.getCurrentTravelTimeDirection1();
			int elements = 1;
							
			logMessage += String.format("Mac\t\t\t\tEvent TS,\t\tDur,\tOldest TS\t\tDifference\n");
			for (MacAddressTravelTimePair m : direction1Data)
			{
				logMessage += String.format("%s\t%d\t%d\t%d\t\tDifference: %d", m.getEvent1().getMacAddress(), m.getTimeStamp(), m.getDuration(), (oldestTimestampInData - 60), (m.getTimeStamp() - (oldestTimestampInData - 60)));
				logMessage += String.format(" Can you see this timestamp %d", m.getTimeStamp());  //TESTTESTTEST
				
				if (oldestTimestampInData - 60 > m.getTimeStamp() && (direction1Data.size() - toRemove.size()) > 5)
				{
					toRemove.add(m);
					logMessage += " - Removed";
				}
				else
				{
					timeSum += m.getDuration();
					elements++;
				}
				
				logMessage += "\n";
			}
			
			int avg = timeSum / elements;
			
			logMessage += String.format("Updating travel time average to %d\n", avg);
			
			this.updateCurrentTravelTimeDirection1(avg);
			
			logMessage += String.format("Removing %d elements from data array\n", toRemove.size());
			
			for (MacAddressTravelTimePair m : toRemove)
				direction1Data.remove(m);
			
			logMessage += String.format("New data array size: %d\n", direction1Data.size());
			logMessage += String.format("Current Elements in data array\n");
			for (MacAddressTravelTimePair m : direction1Data)
			{
				logMessage += String.format("%s, %d, %d\n", m.getEvent1().getMacAddress(), m.getTimeStamp(), m.getDuration());
			}
			
			logMessage += String.format("New Estimated Travel Time Calculation: %d\n", this.currentTravelTimeDirection1);

		}
		else
		{
			for (MacAddressTravelTimePair m : direction1Data)
			{
				logMessage += String.format("%s, %d, %d\n", m.getEvent1().getMacAddress(), m.getTimeStamp(), m.getDuration());
			}

			logMessage += String.format("Not enough elements to re-calculate current travel time. Travel time will remain at %d\n", this.currentTravelTimeDirection1);
		}
		
		float currentLowerBound = (float) (getCurrentTravelTimeDirection1() * this.lowerRangeValue);
		float currentUpperBound = (float) (getCurrentTravelTimeDirection1() * this.upperRangeValue);
				
		logMessage += String.format("\nUpper and Lower bounds for durations: %.2f & %.2f\n", currentLowerBound, currentUpperBound);

		String csvString = "";
		if (currentLowerBound < mattp.getDuration() && mattp.getDuration() < currentUpperBound)
		{
			logMessage += String.format("%s at %d with time %d is valid.\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration());

			direction1Data.add(mattp);
			csvString = String.format("%s,%d,%d,valid,%.2f,%.2f\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration(), currentLowerBound, currentUpperBound);			
			this.createAndSendLogData(false, String.format("Link_Segment_%d-%d", getSensor1ID(), getSensor2ID()), "CSV", csvString);

//					FileWriter fw = new FileWriter(direction2CSV, true);
//					fw.write(String.format("%s, %d, %d, valid\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration()));
//					fw.close();
			logMessage += "------------------------------------------------------------------------\n";
			
			this.createAndSendLogData(false, String.format("Link_Segment_%d-%d", this.sensor1ID, this.sensor2ID), "Info", logMessage);

			mattp.updateTripInformation(true, currentLowerBound, currentUpperBound);
			
			
			
			return mattp;
		}
		else
		{
			logMessage += String.format("%s at %d with time %d is invalid.\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration());
			csvString = String.format("%s,%d,%d,invalid\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration());			
			this.createAndSendLogData(false, String.format("Link_Segment_%d-%d", getSensor1ID(), getSensor2ID()), "CSV", csvString);

//					FileWriter fw = new FileWriter(direction2CSV, true);
//					fw.write(String.format("%s, %d, %d, invalid\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration()));
//					fw.close();
//					
			
			logMessage += "------------------------------------------------------------------------\n";
			
			this.createAndSendLogData(false, String.format("Link_Segment_%d-%d", this.sensor1ID, this.sensor2ID), "Info", logMessage);
			
			mattp.updateTripInformation(false, currentLowerBound, currentUpperBound);

			return mattp;
		}
	}
	
	public MacAddressTravelTimePair calculateDirection2(MacAddressTravelTimePair mattp)
	{
		String logMessage = "\n------------------------------------------------------------------------\n";
		logMessage += String.format("Direction %d -> %d Calculation\n", sensor2ID, sensor1ID);
		
		Vector<MacAddressTravelTimePair> toRemove = new Vector<MacAddressTravelTimePair>();
		
		logMessage += String.format("Current Elements in data array (current size is %d)\n", direction2Data.size());

		Collections.sort(direction2Data);
		
		if (direction2Data.size() > 5)
		{
			long oldestTimestampInData = direction2Data.elementAt(direction2Data.size() - 1).getTimeStamp();
			int timeSum = this.getCurrentTravelTimeDirection2();
			int elements = 1;
							
			logMessage += String.format("Mac\t\t\t\tEvent TS,\t\tDur,\tOldest TS\t\tDifference\n");
			for (MacAddressTravelTimePair m : direction2Data)
			{
				logMessage += String.format("%s\t%d\t%d\t%d\t\tDifference: %d", m.getEvent1().getMacAddress(), m.getTimeStamp(), m.getDuration(), (oldestTimestampInData - 60), (m.getTimeStamp() - (oldestTimestampInData - 60)));
				
				if (oldestTimestampInData - 60 > m.getTimeStamp() && (direction2Data.size() - toRemove.size()) > 5)
				{
					toRemove.add(m);
					logMessage += "\t Removed";
				}
				else
				{
					timeSum += m.getDuration();
					elements++;
				}
				
				logMessage += "\n";
			}
			
			int avg = timeSum / elements;
			
			logMessage += String.format("Updating travel time average to %d\n", avg);

			this.updateCurrentTravelTimeDirection2(avg);
			
			logMessage += String.format("Removing %d elements from data array\n", toRemove.size());
			for (MacAddressTravelTimePair m : toRemove)
				direction2Data.remove(m);
			
			logMessage += String.format("New data array size: %d\n", direction2Data.size());
			logMessage += String.format("Current Elements in data array\n");
			for (MacAddressTravelTimePair m : direction2Data)
			{
				logMessage += String.format("%s, %d, %d\n", m.getEvent1().getMacAddress(), m.getTimeStamp(), m.getDuration());
			}
			
			logMessage += String.format("New Estimated Travel Time Calculation: %d\n", this.currentTravelTimeDirection2);

		}
		else
		{
			for (MacAddressTravelTimePair m : direction2Data)
			{
				logMessage += String.format("%s, %d, %d\n", m.getEvent1().getMacAddress(), m.getTimeStamp(), m.getDuration());
			}

			logMessage += String.format("Not enough elements to re-calculate current travel time. Travel time will remain at %d\n", this.currentTravelTimeDirection2);
		}
		
		float currentLowerBound = (float) (getCurrentTravelTimeDirection2() * this.lowerRangeValue);
		float currentUpperBound = (float) (getCurrentTravelTimeDirection2() * this.upperRangeValue);
				
		logMessage += String.format("\nUpper and Lower bounds for durations: %.2f & %.2f\n", currentLowerBound, currentUpperBound);

		String csvString = "";
		
		if (currentLowerBound < mattp.getDuration() && mattp.getDuration() < currentUpperBound)
		{
			logMessage += String.format("%s at %d with time %d is valid.\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration());

			direction2Data.add(mattp);
			csvString = String.format("%s,%d,%d,valid,%.2f,%.2f\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration(), currentLowerBound, currentUpperBound);			
			this.createAndSendLogData(false, String.format("Link_Segment_%d-%d", getSensor2ID(), getSensor1ID()), "CSV", csvString);

//				FileWriter fw = new FileWriter(direction2CSV, true);
//				fw.write(String.format("%s, %d, %d, valid\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration()));
//				fw.close();
			logMessage += "------------------------------------------------------------------------\n";
			
			this.createAndSendLogData(false, String.format("Link_Segment_%d-%d", this.sensor2ID, this.sensor1ID), "Info", logMessage);

			mattp.updateTripInformation(true, currentLowerBound, currentUpperBound);

			return mattp;
		}
		else
		{
			logMessage += String.format("%s at %d with time %d is invalid.\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration());
			csvString = String.format("%s,%d,%d,invalid\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration());			
			this.createAndSendLogData(false, String.format("Link_Segment_%d-%d", getSensor2ID(), getSensor1ID()), "CSV", csvString);

//				FileWriter fw = new FileWriter(direction2CSV, true);
//				fw.write(String.format("%s, %d, %d, invalid\n", mattp.getEvent1().getMacAddress(), mattp.getTimeStamp(), mattp.getDuration()));
//				fw.close();
//				
			
			logMessage += "------------------------------------------------------------------------\n";
			
			this.createAndSendLogData(false, String.format("Link_Segment_%d-%d", this.sensor2ID, this.sensor1ID), "Info", logMessage);
			
			mattp.updateTripInformation(false, currentLowerBound, currentUpperBound);

			return mattp;
		}
	}
	
	
	
	
	
	private void createAndSendLogData(boolean print, String title, String type, String message)
	{
		LogItem li = new LogItem(title, message, type, Calendar.getInstance());
		this.lt.addToList(li);

		if (print)
			System.out.println(li.getMessage());
	}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
