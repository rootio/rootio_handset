package org.rootio.activities;

import java.util.ArrayList;

import org.rootio.activities.cloud.CloudActivity;
import org.rootio.activities.diagnostics.DiagnosticsConfigurationActivity;
import org.rootio.activities.services.ServiceExitInformable;
import org.rootio.activities.services.ServicesActivity;
import org.rootio.activities.stationDetails.StationActivity;
import org.rootio.activities.stationDetails.StationActivityAdapter;
import org.rootio.activities.synchronization.SynchronizationLogDownloadActivity;
import org.rootio.activities.telephoneLog.TelephoneLogActivity;
import org.rootio.radioClient.R;
import org.rootio.services.Notifiable;
import org.rootio.services.ProgramService;
import org.rootio.services.ServiceConnectionAgent;
import org.rootio.tools.radio.ProgramSlot;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class RadioActivity extends Activity implements Notifiable, ServiceExitInformable {

	private ServiceConnectionAgent programServiceConnection;
	private RadioServiceExitBroadcastHandler exitBroadCastHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.station_activity);
		this.setTitle("Station Details");
	}

	@Override
	public void onResume() {
		super.onResume();
		this.bindToService();
		this.setupExitIntentListener();
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
			case R.id.diagnostics_menu_item:
				intent = new Intent(this, DiagnosticsConfigurationActivity.class);
				startActivity(intent);
				return true;
			case R.id.quity_menu_item:
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

	/**
	 * Binds to the program service to get status of programs that are displayed
	 * on the home radio screen
	 */
	private void bindToService() {
		programServiceConnection = new ServiceConnectionAgent(this, 4);
		Intent intent = new Intent(this, ProgramService.class);
		if (this.getApplicationContext().bindService(intent, programServiceConnection, BIND_AUTO_CREATE)) {
			// just wait for the async call
		}
	}

	/**
	 * Set up a listener to detect exit of the program service. This is so that
	 * the activity can unbind from the service in order for the service to shut
	 * down
	 */
	private void setupExitIntentListener() {
		this.exitBroadCastHandler = new RadioServiceExitBroadcastHandler(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("org.rootio.activities.services.RADIO_SERVICE_STOP");
		this.registerReceiver(exitBroadCastHandler, intentFilter);
	}

	@Override
	public void disconnectFromRadioService() {
		try {
			this.getApplicationContext().unbindService(this.programServiceConnection);
		} catch (Exception ex) {
			Log.e(this.getString(R.string.app_name), ex.getMessage());
		}
	}

	@Override
	public void notifyServiceConnection(int serviceId) {
		ProgramService programService = (ProgramService) programServiceConnection.getService();
		final ArrayList<ProgramSlot> programSlots = programService.getProgramSlots();
		StationActivityAdapter stationActivityAdapter = new StationActivityAdapter(programSlots);
		ListView stationActivityList = (ListView) this.findViewById(R.id.station_activity_lv);
		stationActivityList.setAdapter(stationActivityAdapter);
		stationActivityList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
				Intent intent = new Intent(RadioActivity.this, RadioProgramActionsActivity.class);
				intent.putExtra("index", index);
				RadioActivity.this.startActivity(intent);
			}
		});

	}

	@Override
	public void onPause() {
		super.onPause();
		this.disconnectFromRadioService();
	}

	@Override
	public void notifyServiceDisconnection(int serviceId) {
		this.bindToService();

	}

}