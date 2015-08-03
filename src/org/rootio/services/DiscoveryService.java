package org.rootio.services;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DiscoveryService extends Service implements ServiceInformationPublisher {

	private InetAddress multicastAddress;
	private int port;
	private boolean isRunning;
	private final int serviceId = 6;
	private boolean wasStoppedOnPurpose = true;

	@Override
	public IBinder onBind(Intent arg0) {
		BindingAgent bindingAgent = new BindingAgent(this);
		return bindingAgent;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!isRunning) {
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
	public void onTaskRemoved(Intent intent)
	{
		super.onTaskRemoved(intent);
		if(intent != null)	
		{
			wasStoppedOnPurpose  = intent.getBooleanExtra("wasStoppedOnPurpose", false);
			if(wasStoppedOnPurpose)
			{
				this.shutDownService();
			}
			else
			{
				this.onDestroy();
			}
		}
	}

	@Override
	public void onDestroy() {
		if(this.wasStoppedOnPurpose == false)
		{
			Intent intent = new Intent("org.rootio.services.restartServices");
			sendBroadcast(intent);
		}
		else
		{
			this.shutDownService();
		}
		super.onDestroy();
	}

	private void shutDownService() {
		if (this.isRunning) {
			this.isRunning = false;
			Utils.doNotification(this, "RootIO", "Discovery Service Stopped");
			this.sendEventBroadcast();
		}
	}

	/**
	 * Gets the ID of this service
	 * 
	 * @return Integer representation of the ID of this service
	 */
	@Override
	public int getServiceId() {
		return this.serviceId;
	}

	@Override
	public boolean isRunning() {
		return this.isRunning;
	}

	/**
	 * Persists the streaming the configuration of the streaming server received
	 * from broadcasts on the network
	 * 
	 * @param serverAddress
	 *            The Address of the streaming server
	 * @param port
	 *            The port at which the streaming server is listening
	 * @param path
	 *            The path to the content to be played during the show
	 */
	private void saveStreamingConfiguration(InetAddress serverAddress, int port, String path) {
		String tableName = "streamingconfiguration";
		ContentValues data = new ContentValues();
		data.put("ipaddress", serverAddress.getHostAddress());
		data.put("port", port);
		data.put("path", path);
		DBAgent dbAgent = new DBAgent(this);
		dbAgent.saveData(tableName, null, data);
	}

	/**
	 * Fetches the configuration for the discovery service from the database
	 */
	private void loadServiceDiscoveryConfiguration() {
		String tableName = "station";
		String[] columns = new String[] { "multicastipaddress", "multicastport" };
		DBAgent dbAgent = new DBAgent(this);
		String[][] results = dbAgent.getData(true, tableName, columns, null, null, null, null, null, null);
		if (results.length > 0) {
			try {
				this.multicastAddress = InetAddress.getByName(results[0][0]);
				this.port = Utils.parseIntFromString(results[0][1]);

			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends out broadcasts to listeners informing them of change in the state
	 * of the service
	 */
	private void sendEventBroadcast() {
		Intent intent = new Intent();
		intent.putExtra("serviceId", this.serviceId);
		intent.putExtra("isRunning", this.isRunning);
		intent.setAction("org.rootio.services.discovery.EVENT");
		this.sendBroadcast(intent);
	}

	/**
	 * Defines the statuses that can be reached at the end of receipt and
	 * persistence of a discovery announcement
	 * 
	 * @author Jude Mukundane
	 * 
	 */
	enum Status {
		SUCCESS, FAILURE
	};

	/**
	 * This class is used to listen for broadcast announcements on the network
	 * for streaming configuration
	 * 
	 * @author HP Envy
	 * 
	 */
	class StreamingServerAnnouncementListener implements Runnable {
		byte[] receivedData = new byte[1024];
		byte[] sentData = new byte[1024];

		@Override
		public void run() {
			try {
				MulticastSocket socket = new MulticastSocket(DiscoveryService.this.port);
				socket.joinGroup(DiscoveryService.this.multicastAddress);
				while (DiscoveryService.this.isRunning) {
					DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
					socket.receive(receivedPacket);
					String message = new String(receivedPacket.getData());
					String response = this.processMessage(message);
					sentData = response.getBytes();
					DatagramPacket sentPacket = new DatagramPacket(sentData, sentData.length, receivedPacket.getAddress(), receivedPacket.getPort());
					socket.send(sentPacket);
				}
			} catch (IOException e) {
				Log.e(DiscoveryService.this.getString(R.string.app_name), e.getMessage());
			} catch (NullPointerException e) {
				Utils.toastOnScreen("Can not Start service. Please specify the Multicast IP address and port number", DiscoveryService.this);
				Log.e(DiscoveryService.this.getString(R.string.app_name), e.getMessage() == null ? "Null pointer exception" : e.getMessage());
				DiscoveryService.this.onDestroy();
			}
		}

		/**
		 * Processes the JSON string received in the streaming service
		 * announcement
		 * 
		 * @param message
		 *            The message that was received in the streaming server
		 *            announcement
		 * @return JSON String message with the result of the operation to be
		 *         returned to the streaming server
		 */
		private String processMessage(String message) {
			JSONObject json;
			try {
				json = new JSONObject(message);
				int port = json.getInt("port");
				String ipAddress = json.getString("ipaddress");
				String path = json.getString("path");
				InetAddress serverAddress = InetAddress.getByName(ipAddress);
				DiscoveryService.this.saveStreamingConfiguration(serverAddress, port, path);
				return this.getStatusMessage(Status.SUCCESS);
			} catch (JSONException e) {
				Log.e(DiscoveryService.this.getString(R.string.app_name), e.getMessage());
				return this.getStatusMessage(Status.FAILURE);
			} catch (UnknownHostException e) {
				Log.e(DiscoveryService.this.getString(R.string.app_name), e.getMessage());
				return this.getStatusMessage(Status.FAILURE);
			}
		}

		/**
		 * Constructs the JSON message to be returned to the streaming server
		 * upon a service announcement
		 * 
		 * @param status
		 *            The status of processing the announcement that was
		 *            received
		 * @return A JSON string with status information about the processing of
		 *         the received announcement
		 */
		private String getStatusMessage(Status status) {
			JSONObject json = new JSONObject();
			try {
				json.put("status", status.name());
			} catch (JSONException e) {
				Log.e(DiscoveryService.this.getString(R.string.app_name), e.getMessage());
			}
			return json.toString();
		}
	}

}
