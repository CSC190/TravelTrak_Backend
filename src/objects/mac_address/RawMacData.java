package objects.mac_address;

public class RawMacData 
{
	private int nodeID;
	private String deviceData;
	
	public RawMacData(int nID, String data)
	{
		this.nodeID = nID;
		this.deviceData = data;
	}
	
	public int getDeviceID()
	{
		return this.nodeID;
	}
	
	public String getDeviceData()
	{
		return this.deviceData;
	}
	
	public String toString()
	{
		return String.format("Node #%d: Data '%s'", this.nodeID, this.deviceData);
	}
}
