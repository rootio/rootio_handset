package org.rootio.tools.cloud;

import java.net.InetAddress;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

public class Cloud {
	private InetAddress IPAddress;
	private String serverAddress;
	private int HTTPPort;
	private int FTPPort;
	private int rawTCPPort;
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
	
	public String getServerAddress()
	{
		return this.serverAddress;
	}

	public InetAddress getIPAddress() {
		return this.IPAddress;
	}

	public int getHTTPPort() {
		return this.HTTPPort;
	}

	public String getTelephoneNumber() {
		return this.telephoneNumber;
	}

	public int getFTPPort() {
		return this.FTPPort;
	}

	public int getRawTCPPort() {
		return this.rawTCPPort;
	}

	public String getUsername()
	{
		return this.username;
	}
	
	public String getPassword()
	{
		return this.password;
	}
	
	private int getParsedValue(String input)
	{
		try 
		{
			return Integer.parseInt(input);
		}
		catch(NumberFormatException ex)
		{
			return 0;
		}
	}
	
	public String getServerKey()
	{
		return this.serverKey;
	}
	
	public int getStationId()
	{
		return this.stationId;
	}
	
	public void setHTTPPort(int HTTPPort)
	{
		this.HTTPPort = HTTPPort;
	}
	
	public void setServerAddress(String serverAddress)
	{
		this.serverAddress = serverAddress;
	}
	
	public void setIPAddress(InetAddress IPAddress)
	{
		this.IPAddress = IPAddress;
	}
	
	public void setServerKey(String serverKey)
	{
		this.serverKey = serverKey;
	}
	
	public void setStationId(int stationId)
	{
		this.stationId = stationId;
	}
	
	public void persist()
	{
		String tableName = "cloud";
		ContentValues data = new ContentValues();
		data.put("ipaddress", this.serverAddress);
		data.put("httpport", this.HTTPPort);
		data.put("serverkey", this.serverKey);
		data.put("stationid", this.stationId);
		DBAgent dbAgent = new DBAgent(this.context);
		dbAgent.updateRecords(tableName, data, null, null);
	}

	private void loadCloudInfo() {
		String tableName = "cloud";
		String[] columnsToFetch = new String[] { "ipaddress", "httpport", "ftpport", "rawtcpport", "telephonenumber","username","password","serverkey", "stationid" };
		DBAgent dbAgent = new DBAgent(this.context);
		String[][] cloudDetails = dbAgent.getData(true, tableName, columnsToFetch, null, null, null, null, null, null);
		if (cloudDetails.length > 0) {
			this.IPAddress = Utils.parseInetAddressFromString(cloudDetails[0][0]);
			this.serverAddress = cloudDetails[0][0];
			this.HTTPPort = getParsedValue(cloudDetails[0][1]);
			this.FTPPort = getParsedValue(cloudDetails[0][2]);
			this.rawTCPPort = getParsedValue(cloudDetails[0][3]);
			this.telephoneNumber = cloudDetails[0][4];
			this.username = cloudDetails[0][5];
			this.password = cloudDetails[0][6];
			this.stationId = Utils.parseIntFromString(cloudDetails[0][8]);
			this.serverKey = cloudDetails[0][7];
		}
	}

}
