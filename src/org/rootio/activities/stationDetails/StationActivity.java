package org.rootio.activities.stationDetails;

import org.rootio.handset.R;
import org.rootio.tools.radio.Station;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class StationActivity extends Activity {

	private Station station;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		this.setContentView(R.layout.station_details);
		this.setTitle("Station Details");
		this.getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public void onStart() {
		super.onStart();
		
		station = new Station(this);
		renderStationInformation();
	}

	/**
	 * Renders station information to the views on the screen
	 */
	private void renderStationInformation() {
		((TextView) findViewById(R.id.station_name)).setText(station.getName());
		((TextView) findViewById(R.id.station_owner)).setText(station.getNetwork());
		((TextView) findViewById(R.id.station_location)).setText(station.getLocation());
		((TextView) findViewById(R.id.station_frequency)).setText(String.valueOf(station.getFrequency()));
		((TextView) findViewById(R.id.station_telephone)).setText(station.getTelephoneNumber());
		((TextView) findViewById(R.id.station_multicast_ip)).setText(station.getMulticastIPAddress() == null ? "" : station.getMulticastIPAddress().getHostAddress());
		((TextView) findViewById(R.id.station_multicast_port)).setText(String.valueOf(station.getMulticastPort()));
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		default: // handles the click of the application icon
			this.finish();
			return false;
		}
	}
}
