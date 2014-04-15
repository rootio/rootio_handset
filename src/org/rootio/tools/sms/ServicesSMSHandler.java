package org.rootio.tools.sms;

import org.rootio.services.DiagnosticsService;
import org.rootio.services.ProgramService;
import org.rootio.services.TelephonyService;

import android.content.Context;
import android.content.Intent;

public class ServicesSMSHandler implements MessageProcessor {

	private Context parent;
	private String from;
	private String[] messageParts;

	public ServicesSMSHandler(Context parent, String from, String[] messageParts) {
		this.parent = parent;
		this.messageParts = messageParts;
	}

	@Override
	public boolean ProcessMessage() {
		if (messageParts.length != 4) {
			return false;
		}

		// stopping a service
		if (messageParts[2].equals("stop")) {
			try {
				return this.stopService(Integer.parseInt(messageParts[3]));
			} catch (Exception ex) {
				return false;
			}
		}
		
		//starting a srevice
		if (messageParts[2].equals("start")) {
			try {
				return this.startService(Integer.parseInt(messageParts[3]));
			} catch (Exception ex) {
				return false;
			}
		}
		
		//getting the service status
		if (messageParts[2].equals("status")) {
			try {
				return this.getServiceStatus(Integer.parseInt(messageParts[3]));
			} catch (Exception ex) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Starts the service whose ID is specified
	 * @param serviceId The ID of the service to start
	 * @return Boolean indicating whether or not the operatoin was successful
	 */
	private boolean startService(int serviceId) {
		Intent intent  = this.getServiceIntent(serviceId);
		if(intent == null)
		{
			return false;
		}
		this.parent.startService(intent);
		return false;
	}

	/**
	 * Stops the Service whose ID is specified
	 * @param serviceId The ID of the service to be stopped
	 * @return Boolean indicating whether or not the operation was successful
	 */
	private boolean stopService(int serviceId) {
		return false;
	}

	/**
	 * Gets the status of the service whose ID is specified
	 * @param serviceId The ID of the service whose status to return
	 * @return Boolean indicating whether or not the service is running. True: Running, False: Not running
	 */
	private boolean getServiceStatus(int serviceId) {
		return false;
	}

	/**
	 * Gets the Intent to be used to communicate with the intended service
	 * @param serviceId The ID of the service with which to communicate
	 * @return The intent to be used in communicating with the desired service
	 */
	private Intent getServiceIntent(int serviceId)
	{
		Intent intent = null;
		switch(serviceId)
		{
		case 1:
			intent = new Intent(this.parent, TelephonyService.class);
			break;
		case 2: //the SMS service can not be stopped remotely
			break;
		case 3:
			intent = new Intent(this.parent, DiagnosticsService.class);
			break;
		case 4:
			intent = new Intent(this.parent, ProgramService.class);
			break;
		case 5: //not yet implemented
			break;		
	}
		return intent;
	}

	@Override
	public void respondAsyncStatusRequest(String from, String data) {
		// TODO Auto-generated method stub
		
	}
}
