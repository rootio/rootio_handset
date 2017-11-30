package org.rootio.tools.radio;

import java.net.InetAddress;

import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.util.Log;

public class Station {
    private String location;
    private String network;
    private double frequency;
    private String telephoneNumber;
    private String name;
    private Context parent;
    private InetAddress multicastIPAddress;
    private int multicastPort;

    public Station(Context parent) {
        this.parent = parent;
        this.loadStationInfo();
    }

    /**
     * Gets the street location of this station
     *
     * @return String representation of the location of this station
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Gets the name of the owner of this station. can be a radio network
     *
     * @return String representation of the name of the owner of this station
     */
    public String getNetwork() {
        return this.network;
    }

    /**
     * Gets the telephone number of this station phone
     *
     * @return String representation of the telephone number of this station
     * phone
     */
    public String getTelephoneNumber() {
        return this.telephoneNumber;
    }

    /**
     * Gets the name of this station
     *
     * @return Name of the station
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the FM frequency at which the station for this phone is licensed to
     * broadcast
     *
     * @return Long representation of the frequency of the station
     */
    public double getFrequency() {
        return this.frequency;
    }

    /**
     * Gets the IP address of the multicast group to which the phone subscribes
     * for streaming server announcements
     *
     * @return InetAddress of the Multicast group to join for announcements
     */
    public InetAddress getMulticastIPAddress() {
        return this.multicastIPAddress;
    }

    /**
     * Gets the port on which the phone listens for multicast service
     * announcements
     *
     * @return Integer port number of the multicast port
     */
    public int getMulticastPort() {
        return this.multicastPort;
    }

    private void loadStationInfo() {
        try {
            JSONObject stationInformation = Utils.getJSONFromFile(this.parent, this.parent.getFilesDir().getAbsolutePath() + "/station.json");
            if (stationInformation.getJSONObject("station").has("location")) {
                this.location = String.format("%s (lat/lng: %s,  %s)", stationInformation.getJSONObject("station").getJSONObject("location").optString("name"), stationInformation.getJSONObject("station").getJSONObject("location").optString("latitude"), stationInformation.getJSONObject("station").getJSONObject("location").optString("longitude"));
            }
            this.telephoneNumber = stationInformation.getJSONObject("station").optString("telephone");
            this.name = stationInformation.getJSONObject("station").optString("name");
            this.network = stationInformation.getJSONObject("station").optString("network");
            this.multicastPort = stationInformation.getJSONObject("station").optInt("multicast_port");
            this.frequency = stationInformation.getJSONObject("station").optDouble("frequency");
            this.multicastIPAddress = Utils.parseInetAddressFromString(stationInformation.getJSONObject("station").optString("multicast_IP"));
        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "NullPointer(Station.LoadStationInfo)" : ex.getMessage());
        }
    }

}
