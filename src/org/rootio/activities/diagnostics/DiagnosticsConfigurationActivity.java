package org.rootio.activities.diagnostics;

import java.util.HashMap;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class DiagnosticsConfigurationActivity extends Activity implements OnCheckedChangeListener {

	private Switch batteryCtv, gsmCtv, wirelessCtv, gpsCtv, cpuCtv, memoryCtv, storageCtv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.diagnostics_configuration);
		this.setTitle("Configure Diagnostics");
		setSwitches();
	}

	/**
	 * Initializes references to the switches in the layout
	 */
	private void setSwitches() {
		batteryCtv = (Switch) this.findViewById(R.id.battery_level_ctv);
		gsmCtv = (Switch) this.findViewById(R.id.gsm_signal_ctv);
		wirelessCtv = (Switch) this.findViewById(R.id.wireless_signal_ctv);
		gpsCtv = (Switch) this.findViewById(R.id.gps_location_ctv);
		cpuCtv = (Switch) this.findViewById(R.id.cpu_usage_ctv);
		memoryCtv = (Switch) this.findViewById(R.id.memory_usage_ctv);
		storageCtv = (Switch) this.findViewById(R.id.storage_ctv);
	}

	/**
	 * Sets listeners for the various switches to persist the change of state to the database
	 */
	private void setOnchangeListeners() {
		batteryCtv.setOnCheckedChangeListener(this);
		gsmCtv.setOnCheckedChangeListener(this);
		wirelessCtv.setOnCheckedChangeListener(this);
		gpsCtv.setOnCheckedChangeListener(this);
		cpuCtv.setOnCheckedChangeListener(this);
		memoryCtv.setOnCheckedChangeListener(this);
		storageCtv.setOnCheckedChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = this.getMenuInflater();
		menuInflater.inflate(R.menu.diagnostics_configuration_frequency, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.diagnostics_configuration_frequency_menu_item:
			Intent intent = new Intent(this, DiagnosticsConfigurationFrequencyActivity.class);
			this.startActivity(intent);
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		String[][] savedSettings = this.getSavedSettings();
		this.renderSavedSettings(savedSettings);
		this.setOnchangeListeners();
	}

	/**
	 * Sets the switches to reflect the current status of the diagnostic parameters in the database
	 * @param savedSettings
	 */
	private void renderSavedSettings(String[][] savedSettings)
	{
		 HashMap<String, Switch> switches = new HashMap<String, Switch>();
		 switches.put("1", gsmCtv);
		 switches.put("2", wirelessCtv);
		 switches.put("3", gpsCtv);
		 switches.put("4", cpuCtv);
		 switches.put("5", memoryCtv);
		 switches.put("6", storageCtv);
		 switches.put("7", batteryCtv);
		 for(int i = 0; i < savedSettings.length; i++)
		 {
			 try
			 {
				 switches.get(savedSettings[i][0]).setChecked(savedSettings[i][2].equals("1")); 
			 }
			 catch(Exception ex)
			 {
				 Log.e(this.getString(R.string.app_name), ex == null? "null pointer" :ex.getMessage());
			 }
		 }
	}
	
	/**
	 * Fetches the diagnostics parameters and their values as persisted in the database
	 * @return An array of String arrays representing the diagnostics configuration information
	 */
	private String[][] getSavedSettings()
	{
		String tableName = "diagnosticsconfiguration";
		String[] columns = new String[] {"_id", "title", "enabled"};
		DBAgent agent = new DBAgent(this);
		String[][] result = agent.getData(true, tableName, columns, null, null, null, null, null, null);
		return result;
	}
	
	/**
	 * Updates the value of a parameter in the database
	 * @param parameter The parameter whose value is being updated
	 * @param value The value to which the parameter is being set
	 * @return number of rows affected by the update
	 */
	private int runParameterUpdate(String parameter, boolean value) {
		String tableName = "diagnosticsconfiguration";
		String whereClause = "title = ?";
		String[] whereArgs = new String[] { parameter };
		ContentValues data = new ContentValues();
		data.put("enabled", value);
		DBAgent agent = new DBAgent(this.getApplicationContext());
		return agent.updateRecords(tableName, data, whereClause, whereArgs);
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		switch(arg0.getId())
		{
		case R.id.gsm_signal_ctv:
			this.runParameterUpdate("gsm", gsmCtv.isChecked());
			break;
		case R.id.wireless_signal_ctv:
			this.runParameterUpdate("wireless", wirelessCtv.isChecked());
			break;
		case R.id.battery_level_ctv:
			this.runParameterUpdate("battery", batteryCtv.isChecked());
			break;
		case R.id.memory_usage_ctv:
			this.runParameterUpdate("memory", memoryCtv.isChecked());
			break;
		case R.id.cpu_usage_ctv:
			this.runParameterUpdate("cpu", cpuCtv.isChecked());
			break;
		case R.id.storage_ctv:
			this.runParameterUpdate("storage", storageCtv.isChecked());
			break;
		case R.id.gps_location_ctv:
			this.runParameterUpdate("gps", gpsCtv.isChecked());
			break;
		}
		
	}
}
