package org.rootio.services;

import org.json.JSONObject;
import org.rootio.tools.diagnostics.DiagnosticAgent;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class DiagnosticsService extends Service implements ServiceInformationPublisher {

	private boolean isRunning;
	private int serviceId = 3;
	private Thread runnerThread;
	private boolean wasStoppedOnPurpose = true;

	@Override
	public IBinder onBind(Intent arg0) {
		BindingAgent bindingAgent = new BindingAgent(this);
		return bindingAgent;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!this.isRunning) {
			Utils.doNotification(this, "RootIO", "Diagnostics service started");
			long delay = this.getDelay();
			delay = delay > 0 ? delay : 10000; // 10000 default
			DiagnosticsRunner diagnosticsRunner = new DiagnosticsRunner(this, delay);
			runnerThread = new Thread(diagnosticsRunner);
			runnerThread.start();
			this.isRunning = true;
			this.sendEventBroadCast();
		}
		return Service.START_STICKY;
	}

	@Override
	public void onTaskRemoved(Intent intent) {
		super.onTaskRemoved(intent);
		if (intent != null) {
			wasStoppedOnPurpose = intent.getBooleanExtra("wasStoppedOnPurpose", false);
			if (wasStoppedOnPurpose) {
				this.shutDownService();
			} else {
				this.onDestroy();
			}
		}
	}

	@Override
	public void onDestroy() {
		if (this.wasStoppedOnPurpose == false) {
			Intent intent = new Intent("org.rootio.services.restartServices");
			sendBroadcast(intent);
		} else {
			this.shutDownService();
		}
		super.onDestroy();
	}

	private void shutDownService() {
		if (runnerThread != null) {
			super.onDestroy();
			this.isRunning = false;
			this.sendEventBroadCast();
		}
	}

	@Override
	public boolean isRunning() {
		return this.isRunning;
	}

	/**
	 * Get the number of seconds for which to sleep between synchronizations
	 */
	/*
	 * private long getDelay() { String tableName = "frequencyconfiguration";
	 * String[] columnsToReturn = new String[] { "frequencyunitid", "quantity"
	 * }; String whereClause = "title = ?"; String[] whereArgs = new String[] {
	 * "diagnostics" }; DBAgent dbAgent = new DBAgent(this); String[][] results
	 * = dbAgent.getData(true, tableName, columnsToReturn, whereClause,
	 * whereArgs, null, null, null, null); return results.length > 0 ?
	 * this.getMillisToSleep( Utils.parseIntFromString(results[0][0]),
	 * Utils.parseIntFromString(results[0][1])) : 0; }
	 */

	private long getDelay() {
		try {
			JSONObject frequencyInformation = Utils.getJSONFromFile(this, this.getFilesDir().getAbsolutePath() + "/frequency.json");
			return this.getMillisToSleep(frequencyInformation.getJSONObject("diagnostic").getString("units"), frequencyInformation.getJSONObject("diagnostic").getInt("interval"));
		} catch (Exception ex) {
			return this.getMillisToSleep("minutes", 10);
		}
	}

	/**
	 * Get the time in milliseconds for which to sleep given the unit and
	 * quantity
	 * 
	 * @param unitId
	 *            The ID of the units to be used in measuring time
	 * @param quantity
	 *            The quantity of units to be used in measuring time
	 * @return The amount of time in milliseconds
	 */
	private long getMillisToSleep(String units, int quantity) {
		if (units == "hours")
			return quantity * 3600 * 1000;
		else if (units == "minutes")
			return quantity * 60 * 1000;
		else if (units == "seconds")
			return quantity * 1000;
		else
			return this.getMillisToSleep("minutes", quantity);
	}

	/**
	 * Announces change in service status to listening broadcast receivers
	 */
	private void sendEventBroadCast() {
		Intent intent = new Intent();
		intent.putExtra("serviceId", this.serviceId);
		intent.putExtra("isRunning", this.isRunning);
		intent.setAction("org.rootio.services.diagnostic.EVENT");
		this.sendBroadcast(intent);
	}

	@Override
	public int getServiceId() {
		return this.serviceId;
	}

	class DiagnosticsRunner implements Runnable {
		private DiagnosticAgent diagnosticAgent;
		private Context parentActivity;
		private long delay;

		public DiagnosticsRunner(Context parentActivity, long delay) {
			this.parentActivity = parentActivity;
			this.diagnosticAgent = new DiagnosticAgent(this.parentActivity);
			this.delay = delay;
		}

		@Override
		public void run() {
			while (isRunning) {
				diagnosticAgent.runDiagnostics();
				this.logToDB();
				try {
					Thread.sleep(delay);
				} catch (InterruptedException ex) {

				}
			}
		}

		/**
		 * Saves the diagnostics gathered to the database
		 */
		private void logToDB() {
			String tableName = "diagnostic";
			ContentValues values = new ContentValues();
			values.put("batterylevel", diagnosticAgent.getBatteryLevel());
			values.put("memoryutilization", diagnosticAgent.getMemoryStatus());
			values.put("storageutilization", diagnosticAgent.getStorageStatus());
			values.put("CPUutilization", diagnosticAgent.getCPUUtilization());
			values.put("wificonnected", diagnosticAgent.isConnectedToWifi());
			values.put("gsmconnected", diagnosticAgent.isConnectedToGSM());
			values.put("gsmstrength", diagnosticAgent.getGSMConnectionStrength());
			values.put("latitude", diagnosticAgent.getLatitude());
			values.put("longitude", diagnosticAgent.getLongitude());
			DBAgent dbAgent = new DBAgent(this.parentActivity);
			dbAgent.saveData(tableName, null, values);
		}
	}

}
