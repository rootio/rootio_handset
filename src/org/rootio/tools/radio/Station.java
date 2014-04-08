package org.rootio.tools.radio;

import java.net.InetAddress;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

public class Station {
	private String location;
	private String owner;
	private float frequency;
	private String telephoneNumber;
	private String name;
	private StationStatus stationStatus;
	private Context parent;
	private InetAddress multicastIPAddress;
	private int multicastPort;
	private int stationId;
	private String serverKey;

	public Station(Context parent) {
		this.parent = parent;
        this.loadStationInfo();
	}

	public String getLocation() {
		return this.location;
	}

	public String getOwner() {
		return this.owner;
	}

	public String getTelephoneNumber() {
		return this.telephoneNumber;
	}

	public String getName() {
		return this.name;
	}

	public float getFrequency() {
		return this.frequency;
	}

	public StationStatus getStationStatus() {
		return this.stationStatus;
	}
	
	public InetAddress getMulticastIPAddress()
	{
		return this.multicastIPAddress;
	}
	
	public int getMulticastPort()
	{
		return this.multicastPort;
	}
	
	public String getServerKey()
	{
		return this.serverKey;
	}
	
	public int getStationId()
	{
		return this.stationId;
	}
	
	public void setLocation(String location)
	{
		this.location = location;
	}
	
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	
	public void setTelephoneNumber(String telephoneNumber)
	{
		this.telephoneNumber = telephoneNumber;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setFrequency(float frequency)
	{
		this.frequency = frequency;
	}
	
	public void setMulticastIPAddress(InetAddress multicastIPAddress)
	{
		this.multicastIPAddress = multicastIPAddress;
	}
	
	public void setMulticastPort(int port)
	{
		this.multicastPort = port;
	}
	
	public void persist()
	{
		String tableName = "station";
		ContentValues data = new ContentValues();
		data.put("location", this.location);
		data.put("owner", this.owner);
		data.put("telephonenumber", this.telephoneNumber);
		data.put("name", this.name);
		data.put("frequency", this.frequency);
		data.put("multicastipaddress", this.multicastIPAddress.getHostAddress());
		data.put("multicastport", this.multicastPort);
		data.put("stationid", this.stationId);
		data.put("serverkey", this.serverKey);
		DBAgent dbAgent = new DBAgent(this.parent);
		dbAgent.updateRecords(tableName, data, null, null);
	}
	
	private void loadStationInfo() {
		String tableName = "station";
		String[] columnsToFetch = new String[] { "location", "owner", "telephonenumber", "name", "frequency", "multicastipaddress", "multicastport", "stationid", "serverkey" };
		DBAgent dbAgent = new DBAgent(this.parent);
		String[][] stationDetails = dbAgent.getData(true, tableName, columnsToFetch, null, null, null, null, null, null);
		if (stationDetails.length > 0) {
			this.location = stationDetails[0][0];
			this.owner = stationDetails[0][1];
			this.telephoneNumber = stationDetails[0][2];
			this.name = stationDetails[0][3];
			this.multicastPort = Utils.parseIntFromString(stationDetails[0][6]);
			this.frequency = Utils.parseFloatFromString(stationDetails[0][4]);
			this.multicastIPAddress = Utils.parseInetAddressFromString(stationDetails[0][5]);
			this.stationId = Utils.parseIntFromString(stationDetails[0][7]);
			this.serverKey = stationDetails[0][8];
		}
	}

}
