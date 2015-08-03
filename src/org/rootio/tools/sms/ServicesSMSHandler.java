package org.rootio.tools.sms;

import org.rootio.services.DiagnosticsService;
import org.rootio.services.Notifiable;
import org.rootio.services.ProgramService;
import org.rootio.services.ServiceConnectionAgent;
import org.rootio.services.ServiceInformationPublisher;
import org.rootio.services.TelephonyService;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class ServicesSMSHandler implements MessageProcessor, Notifiable {

	private final Context parent;
	private final String from;
	private final String[] messageParts;
	private ServiceConnectionAgent serviceConnectionAgent;

	public ServicesSMSHandler(Context parent, String from, String[] messageParts) {
		this.parent = parent;
		this.from = from;
		this.messageParts = messageParts;
	}

	@Override
	public boolean ProcessMessage() {
		if (messageParts.length != 3) {
			return false;
		}

		// stopping a service
		if (messageParts[1].equals("stop")) {
			try {
				return this.stopService(Integer.parseInt(messageParts[2]));
			} catch (Exception ex) {
				return false;
			}
		}

		// starting a srevice
		if (messageParts[1].equals("start")) {
			try {
				Utils.toastOnScreen("starting service " + messageParts[2], this.parent);
				return this.startService(Integer.parseInt(messageParts[2]));
			} catch (Exception ex) {
				return false;
			}
		}

		// getting the service status
		if (messageParts[1].equals("status")) {
			try {
				return this.getServiceStatus(Integer.parseInt(messageParts[2]));
			} catch (Exception ex) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Starts the service whose ID is specified
	 * 
	 * @param serviceId
	 *            The ID of the service to start
	 * @return Boolean indicating whether or not the operatoin was successful
	 */
	private boolean startService(int serviceId) {
		Intent intent = this.getServiceIntent(serviceId);
		if (intent == null) {
			return false;
		}
		this.parent.startService(intent);
		this.respondAsyncStatusRequest("start ok", from);
		return true;
	}

	/**
	 * Stops the Service whose ID is specified
	 * 
	 * @param serviceId
	 *            The ID of the service to be stopped
	 * @return Boolean indicating whether or not the operation was successful
	 */
	private boolean stopService(int serviceId) {
		Intent intent = new Intent();
		intent.setAction("org.rootio.services.STOP_EVENT");
		intent.putExtra("serviceId", serviceId);
		this.parent.sendBroadcast(intent);
		// try to shutdown
		Intent intent2 = this.getServiceIntent(serviceId);
		if (intent2 == null) {
			return false;
		}
		this.parent.stopService(intent2);
		this.respondAsyncStatusRequest("start ok", from);
		return true;
	}

	/**
	 * Gets the status of the service whose ID is specified
	 * 
	 * @param serviceId
	 *            The ID of the service whose status to return
	 * @return Boolean indicating whether or not the service is running. True:
	 *         Running, False: Not running
	 */
	private boolean getServiceStatus(int serviceId) {
		this.bindToService(serviceId);
		return true;
	}

	/**
	 * Gets the Intent to be used to communicate with the intended service
	 * 
	 * @param serviceId
	 *            The ID of the service with which to communicate
	 * @return The intent to be used in communicating with the desired service
	 */
	private Intent getServiceIntent(int serviceId) {
		Intent intent = null;
		switch (serviceId) {
			case 1:
				intent = new Intent(this.parent, TelephonyService.class);
				break;
			case 2: // the SMS service can not be stopped remotely
				break;
			case 3:
				intent = new Intent(this.parent, DiagnosticsService.class);
				break;
			case 4:
				intent = new Intent(this.parent, ProgramService.class);
				break;
			case 5: // not yet implemented
				break;
		}
		return intent;
	}

	/**
	 * Binds to the program service to get status of programs that are displayed
	 * on the home radio screen
	 */
	private void bindToService(int serviceId) {
		serviceConnectionAgent = new ServiceConnectionAgent(this, 4);
		Intent intent = this.getServiceIntent(serviceId);
		if (this.parent.bindService(intent, serviceConnectionAgent, Context.BIND_AUTO_CREATE)) {
			// just wait for the async call
		}
	}

	@Override
	public void respondAsyncStatusRequest(String from, String data) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(from, null, data, null, null);
	}

	@Override
	public void notifyServiceConnection(int serviceId) {
		ServiceInformationPublisher service = this.serviceConnectionAgent.getService();
		this.notifyServiceStatus(serviceId, service.isRunning());

	}

	private void notifyServiceStatus(int serviceId, boolean running) {
		this.respondAsyncStatusRequest(this.from, running ? String.format("Service %s running", serviceId) : String.format("Service %s not running", serviceId));
	}

	@Override
	public void notifyServiceDisconnection(int serviceId) {
		// TODO Auto-generated method stub

	}
}
