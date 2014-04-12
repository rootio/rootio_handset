package org.rootio.activities.stationDetails;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MulticastConfigurationActivity extends Activity implements DialogInterface.OnClickListener, Runnable{
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setTitle("Multicast Configuration");
		this.setContentView(R.layout.multicast_configuration);
	}
	
	/**
	 * Handles the click for the save button
	 * @param v the view (Save button) that was clicked
	 */
	public void onSave(View v)
	{
		
	    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	    AlertDialog dialog = alertDialogBuilder.create();
	    dialog.setTitle("Confirmation");
	    dialog.setMessage("Change multicast configuration?");
	    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", this);
	    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",this);
	    dialog.show();
	}
	
	/**
	 * Handles the click for the cancel button, effectively finishing the activity
	 * @param v the view (cancel button) that was clicked
	 */
	public void onCancel(View v)
	{
		this.finish();
	}
	
	/**
	 * Saves the multicast configuration  of the group to join to receive streaming server announcements.
	 * @param serverAddress The address of the broadcast group to join to receive broadcast announcements 
	 * @param port The port at which to listen for multicast announcements
	 */
	private void saveConfiguration(InetAddress serverAddress, int port)
	{
		String tableName = "station";
		ContentValues data = new ContentValues();
		data.put("multicastipaddress", serverAddress.getHostAddress());
		data.put("multicastport", port);
		DBAgent dbAgent = new DBAgent(this);
		dbAgent.updateRecords(tableName, data, null, null);			
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which)
		{
		case DialogInterface.BUTTON_POSITIVE:
			Thread thread = new Thread(this);
			thread.start();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			dialog.dismiss();
			break;
		}
	}

	@Override
	public void run() {
		try
		{ 
			TextView ipAddressTv = (TextView)this.findViewById(R.id.multicast_configuration_ipaddress_etv);
			InetAddress inetAddress = InetAddress.getByName(ipAddressTv.getText().toString());
			TextView portTv = (TextView)this.findViewById(R.id.multicast_configuration_port_etv);
			if(!this.isMulticastAddress(inetAddress))
			{
				Utils.toastOnScreen("Please enter an IP address between 224.0.0.0 and 239.255.255.255");
				return;
			}
			int port = Integer.parseInt(portTv.getText().toString());
			this.saveConfiguration(inetAddress, port);
			Utils.toastOnScreen("The multicast IP address and port number have been successfully saved.");
			this.finish();
		}
		catch(UnknownHostException ex)
		{
			Utils.toastOnScreen("Please supply a valid IP address");
		}
		catch(NumberFormatException ex)
		{
			Utils.toastOnScreen("Please supply a valid port number");	
		}	
		
	}

	/**
	 * Validates supplied IP address for conformity to broadcast address specifications
	 * @param inetAddress the supplied Address for the broadcast group
	 * @return Boolean indicating whether address conforms to broadcast specifications. True: conforms, False: does not conform
	 */
	private boolean isMulticastAddress(InetAddress inetAddress)
	{
		BigInteger baseAddress = new BigInteger(new byte[]{(byte)224,0,0,0});
		BigInteger ceilAddress = new BigInteger(new byte[]{(byte)239, (byte)255,(byte)255,(byte)255});
		BigInteger selectedAddress = new BigInteger(inetAddress.getAddress());
		return selectedAddress.compareTo(baseAddress) >= 0 && selectedAddress.compareTo(ceilAddress) <= 0;
	}
}
