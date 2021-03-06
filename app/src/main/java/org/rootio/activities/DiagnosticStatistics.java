package org.rootio.activities;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.PoorManStatistics;
import org.rootio.tools.utils.Utils;

import java.util.Date;

public class DiagnosticStatistics {

    private final Context parent;
    private double[] cpuData, memoryData, storageData, batteryData, mobileNetworkStrength, wifiConnectivityData, latitudeData, longitudeData;
    private Date[] dateData;
    private int[] idData;
    private String[] mobileNetworkName, mobileNetworkType;
    private boolean [] mobileNetworkConnected;
    private int size;

    public DiagnosticStatistics(Context parent) {
        this.parent = parent;
        this.LoadDiagnosticsData();
    }

    /**
     * Returns the diagnostic records written since the specified ID
      @return An array of String arrays each representing a record of
     * diagnostics
     */
    private void LoadDiagnosticsData() {
        String query = "select batterylevel, firstmobilenetworkname, firstmobilenetworktype, firstmobilenetworkconnected, firstmobilenetworkstrength, wificonnected, storageutilization, memoryutilization, cpuutilization, _id, diagnostictime, latitude, longitude  from diagnostic ";
        String[] filterArgs = new String[]{};
        //DBAgent agent = new DBAgent(this.parent);
        String[][] results = DBAgent.getData(query, filterArgs);
        idData = new int[results.length];
        dateData = new Date[results.length];
        cpuData = new double[results.length];
        memoryData = new double[results.length];
        storageData = new double[results.length];
        batteryData = new double[results.length];
        mobileNetworkStrength = new double[results.length];
        mobileNetworkName = new String[results.length];
        mobileNetworkType = new String[results.length];
        mobileNetworkConnected = new boolean[results.length];
        wifiConnectivityData = new double[results.length];
        latitudeData = new double[results.length];
        longitudeData = new double[results.length];
        size = results.length;
        for (int i = 0; i < results.length; i++) {
            idData[i] = Utils.parseIntFromString(results[i][9]);
            dateData[i] = Utils.getDateFromString(results[i][10], "yyyy-MM-dd HH:mm:ss");
            batteryData[i] = Utils.parseDoubleFromString(results[i][0]);
            cpuData[i] = Utils.parseDoubleFromString(results[i][8]);
            storageData[i] = Utils.parseDoubleFromString(results[i][6]);
            memoryData[i] = Utils.parseDoubleFromString(results[i][7]);
            mobileNetworkStrength[i] = Utils.parseDoubleFromString(results[i][4]);
            mobileNetworkName[i] = results[i][1];
            mobileNetworkType[i] = results[i][2];
            mobileNetworkConnected[i] = results[i][3] == "1";
            wifiConnectivityData[i] = Utils.parseDoubleFromString(results[i][5]);
            latitudeData[i] = Utils.parseDoubleFromString(results[i][11]);
            longitudeData[i] = Utils.parseDoubleFromString(results[i][12]);
        }
    }

    /**
     * Gets The average CPU Utilization for the day
     *
     * @return Double representing average CPU Utilization for the day
     */
    public double getAverageCPUUtilization() {
        return PoorManStatistics.mean(this.cpuData);
    }

    /**
     * Gets the maximum recorded CPU Utilization for the day
     *
     * @return Double representing max CPU Utilization for the day
     */
    public double getMaxCPUUtilization() {
        return PoorManStatistics.max(cpuData);
    }

    /**
     * Gets the minimum recorded CPU Utilization for the day
     *
     * @return Double representing min CPU Utilization for the day
     */
    public double getMinCPUUtilization() {
        return PoorManStatistics.min(cpuData);
    }

    /**
     * Gets the average recorded Memory Utilization for the day
     *
     * @return Double representing average CPU Utilization for the day
     */
    public double getAverageMemoryUtilization() {
        return PoorManStatistics.mean(this.memoryData);
    }

    /**
     * Gets the maximum recorded Memory Utilization for the day
     *
     * @return Double representing max Memory Utilization for the day
     */
    public double getMaxMemoryUtilization() {
        return PoorManStatistics.max(memoryData);
    }

    /**
     * Gets the minimum recorded Memory Utilization for the day
     *
     * @return Double representing min Memory Utilization for the day
     */
    public double getMinMemoryUtilization() {
        return PoorManStatistics.min(memoryData);
    }

