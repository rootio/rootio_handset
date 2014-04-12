package org.rootio.activities.cloud;

import org.rootio.radioClient.R;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class CloudConfigurationActivity extends Activity {

	private Cloud cloud;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.cloud_details_configuration);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		this.cloud = new Cloud(this);
	}
	
	/**
	 * Handles click of the Cancel button, which is offloading the current activity.
	 * @param v the view that was clicked
	 */
	public void onCancel(View v)
	{
		this.finish();
	}
	
	/**
	 * Handles the click for the Save button, saving cloud configuration information to the database
	 * @param v The view that was clicked
	 */
	public void onSave(View v)
	{
		this.promptConfigurationChange();
	}
	
	@SuppressLint("NewApi")
	/**
	 * Saves the cloud configuration information that was supplied
	 * @param serverAddress The IP address or domain name of the cloud server excluding the scheme
	 * @param HTTPPort The port that is used for HTTP communication with the cloud
	 * @param serverKey The key that is used to authenticate HTTP communication between the phone and the cloud server
	 * @param serverKeyConfirmation The confirmation of the HTTP key to make sure the user entered it right
	 * @param stationId The ID by which this phone station is identified in the cloud.
	 */
	private void saveCloudConfiguration(String serverAddress, String HTTPPort, String serverKey, String serverKeyConfirmation, String stationId)
	{
		if(!serverKey.equals(serverKeyConfirmation))
		{
			Utils.toastOnScreen("The server key does not match its confirmation");
			return;
		}
		this.cloud.setHTTPPort(Utils.parseIntFromString(HTTPPort));
		this.cloud.setServerAddress(serverAddress.isEmpty()? this.cloud.getServerAddress() : serverAddress);
		this.cloud.setServerKey(serverKey.isEmpty()? this.cloud.getServerKey() : serverKey);
		this.cloud.setStationId(Utils.parseIntFromString(stationId));
		this.cloud.persist();
		Utils.toastOnScreen("Cloud configuration details have been saved.");
		this.finish();
	}
	
	/**
	 * Prompts for confirmation from the user to change the configuration before changes are persisted to the database.
	 */
	private void promptConfigurationChange() {
		new AlertDialog.Builder(this).setIcon(R.drawable.questionmark)
				.setMessage("Change current configuration?")
				.setTitle("Confirmation")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String serverAddress, HTTPPort, serverKey, serverKeyConfirmation, stationId;
						
						EditText serverAddressEt = (EditText)CloudConfigurationActivity.this.findViewById(R.id.cloud_ip_etv);
						serverAddress = serverAddressEt.getText().toString();
						
						EditText cloudPortEt = (EditText)CloudConfigurationActivity.this.findViewById(R.id.cloud_httpport_etv);
						HTTPPort = cloudPortEt.getText().toString();
						
						EditText cloudKeyEt = (EditText)CloudConfigurationActivity.this.findViewById(R.id.cloud_key_etv);
						serverKey = cloudKeyEt.getText().toString();
						
						EditText cloudKeyConfirmationEt = (EditText)CloudConfigurationActivity.this.findViewById(R.id.cloud_confirm_key_etv);
						serverKeyConfirmation = cloudKeyConfirmationEt.getText().toString();
						
						EditText stationIdEt = (EditText)CloudConfigurationActivity.this.findViewById(R.id.cloud_station_id_etv);
						stationId = stationIdEt.getText().toString();
						CloudConfigurationActivity.this.saveCloudConfiguration(serverAddress, HTTPPort, serverKey, serverKeyConfirmation, stationId);
					}
				}).setNegativeButton("Cancel", null).show();
	}

	
	
}
