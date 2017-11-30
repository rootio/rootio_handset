package org.rootio.tools.cloud;

import java.net.InetAddress;

import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.util.Log;

/**
 * This class is the representation of the cloud server
 *
 * @author Jude Mukundane
 */
public class Cloud {
    private InetAddress IPAddress;
    private String serverAddress;
    private int HTTPPort;
    private String username;
    private String password;
    private String telephoneNumber;
    private String serverKey;
    private int stationId;

    private Context parent;

    public Cloud(Context context) {
        this.parent = context;
        this.loadCloudInfo();
    }

    public Cloud(Context context, String serverAddress, int HTTPPort, int stationId, String serverKey) {
        this.parent = context;
        this.HTTPPort = HTTPPort;
        this.serverAddress = serverAddress;
        this.stationId = stationId;
        this.serverKey = serverKey;
    }

    /**
     * Gets the Address of the cloud server as either an IP address or domain
     * name
     *
     * @return String representation of cloud server address
     */
    public String getServerAddress() {
        return this.serverAddress;
    }

    /**
     * Gets the IP address of the cloud server
     *
     * @return InetAddress object representig cloud server IP address
     */
    public InetAddress getIPAddress() {
        return this.IPAddress;
    }

    /**
     * Gets the port at which the cloud HTTP server is running
     *
     * @return Integer representing the HTTP port of the cloud server
     */
    public int getHTTPPort() {
        return this.HTTPPort;
    }

    /**
     * Gets the Telephone number for the cloud server
     *
     * @return String representing telephone number of the cloud server
     */
    public String getTelephoneNumber() {
        return this.telephoneNumber;
    }

    /**
     * Gets the username that is used to authenticate to the cloud server
     *
     * @return String representing the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Gets the password that is used to authenticate to the cloud server
     *
     * @return String representation of the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Gets the key that is used to authenticate on the cloud server
     *
     * @return String representing the station key
     */
    public String getServerKey() {
        return this.serverKey;
    }

    /**
     * Gets the ID representing this station on the cloud server
     *
     * @return Integer representing the ID of the station on the server
     */
    public int getStationId() {
        return this.stationId;
    }

    /**
     * Sets the HTTP port on which the cloud server is listening
     *
     * @param HTTPPort The value to which to set the HTTP port
     */
    public void setHTTPPort(int HTTPPort) {
        this.HTTPPort = HTTPPort;
    }

    /**
     * Sets the address of the cloud server
     *
     * @param serverAddress The Address to which to set the cloud server
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * Sets the IP address of the cloud server
     *
     * @param IPAddress The IP address of the cloud server
     */
    public void setIPAddress(InetAddress IPAddress) {
        this.IPAddress = IPAddress;
    }

    /**
     * Sets the key used to authenticate on the cloud server
     *
     * @param serverKey The server key
     */
    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    /**
     * Sets the ID of the station as recorded on the cloud server
     *
     * @param stationId The ID of the station
     */
    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    private void loadCloudInfo() {
        try {
            JSONObject cloudInformation = Utils.getJSONFromFile(this.parent, this.parent.getFilesDir().getAbsolutePath() + "/cloud.json");
            this.serverAddress = cloudInformation.optString("server_IP");
            this.HTTPPort = cloudInformation.optInt("server_port");
            this.stationId = cloudInformation.optInt("station_id");
            this.serverKey = cloudInformation.optString("station_key");

        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "NullPointer(Station.LoadStationInfo)" : ex.getMessage());
        }
    }
}
