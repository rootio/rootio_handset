package org.rootio.tools.radio;

import java.net.InetAddress;

import org.json.JSONObject;
import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

public class Station {
	private String location;
	private String owner;
	private double frequency;
	private String telephoneNumber;
	private String name;
	private Context parent;
	private InetAddress multicastIPAddress;
	private int multicastPort;
	private int stationId;
	private String serverKey;

	
	public Station(Context parent) {
		this.parent = parent;
        this.loadStationInfo();
	}

	/**
	 * Gets the street location of this station
	 * @return String representation of the location of this station
	 */
	public String getLocation() {
		return this.location;
	}

	/**
	 * Gets the name of the owner of this station. can be a radio network
	 * @return String representation of the name of the owner of this station
	 */
	public String getOwner() {
		return this.owner;
	}

	/**
	 * Gets the telephone number of this station phone
	 * @return String representation of the telephone number of this station phone
	 */
	public String getTelephoneNumber() {
		return this.telephoneNumber;
	}

	/**
	 * Gets the name of this station
	 * @return Name of the station
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the FM frequency at which the station for this phone is licensed to broadcast
	 * @return Long representation of the frequency of the station
	 */
	public double getFrequency() {
		return this.frequency;
	}
	
	/**
	 * Gets the IP address of the multicast group to which the phone subscribes for streaming server announcements
	 * @return InetAddress of the Multicast group to join for announcements
	 */
	public InetAddress getMulticastIPAddress()
	{
		return this.multicastIPAddress;
	}
	
	/**
	 * Gets the port on which the phone listens for multicast service announcements 
	 * @return Integer port number of the multicast port 
	 */
	public int getMulticastPort()
	{
		return this.multicastPort;
	}
	
	/**
	 * Sets the location of the station, that is the street location
	 * @param location The Street address of the station
	 */
	public void setLocation(String location)
	{
		this.location = location;
	}
	
	/**
	 * Sets the owner of the Station to which the phone belongs
	 * @param owner The name of the owner of the station
	 */
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	
	/**
	 * Sets the telephone number for this telephone
	 * @param telephoneNumber The telephone number for this station phone
	 */
	public void setTelephoneNumber(String telephoneNumber)
	{
		this.telephoneNumber = telephoneNumber;
	}
	
	/**
	 * Sets the name of this station
	 * @param name The name of this station
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Sets the frequency at which the station for this phone is supposed to transmit
	 * @param frequency The frequency at which this station is transmitting
	 */
	public void setFrequency(double frequency)
	{
		this.frequency = frequency;
	}
	
	/**
	 * Sets the IP address of the multicast group to join for streaming server announcements
	 * @param multicastIPAddress The IP address of the multicast group
	 */
	public void setMulticastIPAddress(InetAddress multicastIPAddress)
	{
		this.multicastIPAddress = multicastIPAddress;
	}
	
	/**
	 * Sets the port at which to listen for multicast streaming server announcements
	 * @param port  The port at which to listen for multicast announcements      
	 */
	public void setMulticastPort(int port)
	{
		this.multicastPort = port;
	}
	
	/**
	 * Persists station information to the database
	 */
	@Deprecated
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
	
	
	
	/**
	 * Fetches station information from the database
	 */
	/*@Deprecated
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
	}*/
	
	private void loadStationInfo()
	{
		try
		{
		JSONObject stationInformation = Utils.getJSONFromFile(this.parent, this.parent.getAssets().open("station.json"));
		this.location = stationInformation.getString("location");
		//this.owner = stationDetails[0][1];
		this.telephoneNumber = stationInformation.getString("telephone");
		this.name = stationInformation.getString("name");
		this.multicastPort = stationInformation.getInt("multicast_port");
		this.frequency = stationInformation.getDouble("frequency");
		this.multicastIPAddress = Utils.parseInetAddressFromString(stationInformation.getString("multicast_IP"));
		}
		catch(Exception ex)
		{
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "NullPointer(Station.LoadStationInfo)" : ex.getMessage());
		}
	}

}
