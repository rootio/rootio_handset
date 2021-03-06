package org.rootio.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import org.rootio.activities.cloud.CloudActivity;
import org.rootio.activities.diagnostics.FrequencyActivity;
import org.rootio.activities.services.ServiceExitInformable;
import org.rootio.activities.services.ServicesActivity;
import org.rootio.activities.stationDetails.StationActivity;
import org.rootio.activities.stationDetails.StationActivityAdapter;
import org.rootio.activities.telephoneLog.TelephoneLogActivity;
import org.rootio.handset.R;
import org.rootio.services.Notifiable;
import org.rootio.services.RadioService;
import org.rootio.services.ServiceConnectionAgent;
import org.rootio.services.ServiceStopNotifiable;

public class RadioActivity extends Activity implements Notifiable, ServiceExitInformable, ServiceStopNotifiable {

    private ServiceConnectionAgent programServiceConnection;
    private RadioServiceExitBroadcastHandler exitBroadCastHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.station_activity);
        this.setTitle("Station Details");
        /*
		 * this.setUpServiceStopHandling(); Intent intent = new Intent(this,
		 * CrashMonitor.class);
		 * intent.setAction("org.rootio.recovery.APP_CRASH"); crashIntent =
		 * PendingIntent.getActivity(getBaseContext(), 0, intent, 0);
		 * Thread.setDefaultUncaughtExceptionHandler(new
		 * Thread.UncaughtExceptionHandler(){
		 * 
		 * @Override public void uncaughtException(Thread thread, Throwable ex)
		 * { unexpectedStophandler(thread, ex); }
		 * 
		 * });
		 */
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
            case R.id.frequency_menu_item:
                intent = new Intent(this, FrequencyActivity.class);
                startActivity(intent);
                return true;
            case R.id.quit_menu_item:
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

    /**
     * Binds to the program service to get status of programs that are displayed
     * on the home radio screen
     */
    private void bindToService() {
        programServiceConnection = new ServiceConnectionAgent(this, 4);
        Intent intent = new Intent(this, RadioService.class);
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

//	private void setUpServiceStopHandling() {
//		this.serviceStopReceiver = new ServiceStopReceiver(this);
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction("org.rootio.services.STOP_EVENT");
//		this.registerReceiver(this.serviceStopReceiver, intentFilter);
//	}

    @Override
    public void disconnectFromRadioService() {
        try {
            this.getApplicationContext().unbindService(this.programServiceConnection);
        } catch (Exception ex) {
            Log.e(this.getString(R.string.app_name), String.format("[RadioActivity.disconnectFromRadioService] %s", ex.getMessage()));
        }
    }

    @Override
    public void notifyServiceConnection(int serviceId) {
        RadioService programService = (RadioService) programServiceConnection.getService();
        StationActivityAdapter stationActivityAdapter = new StationActivityAdapter(programService.getPrograms());
        ListView stationActivityList = this.findViewById(R.id.station_activity_lv);
        stationActivityList.setAdapter(stationActivityAdapter);

    }

    @Override
    public void onPause() {
        super.onPause();
        this.disconnectFromRadioService();
    }

    @Override
    public void notifyServiceDisconnection(int serviceId) {
        // this.bindToService();
    }

    @Override
    public void notifyServiceStop(int serviceId) {
        this.disconnectFromRadioService();

    }
	/*
	 * public void unexpectedStophandler(Thread paramThread, Throwable
	 * throwable) { AlarmManager am = (AlarmManager)
	 * getSystemService(Context.ALARM_SERVICE); am.set(AlarmManager.RTC,
	 * System.currentTimeMillis() + 5000, crashIntent); System.exit(2); }
	 */
}