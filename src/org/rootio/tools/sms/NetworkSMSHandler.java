package org.rootio.tools.sms;

import org.rootio.tools.diagnostics.DiagnosticAgent;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.SmsManager;

public class NetworkSMSHandler implements MessageProcessor {

	private String[] messageParts;
	private Context parent;
	private String from;
	
	NetworkSMSHandler(Context parent, String from, String[] messageParts)
	{
		this.parent = parent;
		this.from = from;
		this.messageParts = messageParts;
	}

	@Override
	public boolean ProcessMessage() {
		
		//not enough parameters
		if(this.messageParts.length < 4)
		{
			return false;
		}
		
		if(messageParts[2].equals("wifi"))
		{
			if(messageParts[3].equals("on") || messageParts[3].equals("off"))
			{
				return this.toggleWifi(messageParts[3]);
			}
			
			if(messageParts[3].equals("status"))
			{
				return this.getWifiConnection();
			}
			
			if(messageParts[3].equals("connect"))
			{
				return this.connectToWifi(messageParts[4], messageParts[5]);
			}
		}
		
		if(messageParts[2].equals("gsm"))
		{
			if(messageParts[3].equals("status"))
			{
				return this.getGsmConnection();
			}
		}
		
		//Gibberish
		return false;
	}
	
	private boolean toggleWifi(String state)
	{
		try
		{
			WifiManager wifiManager = (WifiManager)parent.getSystemService(Context.WIFI_SERVICE);
			return wifiManager.setWifiEnabled(state.equals("on"));
		}
		catch(Exception ex)
		{
			return false;
		}
	}
	
	private boolean connectToWifi(String SSID, String password)
	{
		try
		{
			WifiManager wifiManager = (WifiManager) parent.getSystemService(Context.WIFI_SERVICE);
			wifiManager.setWifiEnabled(true);
			try {
				Thread.sleep(5000); //wifi needs to be enabled long before configuring network
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    WifiConfiguration wc = new WifiConfiguration(); 
		    wc.SSID = String.format("\"%s\"", SSID); //IMP! This should be in Quotes!!
		    wc.preSharedKey = String.format("\"%s\"", password);
		    wc.status = WifiConfiguration.Status.ENABLED;
		    wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	        // connect to and enable the connection
	        int netId = wifiManager.addNetwork(wc);
	        return wifiManager.enableNetwork(netId, true);
		}
		catch(Exception ex)
		{
			return false;
		}
	}
	
	private boolean  getGsmConnection()
	{
		try
		{
		DiagnosticAgent diagnosticAgent = new DiagnosticAgent(this.parent);
		diagnosticAgent.runDiagnostics();
		String response = String.format("connected to %s with signal strength %s", diagnosticAgent.getTelecomOperatorName(), diagnosticAgent.getGSMConnectionStrength());
		this.respondAsyncStatusRequest(response, from);
		return true;
		}
		catch(Exception ex)
		{
			return false;
		}
		
	}
	
	private boolean getWifiConnection ()
	{
		WifiManager wifiManager = (WifiManager)this.parent.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if(wifiInfo == null)
		{
			return false;
		}
		else
		{
			String response = String.format("Connected to %s with IP address %s", wifiInfo.getSSID(), wifiInfo.getIpAddress());
			this.respondAsyncStatusRequest(response, from);
			return true;
		}
	}
	
	private boolean toggleBluetooth()
	{
		return false;
	}
	
	private boolean connectBluetoothDevice(String deviceName)
	{
		return false;
	}

	@Override
	public void respondAsyncStatusRequest(String from, String data) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(from, null, data, null, null);
		
	}
	
	
}
