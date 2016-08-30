package org.rootio.tools.sms;

import org.rootio.tools.diagnostics.DiagnosticAgent;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.SmsManager;

public class ResourcesSMSHandler implements MessageProcessor {

	private String[] messageParts;
	private String from;
	private DiagnosticAgent diagnosticsAgent;
	private Context parent;

	public ResourcesSMSHandler(Context parent, String from, String[] messageParts) {
		this.parent = parent;
		this.from = from;
		this.messageParts = messageParts;
		this.diagnosticsAgent = new DiagnosticAgent(this.parent);
	}

	@Override
	public boolean ProcessMessage() {
		this.diagnosticsAgent.runDiagnostics();
		return false;

	}

	/**
	 * Gets the battery level of the phone
	 * 
	 * @return String with Battery level information of the phone
	 */
	private boolean getBatteryLevel() {
		try {
			String response = String.format("Battery Level: %f", this.diagnosticsAgent.getBatteryLevel());
			this.respondAsyncStatusRequest(response, from);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Gets the storage level of the phone
	 * 
	 * @return String with storage information of the phone
	 */
	private boolean getStorageLevel() {
		try {
			StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
			double freeBytes = statFs.getAvailableBlocks() * statFs.getBlockSize();
			double totalBytes = statFs.getBlockCount() * statFs.getBlockSize();
			String response = String.format("Available Bytes: %d, Free Bytes: %d", freeBytes, totalBytes);
			this.respondAsyncStatusRequest(response, from);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Gets the memory usage of the phone
	 * 
	 * @return String with memory utilization information of the phone
	 */
	private boolean getMemoryUsage() {
		try {

			String response = String.format("Memory Utilization: %f", this.diagnosticsAgent.getMemoryStatus());
			this.respondAsyncStatusRequest(response, from);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Gets the CPU utilization of the phone
	 * 
	 * @return String with CPU utilization information of the phone
	 */
	private boolean getCPUusage() {
		try {

			String response = String.format("CPU Usage: %f", this.diagnosticsAgent.getCPUUtilization());
			this.respondAsyncStatusRequest(response, from);
			return true;

		} catch (Exception ex) {
			return false;
		}

	}

	@Override
	public void respondAsyncStatusRequest(String from, String data) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(from, null, data, null, null);
	}

}
