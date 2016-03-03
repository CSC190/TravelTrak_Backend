package objects.mac_address;

import threads.processing.mac_address_core.MacAddressTravelTimeProcessorSubThread;

public class MacAddressTravelTimePair implements Comparable<MacAddressTravelTimePair>
{
	private MacAddressData event1, event2;
	
	private long timestamp;
	private int duration;
	private boolean valid = false;
	private double upperBound;
	private double lowerBound;

	
	private MacAddressTravelTimeProcessorSubThread sender;
	
	public MacAddressTravelTimePair(MacAddressData e1, MacAddressData e2, MacAddressTravelTimeProcessorSubThread sndr)
	{
		this.event1 = e1;
		this.event2 = e2;
		this.sender = sndr;
		this.upperBound = 0.0;
		this.lowerBound = 0.0;
		this.valid = false;
		
		this.timestamp = this.event2.getTimeStamp();
		
		this.duration = (int)(this.event2.getTimeStamp() - this.event1.getTimeStamp()) / 1000;
		
	}	
	
	public void updateTripInformation(boolean valid, double lower, double upper)
	{
		this.valid = valid;
		this.lowerBound = lower;
		this.upperBound = upper;
	}
	
	public boolean isValid()
	{
		return this.valid;
	}
	
	public double getUpperBound()
	{
		return this.upperBound;
	}
	
	public double getLowerBound()
	{
		return this.lowerBound;
	}
	
	public MacAddressTravelTimeProcessorSubThread getSender()
	{
		return this.sender;
	}
	
	public MacAddressData getEvent1()
	{
		return this.event1;
	}
	
	public MacAddressData getEvent2()
	{
		return this.event2;
	}
	
	public long getTimeStamp()
	{
		return this.timestamp;
	}
	
	public int getDuration()
	{
		return this.duration;
	}
	
	public int compareTo(MacAddressTravelTimePair other) 
	{
		return (int)(this.getTimeStamp() - other.getTimeStamp());
	}
	
	public String toString()
	{
		return String.format("Travel Time Pair Information: %s, %d, %d, Sender: %s", this.event1.getMacAddress(), this.timestamp, this.duration, this.sender.getName());
	}
}
