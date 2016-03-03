package objects.mac_address;

import java.util.Vector;

public class NodeSessionData 
{
	private int nodeID;
	private Vector<MacAddressData> macData;
	private Vector<MacAddressData> macSessions;
	
	
	public NodeSessionData(int nodeID)
	{
		this.nodeID = nodeID;
		
		macData 	= new Vector<MacAddressData>();
		macSessions = new Vector<MacAddressData>();
	}
	
	
	public int getNodeID()
	{
		return this.nodeID;
	}
	
	public void addMacData(MacAddressData m)
	{
		this.macData.add(m);
	}
	
	public void removeMacDataBeforeTimeValue(long timeValue)
	{
		for (MacAddressData m : macData)
		{
			if (m.getTimeStamp() < timeValue)
			{
				m.delete();
			}
		}
		
		for (int i = macData.size() - 1; i > 0; i--)
		{
			macData.remove(i);
		}
	}
	
	public int getSessionDataSize()
	{
		return this.macData.size();
	}
	
	public Vector<MacAddressData> getMacData()
	{
		return this.macData;
	}
	
	public void addMacAsSession(MacAddressData mad)
	{
		macSessions.add(mad);
	}
	
	public Vector<MacAddressData> getMacSessions()
	{
		return this.macSessions;
	}
}
