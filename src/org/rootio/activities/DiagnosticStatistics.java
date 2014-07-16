package org.rootio.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.PoorManStatistics;
import org.rootio.tools.utils.Utils;

import android.content.Context;

public class DiagnosticStatistics {

	private final Context parent;
	private double[] cpuData, memoryData, storageData, batteryData, gsmData, wifiConnectivityData, latitudeData, longitudeData;
	private Date[] dateData;
	private int[] idData;
	private final Date sinceDate;
	private int size;

	public DiagnosticStatistics(Context parent, Date sinceDate) {
		this.parent = parent;
		this.sinceDate = sinceDate;
		this.LoadDiagnosticsData(this.sinceDate);
	}

	/**
	 * Returns the diagnostic records written since the specified ID
	 * 
	 * @param maxId
	 *            The ID since which records should be returned. 0 returns
	 *            everything
	 * @return An array of String arrays each representing a record of
	 *         diagnostics
	 */
	private void LoadDiagnosticsData(Date sinceDate) {
		String query = "select batterylevel, gsmstrength, wificonnected, storageutilization, memoryutilization, cpuutilization, _id, diagnostictime, latitude, longitude  from diagnostic where diagnostictime > ?";
		String[] filterArgs = new String[] { Utils.getDateString(sinceDate, "yyyy-MM-dd HH:mm:ss") == null ? Utils.getDateString(this.getTodayBaseDate(), "yyyy-MM-dd HH:mm:ss") : Utils.getDateString(sinceDate, "yyyy-MM-dd HH:mm:ss") };
		DBAgent agent = new DBAgent(this.parent);
		String[][] results = agent.getData(query, filterArgs);
		idData = new int[results.length];
		dateData = new Date[results.length];
		cpuData = new double[results.length];
		memoryData = new double[results.length];
		storageData = new double[results.length];
		batteryData = new double[results.length];
		gsmData = new double[results.length];
		wifiConnectivityData = new double[results.length];
		latitudeData = new double[results.length];
		longitudeData = new double[results.length];
		size = results.length;
		for (int i = 0; i < results.length; i++) {
			idData[i] = Utils.parseIntFromString(results[i][6]);
			dateData[i] = Utils.getDateFromString(results[i][7], "yyyy-MM-dd HH:mm:ss");
			batteryData[i] = Utils.parseDoubleFromString(results[i][0]);
			cpuData[i] = Utils.parseDoubleFromString(results[i][5]);
			storageData[i] = Utils.parseDoubleFromString(results[i][3]);
			memoryData[i] = Utils.parseDoubleFromString(results[i][4]);
			gsmData[i] = Utils.parseDoubleFromString(results[i][1]);
			wifiConnectivityData[i] = Utils.parseDoubleFromString(results[i][2]);
			latitudeData[i] = Utils.parseDoubleFromString(results[i][8]);
			longitudeData[i] = Utils.parseDoubleFromString(results[i][9]);
		}
	}

	/**
	 * Gets The average CPU Utilization for the day
	 * 
	 * @return Double representing average CPU Utilization for the day
	 */
	public double getAverageCPUUtilization() {
		return PoorManStatistics.mean(this.cpuData);
	}

	/**
	 * Gets the maximum recorded CPU Utilization for the day
	 * 
	 * @return Double representing max CPU Utilization for the day
	 */
	public double getMaxCPUUtilization() {
		return PoorManStatistics.max(cpuData);
	}

	/**
	 * Gets the minimum recorded CPU Utilization for the day
	 * 
	 * @return Double representing min CPU Utilization for the day
	 */
	public double getMinCPUUtilization() {
		return PoorManStatistics.min(cpuData);
	}

	/**
	 * Gets the average recorded Memory Utilization for the day
	 * 
	 * @return Double representing average CPU Utilization for the day
	 */
	public double getAverageMemoryUtilization() {
		return PoorManStatistics.mean(this.memoryData);
	}

	/**
	 * Gets the maximum recorded Memory Utilization for the day
	 * 
	 * @return Double representing max Memory Utilization for the day
	 */
	public double getMaxMemoryUtilization() {
		return PoorManStatistics.max(memoryData);
	}

	/**
	 * Gets the minimum recorded Memory Utilization for the day
	 * 
	 * @return Double representing min Memory Utilization for the day
	 */
	public double getMinMemoryUtilization() {
		return PoorManStatistics.min(memoryData);
	}

