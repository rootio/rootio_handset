package org.rootio.tools.radio;

import java.util.HashMap;

public class StationStatus {
	private int batteryLevel;
	private HashMap<String, Float> gpsLocation;
	private int storageStatus;
	private int memoryStatus;
	public StationStatus() {

	}

	public void updateStatus() {

	}

	public int getBatteryLevel() {
		return this.batteryLevel;
	}

	public HashMap<String, Float> getGpsLocation() {
		return this.gpsLocation;
	}

	public int getStorageStatus() {
		return this.storageStatus;
	}

	public int getMemoryStatus() {
		return this.memoryStatus;
	}

	public int getCpuUtilization() {
		return this.batteryLevel;
	}

}
