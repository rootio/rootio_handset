package org.rootio.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.PoorManStatistics;
import org.rootio.tools.utils.Utils;

import android.content.Context;

public class DiagnosticStatistics {
	
	private Context parent;
	private double[] cpuData, memoryData, storageData, batteryData, gsmData, wifiConnectivityData, latitudeData, longitudeData;
	private Date[] dateData;
	private int[] idData;
	private Date sinceDate;
	private int size;
	
	public DiagnosticStatistics(Context parent, Date sinceDate)
	{
		this.parent = parent;
		this.sinceDate = sinceDate;
		this.LoadDiagnosticsData(this.sinceDate);
	}
	
	/**
	 * Returns the diagnostic records written since the specified ID
	 * @param maxId The ID since which records should be returned. 0 returns everything
	 * @return An array of String arrays each representing a record of diagnostics
	 */
	private void LoadDiagnosticsData(Date sinceDate)
	{
		String query = "select batterylevel, gsmstrength, wificonnected, storageutilization, memoryutilization, cpuutilization, _id, diagnostictime, latitude, longitude  from diagnostic where diagnostictime > ?";
		String[] filterArgs = new String[]{Utils.getDateString(sinceDate, "yyyy-MM-dd HH:mm:ss")== null?Utils.getDateString(this.getTodayBaseDate(),"yyyy-MM-dd HH:mm:ss"):Utils.getDateString(sinceDate, "yyyy-MM-dd HH:mm:ss")};
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
		for(int i = 0; i < results.length; i++)
		{
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
	
	public double getAverageCPUUtilization()
	{
		return PoorManStatistics.mean(this.cpuData);
	}
	
	public double getMaxCPUUtilization()
	{
		return PoorManStatistics.max(cpuData);
	}
	
	public double getMinCPUUtilization()
	{
		return PoorManStatistics.min(cpuData);
	}
	
	public double getAverageMemoryUtilization()
	{
		return PoorManStatistics.mean(this.memoryData);
	}
	
	public double getMaxMemoryUtilization()
	{
		return PoorManStatistics.max(memoryData);
	}
	
	public double getMinMemoryUtilization()
	{
		return PoorManStatistics.min(memoryData);
	}
	
	public double getAverageStorageUtilization()
	{
		return PoorManStatistics.mean(this.storageData);
	}
	
	public double getMaxStorageUtilization()
	{
		return PoorManStatistics.max(storageData);
	}
	
	public double getMinStorageUtilization()
	{
		return PoorManStatistics.min(storageData);
	}
	
	public double getAverageGSMStrength()
	{
		return PoorManStatistics.mean(this.gsmData);
	}
	
	public double getMaxGSMStrength()
	{
		return PoorManStatistics.max(gsmData);
	}
	
	public double getMinGSMStrength()
	{
		return PoorManStatistics.min(gsmData);
	}
	
	public double getAverageWiFIAvailability()
	{
		return PoorManStatistics.mean(this.wifiConnectivityData);
	}
	
	public double getAverageBatteryLevel()
	{
		return PoorManStatistics.mean(this.batteryData);
	}
	
	public double getMaxBatteryLevel()
	{
		return PoorManStatistics.max(batteryData);
	}
	
	public double getMinBatteryLevel()
	{
		return PoorManStatistics.min(batteryData);
	}
	
	public double getMinLatitude()
	{
		return PoorManStatistics.min(this.latitudeData);
	}
	
	public double getMinLongitude()
	{
		return PoorManStatistics.min(this.longitudeData);
	}
	
	public double getMaxLatitude()
	{
		return PoorManStatistics.max(latitudeData);
	}
	
	public double getMaxLongitude()
	{
		return PoorManStatistics.max(longitudeData);
	}
	
	public int getSize()
	{
		return this.size;
	}
	
	private Date getTodayBaseDate()
	{
		Date date = Calendar.getInstance().getTime();
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		return date;
	}
	
	public HashMap<String, Object> getRecord(int index)
	{
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