	/**
	 * Gets the average recorded Storage Utilization for the day
	 * 
	 * @return Double representing average Storage Utilization for the day
	 */
	public double getAverageStorageUtilization() {
		return PoorManStatistics.mean(this.storageData);
	}

	/**
	 * Gets the maximum recorded Storage Utilization for the day
	 * 
	 * @return Double representing max Storage Utilization for the day
	 */
	public double getMaxStorageUtilization() {
		return PoorManStatistics.max(storageData);
	}

	/**
	 * Gets the average recorded Storage Utilization for the day
	 * 
	 * @return Double representing average Storage Utilization for the day
	 */
	public double getMinStorageUtilization() {
		return PoorManStatistics.min(storageData);
	}

	/**
	 * Gets the average recorded GSM signal strength for the day
	 * 
	 * @return Double representing average GSM signal strength for the day
	 */
	public double getAverageGSMStrength() {
		return PoorManStatistics.mean(this.gsmData);
	}

	/**
	 * Gets the maximum recorded GSM signal strength for the day
	 * 
	 * @return Double representing max GSM signal strength for the day
	 */
	public double getMaxGSMStrength() {
		return PoorManStatistics.max(gsmData);
	}

	/**
	 * Gets the minimum recorded GSM signal strength for the day
	 * 
	 * @return Double representing min GSM signal strength for the day
	 */
	public double getMinGSMStrength() {
		return PoorManStatistics.min(gsmData);
	}

	/**
	 * Gets the average recorded WiFI availability for the day
	 * 
	 * @return Double representing average WiFI availability for the day
	 */
	public double getAverageWiFIAvailability() {
		return PoorManStatistics.mean(this.wifiConnectivityData);
	}

	/**
	 * Gets the average recorded Battery level for the day
	 * 
	 * @return Double representing average Battery level for the day
	 */
	public double getAverageBatteryLevel() {
		return PoorManStatistics.mean(this.batteryData);
	}

	/**
	 * Gets the maximum recorded Battery level for the day
	 * 
	 * @return Double representing max Battery level for the day
	 */
	public double getMaxBatteryLevel() {
		return PoorManStatistics.max(batteryData);
	}

	/**
	 * Gets the minimum recorded Battery level for the day
	 * 
	 * @return Double representing min Battery level for the day
	 */
	public double getMinBatteryLevel() {
		return PoorManStatistics.min(batteryData);
	}

	/**
	 * Gets the minimum recorded latitude of the phone for the day
	 * 
	 * @return Double representing min latitude of the phone for the day
	 */
	public double getMinLatitude() {
		return PoorManStatistics.min(this.latitudeData);
	}

	/**
	 * Gets the minimum recorded longitude of the phone for the day
	 * 
	 * @return Double representing min longitude of the phone for the day
	 */
	public double getMinLongitude() {
		return PoorManStatistics.min(this.longitudeData);
	}

	/**
	 * Gets the maximum recorded latitude of the phone for the day
	 * 
	 * @return Double representing max latitude of the phone for the day
	 */
	public double getMaxLatitude() {
		return PoorManStatistics.max(latitudeData);
	}

	/**
	 * Gets the maximum recorded longitude of the phone for the day
	 * 
	 * @return Double representing max longitude of the phone for the day
	 */
	public double getMaxLongitude() {
		return PoorManStatistics.max(longitudeData);
	}

	/**
	 * Gets the number of records being analyzed
	 * 
	 * @return Integer representing the number of records being analyzed for
	 *         diagnostics
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * Gets the base date since which to consider records for diagnostics
	 * analysis
	 * 
	 * @return Date since which to fetch records for analysis
	 */
	private Date getTodayBaseDate() {
		Date date = Calendar.getInstance().getTime();
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		return date;
	}

	/**
	 * Gets a record containing diagnostic information
	 * 
	 * @param index
	 *            The index of the record to be returned
	 * @return A Hashmap representing a record of diagnostic information
	 */
	public HashMap<String, Object> getRecord(int index) {
		HashMap<String, Object> record = new HashMap<String, Object>();
		record.put("GSM Strength", gsmData[index]);
		record.put("WiFI Connectivity", gsmData[index]);
		record.put("CPU Utilization", gsmData[index]);
		record.put("Battery Level", gsmData[index]);
		record.put("Storage Utilization", gsmData[index]);
		record.put("Memory Utilization", gsmData[index]);
		record.put("Latitude", latitudeData[index]);
		record.put("Longitude", longitudeData[index]);
		record.put("Record Date", dateData[index]);
		record.put("ID", idData[index]);
		return record;
	}
}
