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

	/**
	 * Runs the checks for the various defined diagnostics
	 */
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

	/**
	 * Loads the name of the telecom operator to which the phone is currently latched
	 */
	private void loadTelecomOperatorName() {
		TelephonyManager telephonyManager = (TelephonyManager) this.parentActivity
				.getSystemService(Context.TELEPHONY_SERVICE);
		this.telecomOperatorName = telephonyManager.getSimOperatorName();
	}

	/**
	 * Loads the wiFI connectivity status
	 */
	private void loadIsConnectedToWifi() {
		NetworkInfo wifiInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		this.isConnectedToWifi = wifiInfo.isConnected();
	}

	/**
	 * Loads the battery level of the phone
	 */
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

	/**
	 * Loads the GSM connectivity status
	 */
	private void loadIsConnectedToGSM() {
		NetworkInfo gsmInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		this.isConnectedToGSM = gsmInfo.isConnected();
	}

	/**
	 * This class is a listener for changes in phone GSM signal strength
	 * @author Jude Mukundane
	 *
	 */
	private class SignalStrengthListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(
				android.telephony.SignalStrength signalStrength) {
			GSMConnectionStrength = signalStrength.getGsmSignalStrength();
			super.onSignalStrengthsChanged(signalStrength);
		}
	}

	@SuppressLint("NewApi")
	/**
	 * Loads the percentage memory utilization of the phone
	 */
	private void loadMemoryStatus() {
		MemoryInfo outInfo = new MemoryInfo();
		activityManager.getMemoryInfo(outInfo);
		this.memoryStatus = (100 * outInfo.availMem) / outInfo.totalMem;
	}

	/**
	 * Loads the percentage CPU Utilization of the phone
	 */
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

	/**
	 * Loads the GPS coordinates of the phone
	 */
	private void loadLatitudeLongitude() {
		locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}

	/**
	 * Loads the percentage Utilization of the phone storage
	 */
	private void loadStorageUtilization() {
		StatFs statFs = new StatFs(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		this.storageStatus = 100 - ((statFs.getAvailableBlocks() * 100) / statFs
				.getBlockCount());
	}

	/**
	 * Gets the name of the telecom operator to which the phone is latched
	 * @return Name of the telecom operator
	 */
	public String getTelecomOperatorName() {
		return this.telecomOperatorName;
	}

	/**Gets whether or not the phone is connected to WiFI
	 * @return Boolean indicating connectivity. True: Connected, False: Not connected
	 */
	public boolean isConnectedToWifi() {
		return this.isConnectedToWifi;
	}

	/**
	 * Gets whether or not the phone is latched onto a GSM network
	 * @return Boolean indicating GSM connection strength. True: Connected, False: Not connected
	 */
	public boolean isConnectedToGSM() {
		return this.isConnectedToGSM;
	}

	/**
	 * Gets the signal strength of the GSM network
	 * @return GSM strength in decibels
	 */
	public int getGSMConnectionStrength() {
		return this.GSMConnectionStrength;
	}

	/**
	 * Gets memory utilization
	 * @return Percentage memory Utilization
	 */
	public float getMemoryStatus() {
		return this.memoryStatus;
	}

	/**
	 * Gets the storage status of the phone
	 * @return Percentage storage Utilization
	 */
	public double getStorageStatus() {
		return this.storageStatus;
	}

	/**
	 * Gets the CPU utilization of the phone
	 * @return Percentage CPU Utilization of the phone
	 */
	public float getCPUUtilization() {
		return this.CPUUtilization;
	}

	/**
	 * Gets the battery level of the phone
	 * @return Percentage battery utilization of the phone
	 */
	public float getBatteryLevel() {
		return this.batteryLevel;
	}

	/**
	 * Gets the latitude of the GPS position of the phone
	 * @return Latitude of the phone
	 */
	public double getLatitude() {
		return this.latitude;
	}

	/**
	 * Gets the longitude of the GPS position of the phone
	 * @return Longitude of the phone
	 */
	public double getLongitude() {
		return this.longitude;
	}
}
