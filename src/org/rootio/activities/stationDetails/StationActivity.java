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
	private Cloud cloud;
	private ProgressDialog progressDialog;
	private Handler handler;

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
		this.handler = new Handler();
		
		station = new Station(this);
		this.cloud = new Cloud(this);
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

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.multicast_configuration_menu_item:
			Intent intent = new Intent(this,
					MulticastConfigurationActivity.class);
			this.startActivity(intent);
			break;
		case R.id.refresh_station_details_menu_item:
			this.fetchStationInformation(this.cloud.getServerAddress(), this.cloud.getHTTPPort(), this.cloud.getStationId(), cloud.getServerKey());
			break;
		default: //handles the click of the application icon
			this.finish();
			break;
		}
		return true;
	}

	/**
	 * Fetches information about the station from the cloud using the supplied cloud parameters
	 * @param serverAddress The address of the cloud server
	 * @param HTTPPort The HTTP port of the cloud server
	 * @param stationId The ID by which this station id identified in the cloud
	 * @param serverKey The key by which this station is authenticated on the cloud server
	 */
	private void fetchStationInformation(final String serverAddress, final int HTTPPort, final int stationId, final String serverKey ) {
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setTitle("Fetching station details");
		this.progressDialog.show();
		Thread thread = new Thread(new Runnable(){

			@Override
			public void run() {
				String URL = String.format("http://%s:%s/api/station/%s?api_key=%s", serverAddress, HTTPPort,	stationId, serverKey);
				String response = Utils.doHTTP(URL);
				JSONObject stationObject = StationActivity.this.getJSON(response);
				if (stationObject != null) {
					StationActivity.this.setStationDetails(stationObject);
				}	
				StationActivity.this.handler.post(new Runnable(){

					@Override
					public void run() {
						StationActivity.this.progressDialog.dismiss();
						StationActivity.this.renderStationInformation();
					}});
			}		
		});
		thread.start();
		
	}
	
	
/**
 * Gets a JSON object from the supplied String
 * @param input The String from which JSON content is to be obtained
 * @return JSON object generated from the supplied JSON or null if JSON could not be obtained from the input
 */
	private JSONObject getJSON(String input) {
		try {
			return new JSONObject(input);
		} catch (JSONException ex) {
			return null;
		}
		catch (NullPointerException ex)
		{
			return null;
		}
	}

	/**
	 * Get station details from the supplied JSON object
	 * @param object The JSON object containing station information
	 */
	private void setStationDetails(JSONObject object) {
		try {
			this.station.setFrequency(object.has("frequency") ? Utils.parseFloatFromString(object.getString("frequency")):this.station.getFrequency());
			this.station.setLocation(object.has("location") ? object.getJSONObject("location").getString("addressline1"):this.station.getLocation());
			this.station.setName(object.has("name") ? object.getString("name") : this.station.getName());
			this.station.setTelephoneNumber(object.has("transmitter_phone") ? object.getString("transmitter_phone") : this.station.getTelephoneNumber());
			this.station.setOwner(object.has("owner_id") ? object.getString("owner_id") : this.station.getOwner());
			this.station.persist();
			Utils.toastOnScreen("Station information successfully updated");
		} catch (JSONException ex) {
            Utils.toastOnScreen("Error encountered trying to refressh station information");
		}

	}
}
