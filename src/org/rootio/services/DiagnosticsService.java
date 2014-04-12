package org.rootio.services;

import org.rootio.tools.diagnostics.DiagnosticsRunner;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DiagnosticsService extends Service  implements RunningStatusPublished{

	private boolean isRunning;
	private int serviceId = 3;
	private Thread runnerThread;

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
			DiagnosticsRunner diagnosticsRunner = new DiagnosticsRunner(this,
					delay);
			runnerThread = new Thread(diagnosticsRunner);
			runnerThread.start();
			this.isRunning = true;
			this.sendEventBroadCast();
		}
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (runnerThread != null) {
			super.onDestroy();
			runnerThread.interrupt();
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
	private long getDelay() {
		String tableName = "frequencyconfiguration";
		String[] columnsToReturn = new String[] { "frequencyunitid", "quantity" };
		String whereClause = "title = ?";
		String[] whereArgs = new String[] { "diagnostics" };
		DBAgent dbAgent = new DBAgent(this);
		String[][] results = dbAgent.getData(true, tableName, columnsToReturn,
				whereClause, whereArgs, null, null, null, null);
		return results.length > 1 ? this.getMillisToSleep(
				Utils.parseIntFromString(results[0][0]),
				Utils.parseIntFromString(results[0][1])) : 0;
	}

	/**
	 * Get the time in milliseconds for which to sleep given the unit and quantity
	 * @param unitId The ID of the units to be used in measuring time
	 * @param quantity The quantity of units to be used in measuring time
	 * @return The amount of time in milliseconds
	 */
	private long getMillisToSleep(int unitId, int quantity) {
		switch (unitId) {
		case 1: // hours
			return quantity * 3600 * 1000;
		case 2: // minutes
			return quantity * 60 * 1000;
		case 3: // seconds
		default:
			return quantity * 1000;
		}
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

}
