package org.rootio.activities;

import org.rootio.activities.cloud.CloudActivity;
import org.rootio.activities.diagnostics.FrequencyActivity;
import org.rootio.activities.services.ServicesActivity;
import org.rootio.activities.stationDetails.StationActivity;
import org.rootio.activities.telephoneLog.TelephoneLogActivity;
import org.rootio.handset.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class DiagnosticActivity extends Activity {

	private DiagnosticStatistics diagnosticStatistics;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.diagnostics_activity);

		this.diagnosticStatistics = new DiagnosticStatistics(this);
		this.renderData();
	}

	/**
	 * Renders diagnostics data returned from the database onto the screen
	 */
	private void renderData() {
		TextView minBatteryTextView = (TextView) this.findViewById(R.id.min_battery_tv);
		minBatteryTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getMinBatteryLevel(), "%"));
		TextView averageBatteryTextView = (TextView) this.findViewById(R.id.average_battery_tv);
		averageBatteryTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getAverageBatteryLevel(), "%"));
		TextView maxBatteryTextView = (TextView) this.findViewById(R.id.max_battery_tv);
		maxBatteryTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getMaxBatteryLevel(), "%"));

		TextView minGsmTextView = (TextView) this.findViewById(R.id.min_gsm_tv);
		minGsmTextView.setText(String.format("%.1f", this.diagnosticStatistics.getMinGSMStrength()));
		TextView averageGsmTextView = (TextView) this.findViewById(R.id.average_gsm_tv);
		averageGsmTextView.setText(String.format("%.1f", this.diagnosticStatistics.getAverageGSMStrength()));
		TextView maxGsmTextView = (TextView) this.findViewById(R.id.max_gsm_tv);
		maxGsmTextView.setText(String.format("%.1f", this.diagnosticStatistics.getMaxGSMStrength()));

		TextView minStorageTextView = (TextView) this.findViewById(R.id.min_storage_tv);
		minStorageTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getMinStorageUtilization(), "%"));
		TextView averageStorageTextView = (TextView) this.findViewById(R.id.average_storage_tv);
		averageStorageTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getAverageStorageUtilization(), "%"));
		TextView maxStorageTextView = (TextView) this.findViewById(R.id.max_storage_tv);
		maxStorageTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getMaxStorageUtilization(), "%"));

		TextView minMemoryTextView = (TextView) this.findViewById(R.id.min_memory_tv);
		minMemoryTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getMinMemoryUtilization(), "%"));
		TextView averageMemoryTextView = (TextView) this.findViewById(R.id.average_memory_tv);
		averageMemoryTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getAverageMemoryUtilization(), "%"));
		TextView maxMemoryTextView = (TextView) this.findViewById(R.id.max_memory_tv);
		maxMemoryTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getMaxMemoryUtilization(), "%"));

		TextView minCpuTextView = (TextView) this.findViewById(R.id.min_processor_tv);
		minCpuTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getMinCPUUtilization(), "%"));
		TextView averageCpuTextView = (TextView) this.findViewById(R.id.average_processor_tv);
		averageCpuTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getAverageCPUUtilization(), "%"));
		TextView maxProcessorTextView = (TextView) this.findViewById(R.id.max_processor_tv);
		maxProcessorTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getMaxCPUUtilization(), "%"));
		TextView wifiAvailabilityTextView = (TextView) this.findViewById(R.id.average_wireless_tv);
		wifiAvailabilityTextView.setText(String.format("%.1f%s", this.diagnosticStatistics.getAverageWiFIAvailability() * 100, "%"));

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
		case R.id.station_menu_item:
			intent = new Intent(this, StationActivity.class);
			startActivity(intent);
			return true;
		case R.id.cloud_menu_item:
			intent = new Intent(this, CloudActivity.class);
			startActivity(intent);
			return true;
		case R.id.telephony_menu_item:
			intent = new Intent(this, TelephoneLogActivity.class);
			intent.putExtra("isHomeScreen", false);
			startActivity(intent);
			return true;
		case R.id.frequency_menu_item:
			intent = new Intent(this, FrequencyActivity.class);
			startActivity(intent);
			return true;
		case R.id.quity_menu_item:

			// radioRunner.stop
			this.onStop();
			this.finish();
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
