package org.rootio.tools.diagnostics;

import org.rootio.tools.persistence.DBAgent;

import android.content.ContentValues;
import android.content.Context;

public class DiagnosticsRunner implements Runnable {
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
		while (true) {
			if(Thread.interrupted())
				return;
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
	private void logToDB()
	{
		String tableName = "diagnostic";
		ContentValues values = new ContentValues();
		values.put("batterylevel", diagnosticAgent.getBatteryLevel());
		values.put("memoryutilization", diagnosticAgent.getMemoryStatus());
		values.put("storageutilization", diagnosticAgent.getStorageStatus());
		values.put("CPUutilization",diagnosticAgent.getCPUUtilization());
		values.put("wificonnected", diagnosticAgent.isConnectedToWifi());
		values.put("gsmconnected", diagnosticAgent.isConnectedToGSM());
		values.put("gsmstrength", diagnosticAgent.getGSMConnectionStrength());
		values.put("latitude",diagnosticAgent.getLatitude());
		values.put("longitude", diagnosticAgent.getLongitude());
		DBAgent dbAgent = new DBAgent(this.parentActivity);
		dbAgent.saveData(tableName, null, values);
	}

	
}
