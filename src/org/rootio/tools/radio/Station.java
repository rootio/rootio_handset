package org.rootio.tools.radio;

import java.net.InetAddress;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.Context;

public class Station {
	private String location;
	private String owner;
	private float frequency;
	private String telephoneNumber;
	private String name;
	private StationStatus stationStatus;
	private Context context;
	private InetAddress multicastIPAddress;
	private int multicastPort;

	public Station(Context context) {
		this.context = context;
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

	private void loadStationInfo() {
		String tableName = "station";
		String[] columnsToFetch = new String[] { "location", "owner", "telephonenumber", "name", "frequency", "multicastipaddress", "multicastport" };
		DBAgent dbAgent = new DBAgent(this.context);
		String[][] stationDetails = dbAgent.getData(true, tableName, columnsToFetch, null, null, null, null, null, null);
		if (stationDetails.length > 0) {
			this.location = stationDetails[0][0];
			this.owner = stationDetails[0][1];
			this.telephoneNumber = stationDetails[0][2];
			this.name = stationDetails[0][3];
			this.multicastPort = Utils.parseIntFromString(stationDetails[0][6]);
			this.frequency = Utils.parseFloatFromString(stationDetails[0][4]);
			this.multicastIPAddress = Utils.parseInetAddressFromString(stationDetails[0][5]);
		}
	}

}
