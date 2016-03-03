package objects.mac_address;

public class UniqueMacAddress
{
	private String macAddress;
	private long uniqueID;
	private int hitCount;
	
	public UniqueMacAddress(String m, int hc)
	{
		this.macAddress = m;
		this.uniqueID = System.currentTimeMillis();
		this.hitCount = hc;
	}
	
	public String getMacAddress()
	{
		return this.macAddress;
	}
	
	public int getHitCount()
	{
		return this.hitCount;
	}
	
	public void increaseHitCount()
	{
		this.hitCount++;
	}
	
	public long getUniqueID()
	{
		return this.uniqueID;
	}
	
	public String toString()
	{
		return String.format("[Mac: %s, UID: %d, Hits: %d]", this.macAddress, this.uniqueID, this.hitCount);
	}
}
