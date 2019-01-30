package org.rootio.tools.cloud;

import android.content.Context;
import android.util.Log;

import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

import java.net.InetAddress;

/**
 * This class is the representation of the cloud server
 *
 * @author Jude Mukundane
 */
public class Cloud {
    private InetAddress IPAddress;
    private int HTTPPort;
    private String serverKey, serverScheme, serverAddress;
    private int stationId;

    private Context parent;

    public Cloud(Context context) {
        this.parent = context;
        this.loadCloudInfo();
    }

    public Cloud(Context context, String serverAddress, int HTTPPort, int stationId, String serverKey, String serverScheme) {
        this.parent = context;
        this.HTTPPort = HTTPPort;
        this.serverAddress = serverAddress;
        this.stationId = stationId;
        this.serverKey = serverKey;
        this.serverScheme = serverScheme;
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

    private void loadCloudInfo() {
        try {
            this.serverAddress = (String)Utils.getPreference("server_IP", String.class, this.parent);
            this.HTTPPort = (int)Utils.getPreference("server_port", int.class, this.parent);
            this.stationId = (int)Utils.getPreference("station_id", int.class, this.parent);
            this.serverKey = (String)Utils.getPreference("station_key", String.class, this.parent);
            this.serverScheme = (String)Utils.getPreference("server_scheme", String.class, this.parent);

        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "NullPointer(Station.LoadStationInfo)" : ex.getMessage());
        }
    }

    public String getServerScheme() {
        return this.serverScheme;
    }
}