    /**
     * Gets the average recorded Storage Utilization for the day
     *
     * @return Double representing average Storage Utilization for the day
     */
    public double getAverageStorageUtilization() {
        return PoorManStatistics.mean(this.storageData);
    }

    /**
     * Gets the maximum recorded Storage Utilization for the day
     *
     * @return Double representing max Storage Utilization for the day
     */
    public double getMaxStorageUtilization() {
        return PoorManStatistics.max(storageData);
    }

    /**
     * Gets the average recorded Storage Utilization for the day
     *
     * @return Double representing average Storage Utilization for the day
     */
    public double getMinStorageUtilization() {
        return PoorManStatistics.min(storageData);
    }

    /**
     * Gets the average recorded GSM signal strength for the day
     *
     * @return Double representing average GSM signal strength for the day
     */
    public double getAverageGSMStrength() {
        return PoorManStatistics.mean(this.mobileNetworkStrength);
    }

    /**
     * Gets the maximum recorded GSM signal strength for the day
     *
     * @return Double representing max GSM signal strength for the day
     */
    public double getMaxGSMStrength() {
        return PoorManStatistics.max(this.mobileNetworkStrength);
    }

    /**
     * Gets the minimum recorded GSM signal strength for the day
     *
     * @return Double representing min GSM signal strength for the day
     */
    public double getMinGSMStrength() {
        return PoorManStatistics.min(this.mobileNetworkStrength);
    }

    /**
     * Gets the average recorded WiFI availability for the day
     *
     * @return Double representing average WiFI availability for the day
     */
    public double getAverageWiFIAvailability() {
        return PoorManStatistics.mean(this.wifiConnectivityData);
    }

    /**
     * Gets the average recorded Battery level for the day
     *
     * @return Double representing average Battery level for the day
     */
    public double getAverageBatteryLevel() {
        return PoorManStatistics.mean(this.batteryData);
    }

    /**
     * Gets the maximum recorded Battery level for the day
     *
     * @return Double representing max Battery level for the day
     */
    public double getMaxBatteryLevel() {
        return PoorManStatistics.max(batteryData);
    }

    /**
     * Gets the minimum recorded Battery level for the day
     *
     * @return Double representing min Battery level for the day
     */
    public double getMinBatteryLevel() {
        return PoorManStatistics.min(batteryData);
    }

    /**
     * Gets the minimum recorded latitude of the phone for the day
     *
     * @return Double representing min latitude of the phone for the day
     */
    public double getMinLatitude() {
        return PoorManStatistics.min(this.latitudeData);
    }

    /**
     * Gets the minimum recorded longitude of the phone for the day
     *
     * @return Double representing min longitude of the phone for the day
     */
    public double getMinLongitude() {
        return PoorManStatistics.min(this.longitudeData);
    }

    /**
     * Gets the maximum recorded latitude of the phone for the day
     *
     * @return Double representing max latitude of the phone for the day
     */
    public double getMaxLatitude() {
        return PoorManStatistics.max(latitudeData);
    }

    /**
     * Gets the maximum recorded longitude of the phone for the day
     *
     * @return Double representing max longitude of the phone for the day
     */
    public double getMaxLongitude() {
        return PoorManStatistics.max(longitudeData);
    }

    /**
     * Gets the number of records being analyzed
     *
     * @return Integer representing the number of records being analyzed for
     * diagnostics
     */
    public int getSize() {
        return this.size;
    }

    public JSONObject getJSONRecords() {
        JSONObject parent = new JSONObject();
        JSONArray analyticData = new JSONArray();
        try {
            for (int index = 0; index < idData.length; index++) {
                JSONObject record = new JSONObject();

                record.put("gsm_signal", mobileNetworkStrength[index]);
                //record.put("mobilenetworkname", mobileNetworkName[index]);
                record.put("gsm_network_type_1", mobileNetworkType[index]);
                //record.put("firstmobilenetworkconnected", mobileNetworkConnected[index]);
                record.put("wifi_connectivity", wifiConnectivityData[index]);
                record.put("cpu_load", cpuData[index]);
                record.put("battery_level", batteryData[index]);
                record.put("storage_usage", storageData[index]);
                record.put("memory_utilization", memoryData[index]);
                record.put("gps_lat", latitudeData[index]);
                record.put("gps_lon", longitudeData[index]);
                record.put("record_date", Utils.getDateString(dateData[index], "yyyy-MM-dd HH:mm:ss"));
                record.put("id", idData[index]);
                analyticData.put(record);

            }
            parent.put("analytic_data", analyticData);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return parent;
    }
}
