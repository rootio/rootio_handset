package org.rootio.tools.cloud;

import java.net.InetAddress;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

/**
 * This class is the representation of the cloud server
 * 
 * @author Jude Mukundane
 * 
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

	private Context context;

	public Cloud(Context context) {
		this.context = context;
		this.loadCloudInfo();
	}

	/**
	 * Gets the Address of the cloud server as either an IP address or domain
	 * name
	 * 
	 * @return String representation of cloud server address
	 */
	public String getServerAddress() {
		return "192.168.10.180"; //this.serverAddress;
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
		return 80; // this.HTTPPort;
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
		return "HBCDRi7SRP"; //this.serverKey;
	}

	/**
	 * Gets the ID representing this station on the cloud server
	 * 
	 * @return Integer representing the ID of the station on the server
	 */
	public int getStationId() {
		return 10; // this.stationId;
	}

	/**
	 * Sets the HTTP port on which the cloud server is listening
	 * 
	 * @param HTTPPort
	 *            The value to which to set the HTTP port
	 */
	public void setHTTPPort(int HTTPPort) {
		this.HTTPPort = HTTPPort;
	}

	/**
	 * Sets the address of the cloud server
	 * 
	 * @param serverAddress
	 *            The Address to which to set the cloud server
	 */
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	/**
	 * Sets the IP address of the cloud server
	 * 
	 * @param IPAddress
	 *            The IP address of the cloud server
	 */
	public void setIPAddress(InetAddress IPAddress) {
		this.IPAddress = IPAddress;
	}

	/**
	 * Sets the key used to authenticate on the cloud server
	 * 
	 * @param serverKey
	 *            The server key
	 */
	public void setServerKey(String serverKey) {
		this.serverKey = serverKey;
	}

	/**
	 * Sets the ID of the station as recorded on the cloud server
	 * 
	 * @param stationId
	 *            The ID of the station
	 */
	public void setStationId(int stationId) {
		this.stationId = stationId;
	}

	/**
	 * Fetches the configuration of the cloud server as it is stored in the
	 * database
	 */
	private void loadCloudInfo() {
		/*
		 * String tableName = "cloud"; String[] columnsToFetch = new String[] {
		 * "ipaddress", "httpport", "ftpport", "rawtcpport",
		 * "telephonenumber","username","password","serverkey", "stationid" };
		 * DBAgent dbAgent = new DBAgent(this.context); String[][] cloudDetails
		 * = dbAgent.getData(true, tableName, columnsToFetch, null, null, null,
		 * null, null, null); if (cloudDetails.length > 0) {
		 */this.IPAddress = Utils.parseInetAddressFromString("192.168.1.136");
		this.serverAddress = "192.168.1.136";
		this.HTTPPort = 80;
		// this.telephoneNumber = cloudDetails[0][4];
		// this.username = cloudDetails[0][5];
		// this.password = cloudDetails[0][6];
		this.stationId = 10;
		this.serverKey = "HBCDRi7SRP";
		// }
	}

}
