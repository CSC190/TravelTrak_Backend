package objects.mac_address;

import java.util.Collections;
import java.util.Vector;

public class MacAddressTripInformation {

	private String macAddress;
	private Vector<MacAddressData> bluetoothEvents;
	
	public MacAddressTripInformation(String macAddress, MacAddressData event)
	{
		this.macAddress = macAddress;
		
		this.bluetoothEvents = new Vector<MacAddressData>();
		
		this.bluetoothEvents.add(event);
	}
	
	public String getMacAddress()
	{
		return this.macAddress;
	}
	
	public void addBluetoothEvent(MacAddressData mad)
	{
		this.bluetoothEvents.add(mad);
		
		Collections.sort(bluetoothEvents);
	}
	
	public Vector<MacAddressData> returnBluetoothEvents()
	{
		return this.bluetoothEvents;
	}
	
	public int getBluetoothEventCount()
	{
		return this.bluetoothEvents.size();
	}
	
	public void removeFirstEvent()
	{
		this.bluetoothEvents.remove(0);
	}
	
	public long getOldestTimestamp()
	{
		return this.bluetoothEvents.firstElement().getTimeStamp();
	}
	
	public MacAddressData getOldestEvent()
	{
		return this.bluetoothEvents.firstElement();
	}
	
	public String toString()
	{
		String tripData = String.format("%s Trip information: ", this.macAddress);
		
		for (MacAddressData mad : this.bluetoothEvents)
		{	
			tripData += String.format("\t%s", mad);
		}
		
		return tripData;
	}
	
}
