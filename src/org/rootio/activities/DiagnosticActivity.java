package org.rootio.activities;

import org.rootio.activities.cloud.CloudActivity;
import org.rootio.activities.diagnostics.DiagnosticsConfigurationActivity;
import org.rootio.activities.services.ServicesActivity;
import org.rootio.activities.stationDetails.StationActivity;
import org.rootio.activities.synchronization.SynchronizationLogDownloadActivity;
import org.rootio.activities.telephoneLog.TelephoneLogActivity;
import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.PoorManStatistics;
import org.rootio.tools.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class DiagnosticActivity extends Activity{

	private double[] cpuData, memoryData, storageData, batteryData, gsmData, wifiConnectivityData;
	private int maxid;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.diagnostics_activity);
	        String[][] data = this.getDiagnosticsData(this.maxid);
	        this.processData(data);
	        this.renderData();
    }
	
	/**
	 * Returns the diagnostic records written since the specified ID
	 * @param maxId The ID since which records should be returned. 0 returns everything
	 * @return An array of String arrays each representing a record of diagnostics
	 */
	private String[][] getDiagnosticsData(long maxId)
	{
		String query = "select batterylevel, gsmstrength, wificonnected, storageutilization, memoryutilization, cpuutilization from diagnostic where diagnostictime > date() and _id > ?";
		String[] filterArgs = new String[]{String.valueOf(maxId)};
		DBAgent agent = new DBAgent(this);
		String[][] results = agent.getData(query, filterArgs);
		return results;
	}
	
	/**
	 * Processes diagnostics data returned from the database into the Arrays for each parameter
	 * @param input An array of string arrays representing diagnostics data fetched from the database
	 */
	private void processData(String[][] input)
	{
		cpuData = new double[input.length]; 
		memoryData = new double[input.length];
		storageData = new double[input.length];
		batteryData = new double[input.length];
		gsmData = new double[input.length];
		wifiConnectivityData = new double[input.length];
		
		for(int i = 0; i < input.length; i++)
		{
			batteryData[i] = Utils.parseDoubleFromString(input[i][0]);
			cpuData[i] = Utils.parseDoubleFromString(input[i][5]);
			storageData[i] = Utils.parseDoubleFromString(input[i][3]);
			memoryData[i] = Utils.parseDoubleFromString(input[i][4]);
			gsmData[i] = Utils.parseDoubleFromString(input[i][1]);
			wifiConnectivityData[i] = Utils.parseDoubleFromString(input[i][2]);
		}
	}
	
	/**
	 * Renders diagnostics data returned from the database onto the screen
	 */
	private void renderData()
	{
		TextView minBatteryTextView = (TextView)this.findViewById(R.id.min_battery_tv);
		minBatteryTextView.setText(String.format("%.2f%s", PoorManStatistics.min(batteryData),"%"));
		TextView averageBatteryTextView = (TextView)this.findViewById(R.id.average_battery_tv);
		averageBatteryTextView.setText(String.format("%.2f%s", PoorManStatistics.mean(batteryData),"%"));
		TextView maxBatteryTextView = (TextView)this.findViewById(R.id.max_battery_tv);
		maxBatteryTextView.setText(String.format("%.2f%s", PoorManStatistics.max(batteryData),"%"));
		
		TextView minGsmTextView = (TextView)this.findViewById(R.id.min_gsm_tv);
		minGsmTextView.setText(String.format("%.2f", PoorManStatistics.min(gsmData)));
		TextView averageGsmTextView = (TextView)this.findViewById(R.id.average_gsm_tv);
		averageGsmTextView.setText(String.format("%.2f", PoorManStatistics.mean(gsmData)));
		TextView maxGsmTextView = (TextView)this.findViewById(R.id.max_gsm_tv);
		maxGsmTextView.setText(String.format("%.2f", PoorManStatistics.max(gsmData)));
		
		TextView minStorageTextView = (TextView)this.findViewById(R.id.min_storage_tv);
		minStorageTextView.setText(String.format("%.2f%s", PoorManStatistics.min(storageData),"%"));
		TextView averageStorageTextView = (TextView)this.findViewById(R.id.average_storage_tv);
		averageStorageTextView.setText(String.format("%.2f%s", PoorManStatistics.mean(storageData),"%"));
		TextView maxStorageTextView = (TextView)this.findViewById(R.id.max_storage_tv);
		maxStorageTextView.setText(String.format("%.2f%s", PoorManStatistics.max(storageData),"%"));
		
		TextView minMemoryTextView = (TextView)this.findViewById(R.id.min_memory_tv);
		minMemoryTextView.setText(String.format("%.2f%s", PoorManStatistics.min(memoryData),"%"));
		TextView averageMemoryTextView = (TextView)this.findViewById(R.id.average_memory_tv);
		averageMemoryTextView.setText(String.format("%.2f%s", PoorManStatistics.mean(memoryData),"%"));
		TextView maxMemoryTextView = (TextView)this.findViewById(R.id.max_memory_tv);
		maxMemoryTextView.setText(String.format("%.2f%s", PoorManStatistics.max(memoryData),"%"));
		
		TextView minCpuTextView = (TextView)this.findViewById(R.id.min_processor_tv);
		minCpuTextView.setText(String.format("%.2f%s", PoorManStatistics.min(cpuData),"%"));
		TextView averageCpuTextView = (TextView)this.findViewById(R.id.average_processor_tv);
		averageCpuTextView.setText(String.format("%.2f%s", PoorManStatistics.mean(cpuData),"%"));
		TextView maxProcessorTextView = (TextView)this.findViewById(R.id.max_processor_tv);
		maxProcessorTextView.setText(String.format("%.2f%s", PoorManStatistics.max(cpuData),"%"));
		TextView wifiAvailabilityTextView = (TextView)this.findViewById(R.id.average_wireless_tv);
		wifiAvailabilityTextView.setText(String.format("%.2f%s", PoorManStatistics.mean(wifiConnectivityData) *100,"%"));
		
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		
		switch(item.getItemId())
		{
		case R.id.station_menu_item:
			intent = new Intent(this, StationActivity.class);
			startActivity(intent);
			return true;
		case R.id.cloud_menu_item :
			intent = new Intent(this, CloudActivity.class);
			startActivity(intent);
			return true;
		case R.id.telephony_menu_item:
			intent = new Intent(this, TelephoneLogActivity.class);
			startActivity(intent);
			return true;
		case R.id.diagnostics_menu_item:
			intent = new Intent(this, DiagnosticsConfigurationActivity.class);
			startActivity(intent);
			return true;
		case R.id.quity_menu_item:
		
			//radioRunner.stop
			this.onStop();
			this.finish();
			return true;
		case R.id.synchronization_menu_item:
			intent = new Intent(this, SynchronizationLogDownloadActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.services_menu_item:
			intent = new Intent(this, ServicesActivity.class);
			this.startActivity(intent);
			return true;
		default:
		return super.onContextItemSelected(item);
		}
	}


}
