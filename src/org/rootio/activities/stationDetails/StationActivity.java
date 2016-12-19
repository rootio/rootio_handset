package org.rootio.activities.stationDetails;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.radioClient.R;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.radio.Station;
import org.rootio.tools.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
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
		((TextView) findViewById(R.id.station_owner)).setText(station.getOwner());
		((TextView) findViewById(R.id.station_location)).setText(station.getLocation());
		((TextView) findViewById(R.id.station_frequency)).setText(String.valueOf(station.getFrequency()));
		((TextView) findViewById(R.id.station_telephone)).setText(station.getTelephoneNumber());
		((TextView) findViewById(R.id.station_multicast_ip)).setText(station.getMulticastIPAddress() == null ? "" : station.getMulticastIPAddress().getHostAddress());
		((TextView) findViewById(R.id.station_multicast_port)).setText(String.valueOf(station.getMulticastPort()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = this.getMenuInflater();
		menuInflater.inflate(R.menu.multicast_configuration, menu);
		return true;
	}

	
	/**
	 * Gets a JSON object from the supplied String
	 * 
	 * @param input
	 *            The String from which JSON content is to be obtained
	 * @return JSON object generated from the supplied JSON or null if JSON
	 *         could not be obtained from the input
	 */
	private JSONObject getJSON(String input) {
		try {
			return new JSONObject(input);
		} catch (JSONException ex) {
			return null;
		} catch (NullPointerException ex) {
			return null;
		}
	}
}
