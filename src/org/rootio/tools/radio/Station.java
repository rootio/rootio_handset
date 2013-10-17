package org.rootio.tools.radio;

import org.rootio.tools.persistence.DBAgent;

public class Station {
	private String location;
	private String owner;
	private float frequency;
	private String telephoneNumber;
	private String name;
	private StationStatus stationStatus;

	public Station() {
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

	private void loadStationInfo() {
		String tableName = "station";
		String[] columnsToFetch = new String[] { "location", "owner", "telephonenumber", "name", "frequency" };
		DBAgent dbAgent = new DBAgent();
		String[][] stationDetails = dbAgent.getData(true, tableName, columnsToFetch, null, null, null, null, null, null);
		if (stationDetails.length > 0) {
			this.location = stationDetails[0][0];
			this.owner = stationDetails[0][1];
			this.telephoneNumber = stationDetails[0][2];
			this.name = stationDetails[0][3];
			try {
				this.frequency = Float.parseFloat(stationDetails[0][4]);
			} catch (NumberFormatException ex) {
				this.frequency = 0;
			}
		}
	}

}
