package objects.mac_address;

public class MacAddressData implements Comparable<MacAddressData>
{
	private int nodeID;
	private long uniqueID;
	private boolean delete;
	private long timestamp;
	private String macAddress;
	private boolean session;
	
	private int sessionHitCount;
		
	private boolean sentToOutlierProcessor;
	
	public MacAddressData(int nID, long uID, long ts, String mac)
	{
		this.delete = false;
		this.session = false;
	
		this.nodeID = nID;
		this.uniqueID = uID;
		this.timestamp = ts;
		this.macAddress = mac;
		
		this.sessionHitCount = 0;
		
		this.sentToOutlierProcessor = false;
	}
	
	public MacAddressData(int nID, long ts, String mac)
	{
		this.delete = false;
		this.session = false;
		
		this.nodeID = nID;
		this.timestamp = ts;
		this.macAddress = mac;
		
		this.sessionHitCount = 0;
		this.uniqueID = -1;

		this.sentToOutlierProcessor = false;
	}
	
	public String toString()
	{
		return String.format("[Mac: %s; Sensor: %d; Timestamp: %d; Hit Counter: %d]", this.macAddress, nodeID, timestamp, sessionHitCount);
	}
	
	public void setUniqueID(long uID)
	{
		this.uniqueID = uID;
	}
	
	public void sentToOutlierProcessor()
	{
		this.sentToOutlierProcessor = true;
	}
	
	public boolean wasSentToOutlierProcessor()
	{
		return this.sentToOutlierProcessor;
	}
	
	public int getNodeID()
	{
		return this.nodeID;
	}
	
	public void delete()
	{
		this.delete = true;
	}
	
	public boolean isDeleted()
	{
		return this.delete;
	}
	
	public long getTimeStamp()
	{
		return this.timestamp;
	}
	
	
	public String getMacAddress()
	{
		return this.macAddress;
	}

	public void setSession()
	{
		this.session = true;
	}
	
	public boolean isSession()
	{
		return this.session;
	}
	
	public void increaseSessionHitCount()
	{
		this.sessionHitCount++;
	}
	
	public int getSessionHitCount()
	{
		return this.sessionHitCount;
	}
	
	public long getUniqueID()
	{
		return this.uniqueID;
	}

	public int compareTo(MacAddressData other) 
	{
		return (int)(this.getTimeStamp() - other.getTimeStamp());
	}
}
