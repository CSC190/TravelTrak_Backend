package objects.mac_address;

import java.util.Collections;
import java.util.Vector;

public class TravelTimeSegment 
{
	private int nodeID1;
	private int nodeID2;
	private int baseTravelTime;
	private int currentTravelTime;
	
	private Vector<MacAddressTravelTimePair> segmentEvents;
	
	public TravelTimeSegment(int n1, int n2, int btt)
	{
		this.nodeID1 = n1;
		this.nodeID2 = n2;
		this.baseTravelTime = btt;
		this.currentTravelTime = btt;
		
		this.segmentEvents = new Vector<MacAddressTravelTimePair>();
	}
	
	public int getNodeID1()
	{
		return this.nodeID1;
	}
	
	public int getNodeID2()
	{
		return this.nodeID2;
	}
	
	public int getBaseTravelTime()
	{
		return this.baseTravelTime;
	}
	
	public int getCurrentTravelTime()
	{
		return this.currentTravelTime;
	}
	
	public void updateCurrentTravelTime(int newTravelTime)
	{
		this.currentTravelTime = newTravelTime;
	}
	
	public void addNewPair(MacAddressTravelTimePair bttp)
	{
		this.segmentEvents.add(bttp);
	
		Collections.sort(this.segmentEvents);
	}	
}
