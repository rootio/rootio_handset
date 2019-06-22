package org.rootio.tools.diagnostics;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.ActivityCompat;
import android.telecom.TelecomManager;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DiagnosticAgent {

    private boolean isConnectedToWifi;
    private float batteryLevel;
    private boolean isConnectedToMobileNetwork;
    private int mobileSignalStrength;
    private float memoryStatus;
    private float CPUUtilization;
    private double latitude;
    private double longitude;
    private double storageStatus;
    private String telecomOperatorName, mobileNetworkType;
    private final ConnectivityManager connectivityManager;
    private final Context parentActivity;
    private final ActivityManager activityManager;
    private final LocationManager locationManager;

    public DiagnosticAgent(Context parentActivity) {
        this.parentActivity = parentActivity;
        connectivityManager = (ConnectivityManager) parentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        activityManager = (ActivityManager) parentActivity.getSystemService(Context.ACTIVITY_SERVICE);
        locationManager = (LocationManager) parentActivity.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Runs the checks for the various defined diagnostics
     */
    public void runDiagnostics() {
        this.loadSignalStrength();
        this.loadIsConnectedToMobileNetwork();
        this.loadIsConnectedToWifi();
        this.loadBatteryLevel();
        this.loadMemoryStatus();
        this.loadStorageUtilization();
        this.loadCPUutilization();
        this.loadLatitudeLongitude();
        this.loadTelecomOperatorName();
        this.loadMobileNetworkType();
     }

    private void loadMobileNetworkType() {
        if (ActivityCompat.checkSelfPermission(this.parentActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        TelephonyManager mgr = ((TelephonyManager) this.parentActivity.getSystemService(Context.TELEPHONY_SERVICE));

        this.mobileNetworkType = String.format("%s|%s|%s", mgr.getNetworkType(), mgr.getDataNetworkType(), mgr.getVoiceNetworkType());
    }

    /**
     * Loads the name of the telecom operator to which the phone is currently
     * latched
     */
    private void loadTelecomOperatorName() {
        TelephonyManager telephonyManager = (TelephonyManager) this.parentActivity.getSystemService(Context.TELEPHONY_SERVICE);
        this.telecomOperatorName = telephonyManager.getSimOperatorName();
    }

    /**
     * Loads the wiFI connectivity status
     */
    private void loadIsConnectedToWifi() {
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        this.isConnectedToWifi = wifiInfo.isConnected();
    }

    /**
     * Loads the battery level of the phone
     */
    private void loadBatteryLevel() {
        Intent batteryIntent = this.parentActivity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            batteryLevel = 50.0f;
        }

        batteryLevel = ((float) level / (float) scale) * 100.0f;
    }

    /**
     * Loads the mobile data connectivity status
     */
    private void loadIsConnectedToMobileNetwork() {
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        this.isConnectedToMobileNetwork = mobileInfo.isConnected();
    }



    //TODO: actually read documentation for below method
    private void loadSignalStrength() {
        TelephonyManager telephonyManager = (TelephonyManager) this.parentActivity.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this.parentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        try {
            //this takes the highest registered network type. According to documentation, phone can register to more than one cell.
            //THe assumption is that for a network type, the phone registers to strongest signal tower only, and that we are interested in the fastest network type
            for (Object cellinfo : telephonyManager.getAllCellInfo()) {
                if (cellinfo.getClass() == CellInfoLte.class) {
                    if (((CellInfoLte) cellinfo).isRegistered()) {
                        this.mobileSignalStrength = ((CellInfoLte) cellinfo).getCellSignalStrength().getDbm();
                    }
                } else if (cellinfo.getClass() == CellInfoCdma.class) {
                    if (((CellInfoCdma) cellinfo).isRegistered()) {
                        this.mobileSignalStrength = ((CellInfoCdma) cellinfo).getCellSignalStrength().getDbm();
                    }
                } else if (cellinfo.getClass() == CellInfoWcdma.class) {
                    if (((CellInfoWcdma) cellinfo).isRegistered()) {
                        this.mobileSignalStrength = ((CellInfoWcdma) cellinfo).getCellSignalStrength().getDbm();
                    }
                } else if (cellinfo.getClass() == CellInfoGsm.class) {
                    if (((CellInfoGsm) cellinfo).isRegistered()) {
                        this.mobileSignalStrength = ((CellInfoGsm) cellinfo).getCellSignalStrength().getDbm();
                    }
                }
            }
        }
        catch(Exception ex)
        {
            Log.e(parentActivity.getString(R.string.app_name), ex.getMessage() == null ? "NullPointer(DiagnosticAgent.loadSignalStrength)" : ex.getMessage());
        }
    }

    /**
     * Loads the percentage memory utilization of the phone
     */
    private void loadMemoryStatus() {
        try {
            MemoryInfo memInfo = new MemoryInfo();
            activityManager.getMemoryInfo(memInfo);
            this.memoryStatus = (100 * memInfo.availMem) / this.getTotalMemory(); // memInfo.totalMem;
            // doesnt
            // work
            // before
            // jellybean
        } catch (Exception ex) {
            Log.e(parentActivity.getString(R.string.app_name), ex.getMessage() == null ? "NullPointer(DiagnosticAgent.getTotalMemory)" : ex.getMessage());
        }
    }

    private long getTotalMemory() {
        RandomAccessFile memInfo = null;
        String load = null;
        try {
            memInfo = new RandomAccessFile("/proc/meminfo", "r");
            load = memInfo.readLine().split("\\s+")[1];
        } catch (Exception ex) {
            Log.e(parentActivity.getString(R.string.app_name), ex.getMessage() == null ? "NullPointer(DiagnosticAgent.getTotalMemory)" : ex.getMessage());
        } finally {
            try {
                memInfo.close();
            } catch (Exception ex) {
                Log.e(parentActivity.getString(R.string.app_name), ex.getMessage() == null ? "NullPointer(DiagnosticAgent.getTotalMemory)" : ex.getMessage());
            }
        }
        return Utils.parseLongFromString(load) * 1024;
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
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {
            }

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" ");

            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            this.CPUUtilization = (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // this.CPUUtilization = 0;
    }

    /**
     * Loads the GPS coordinates of the phone
     */
    private void loadLatitudeLongitude() {
        if (ActivityCompat.checkSelfPermission(this.parentActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.parentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        }
    }

    /**
     * Loads the percentage Utilization of the phone storage
     */
    private void loadStorageUtilization() {
        try {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            this.storageStatus = 100 - ((statFs.getAvailableBlocks() * 100) / statFs.getBlockCount());
        } catch (Exception ex) {
            this.storageStatus = 0;
        }
    }

    /**
     * Gets the name of the telecom operator to which the phone is latched
     *
     * @return Name of the telecom operator
     */
    public String getTelecomOperatorName() {
        return this.telecomOperatorName;
    }

    /**
     * Gets whether or not the phone is connected to WiFI
     *
     * @return Boolean indicating connectivity. True: Connected, False: Not
     * connected
     */
    public boolean isConnectedToWifi() {
        return this.isConnectedToWifi;
    }

    /**
     * Gets whether or not the phone is latched onto a GSM network
     *
     * @return Boolean indicating GSM connection strength. True: Connected,
     * False: Not connected
     */
    public boolean isConnectedToMobileNetwork() {
        return this.isConnectedToMobileNetwork;
    }

    /**
     * Gets the signal strength of the GSM network
     *
     * @return GSM strength in decibels
     */
    public int getMobileSignalStrength() {
        return this.mobileSignalStrength;
    }

    /**
     * Gets memory utilization
     *
     * @return Percentage memory Utilization
     */
    public float getMemoryStatus() {
        return this.memoryStatus;
    }

    /**
     * Gets the storage status of the phone
     *
     * @return Percentage storage Utilization
     */
    public double getStorageStatus() {
        return this.storageStatus;
    }

    /**
     * Gets the CPU utilization of the phone
     *
     * @return Percentage CPU Utilization of the phone
     */
    public float getCPUUtilization() {
        return this.CPUUtilization;
    }

    /**
     * Gets the battery level of the phone
     *
     * @return Percentage battery utilization of the phone
     */
    public float getBatteryLevel() {
        return this.batteryLevel;
    }

    /**
     * Gets the latitude of the GPS position of the phone
     *
     * @return Latitude of the phone
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * Gets the longitude of the GPS position of the phone
     *
     * @return Longitude of the phone
     */

    public double  getLongitude()
    {
        return this.longitude;
    }

    public String getMobileNetworkType(){
        return this.mobileNetworkType;
    }
}
