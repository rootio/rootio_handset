package org.rootio.tools.diagnostics;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class DiagnosticAgent {

	private boolean isConnectedToWifi;
	private float batteryLevel;
	private boolean isConnectedToGSM;
	private int GSMConnectionStrength;
	private float memoryStatus;
	private float CPUUtilization;
	private double latitude;
	private double longitude;
	private double storageStatus;
	private String telecomOperatorName;
	private ConnectivityManager connectivityManager;
	private Context parentActivity;
	private SignalStrengthListener signalStrengthListener;
	private ActivityManager activityManager;
	private LocationManager locationManager;

	public DiagnosticAgent(Context parentActivity) {
		this.parentActivity = parentActivity;
		connectivityManager = (ConnectivityManager) parentActivity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		signalStrengthListener = new SignalStrengthListener();
		((TelephonyManager) this.parentActivity
				.getSystemService(Context.TELEPHONY_SERVICE)).listen(
				signalStrengthListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		activityManager = (ActivityManager) parentActivity
				.getSystemService(Context.ACTIVITY_SERVICE);
		locationManager = (LocationManager) parentActivity
				.getSystemService(Context.LOCATION_SERVICE);
	}

	public void runDiagnostics() {
		this.loadIsConnectedToGSM();
		this.loadIsConnectedToWifi();
		this.loadBatteryLevel();
		this.loadMemoryStatus();
		this.loadStorageUtilization();
		this.loadCPUutilization();
		this.loadLatitudeLongitude();
		this.loadTelecomOperatorName();
	}

	private void loadTelecomOperatorName() {
		TelephonyManager telephonyManager = (TelephonyManager) this.parentActivity
				.getSystemService(Context.TELEPHONY_SERVICE);
		this.telecomOperatorName = telephonyManager.getSimOperatorName();
	}

	private void loadIsConnectedToWifi() {
		NetworkInfo wifiInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		this.isConnectedToWifi = wifiInfo.isConnected();
	}

	private void loadBatteryLevel() {
		Intent batteryIntent = this.parentActivity.registerReceiver(null,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		// Error checking that probably isn't needed but I added just in case.
		if (level == -1 || scale == -1) {
			batteryLevel = 50.0f;
		}

		batteryLevel = ((float) level / (float) scale) * 100.0f;
	}

	private void loadIsConnectedToGSM() {
		NetworkInfo gsmInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		this.isConnectedToGSM = gsmInfo.isConnected();
	}

	private class SignalStrengthListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(
				android.telephony.SignalStrength signalStrength) {
			GSMConnectionStrength = signalStrength.getGsmSignalStrength();
			super.onSignalStrengthsChanged(signalStrength);
		}
	}

	@SuppressLint("NewApi")
	private void loadMemoryStatus() {
		MemoryInfo outInfo = new MemoryInfo();
		activityManager.getMemoryInfo(outInfo);
		this.memoryStatus = (100 * outInfo.availMem) / outInfo.totalMem;
	}

	private void loadCPUutilization() {
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
			String load = reader.readLine();

			String[] toks = load.split(" ");

			long idle1 = Long.parseLong(toks[5]);
			long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
					+ Long.parseLong(toks[4]) + Long.parseLong(toks[6])
					+ Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

			try {
				Thread.sleep(360);
			} catch (Exception e) {
			}

			reader.seek(0);
			load = reader.readLine();
			reader.close();

			toks = load.split(" ");

			long idle2 = Long.parseLong(toks[5]);
			long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
					+ Long.parseLong(toks[4]) + Long.parseLong(toks[6])
					+ Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

			this.CPUUtilization = (float) (cpu2 - cpu1)
					/ ((cpu2 + idle2) - (cpu1 + idle1));

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// this.CPUUtilization = 0;
	}

	private void loadLatitudeLongitude() {
		locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}

	private void loadStorageUtilization() {
		StatFs statFs = new StatFs(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		this.storageStatus = 100 - ((statFs.getAvailableBlocks() * 100) / statFs
				.getBlockCount());
	}

	public String getTelecomOperatorName() {
		return this.telecomOperatorName;
	}

	public boolean isConnectedToWifi() {
		return this.isConnectedToWifi;
	}

	public boolean isConnectedToGSM() {
		return this.isConnectedToGSM;
	}

	public int getGSMConnectionStrength() {
		return this.GSMConnectionStrength;
	}

	public float getMemoryStatus() {
		return this.memoryStatus;
	}

	public double getStorageStatus() {
		return this.storageStatus;
	}

	public float getCPUUtilization() {
		return this.CPUUtilization;
	}

	public float getBatteryLevel() {
		return this.batteryLevel;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}
}
