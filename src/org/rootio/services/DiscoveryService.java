package org.rootio.services;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;

public class DiscoveryService extends Service  implements RunningStatusPublished{

	private InetAddress multicastAddress;
	private int port;
	private boolean isRunning;
	private int serviceId = 6;

	@Override
	public IBinder onBind(Intent arg0) {
		BindingAgent bindingAgent = new BindingAgent(this);
		return bindingAgent;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if(!isRunning)
		{
		this.loadServiceDiscoveryConfiguration();
		StreamingServerAnnouncementListener listener = new StreamingServerAnnouncementListener();
		isRunning = true;
		Thread thread = new Thread(listener);
		thread.start();
		Utils.doNotification(this, "RootIO", "Discovery Service Started");
		this.sendEventBroadcast();
		}
		return Service.START_STICKY;
		
	}
	
	@Override
	public void onDestroy()
	{
		Utils.toastOnScreen("stop received");
		this.isRunning = false;
		Utils.doNotification(this, "RootIO", "Discovery Service Stopped");
		this.sendEventBroadcast();
	}

	public int getServiceId()
	{
		return this.serviceId;
	}
	
	@Override
	public boolean isRunning()
	{
		return this.isRunning;
	}
	
	private void saveStreamingConfiguration(InetAddress serverAddress,
			int port, String path) {
		String tableName = "streamingconfiguration";
		ContentValues data = new ContentValues();
		data.put("ipaddress", serverAddress.getHostAddress());
		data.put("port", port);
		data.put("path", path);
		DBAgent dbAgent = new DBAgent(this);
		dbAgent.saveData(tableName, null, data);
	}

	private void loadServiceDiscoveryConfiguration() {
		String tableName = "station";
		String[] columns = new String[] { "multicastipaddress", "multicastport" };
		DBAgent dbAgent = new DBAgent(this);
		String[][] results = dbAgent.getData(true, tableName, columns, null,
				null, null, null, null, null);
		if (results.length > 0) {
			try {
				Utils.toastOnScreen("ip "+results[0][0]+ " port "+results[0][1]);
				this.multicastAddress = InetAddress.getByName(results[0][0]);
				this.port = Utils.parseIntFromString(results[0][1]);
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendEventBroadcast() {
		Intent intent = new Intent();
		intent.putExtra("serviceId", this.serviceId);
		intent.putExtra("isRunning", this.isRunning);
		intent.setAction("org.rootio.services.discovery.EVENT");
		this.sendBroadcast(intent);
	}

	enum Status{SUCCESS, FAILURE};
	
	class StreamingServerAnnouncementListener implements Runnable {
		byte[] receivedData = new byte[1024];
		byte[] sentData = new byte[1024];
		

		@Override
		public void run() {
			try {
				MulticastSocket socket = new MulticastSocket(
						DiscoveryService.this.port);
				socket.joinGroup(DiscoveryService.this.multicastAddress);
				while (DiscoveryService.this.isRunning) {
					DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
					socket.receive(receivedPacket);
					String message = new String(receivedPacket.getData());
					Utils.toastOnScreen("received message "+message);
					String response = this.processMessage(message);
					sentData = response.getBytes();
					DatagramPacket sentPacket = new DatagramPacket(sentData, sentData.length, receivedPacket.getAddress(), receivedPacket.getPort());
					socket.send(sentPacket);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e)
			{
				Utils.toastOnScreen("Can not Start service. Please specify the Multicast IP address and port number");
				DiscoveryService.this.onDestroy();
			}
		}

		private String processMessage(String message) {
			JSONObject json;
			try {
				json = new JSONObject(message);
				int port = json.getInt("port");
				String ipAddress = json.getString("ipaddress");
				String path = json.getString("path");
				InetAddress serverAddress = InetAddress.getByName(ipAddress);
				DiscoveryService.this.saveStreamingConfiguration(serverAddress,	port, path);
				return this.getStatusMessage(Status.SUCCESS);
			} catch (JSONException e) {
				return this.getStatusMessage(Status.FAILURE);
			} catch (UnknownHostException e)
			{
				return this.getStatusMessage(Status.FAILURE);
			}	
		}
		
		private String getStatusMessage(Status status)
		{
			JSONObject json = new JSONObject();
			try {
				json.put("status", status.name());
			} catch (JSONException e) {
				//log this
			}
			return json.toString();
		}
	}
	
}
