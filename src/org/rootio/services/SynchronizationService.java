package org.rootio.services;

import org.rootio.services.synchronization.SynchronizationDaemon;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SynchronizationService extends Service implements ServiceInformationPublisher {

	private final int serviceId = 5;
	private boolean isRunning;
	private boolean wasStoppedOnPurpose = true;

	@Override
	public IBinder onBind(Intent arg0) {
		return new BindingAgent(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!this.isRunning) {
			SynchronizationDaemon synchronizationDaemon = new SynchronizationDaemon(this);
			Thread thread = new Thread(synchronizationDaemon);
			this.isRunning = true;
			thread.start();
			this.sendEventBroadcast();
			Utils.doNotification(this, "RootIO", "Synchronization Service Started");
		}
		return Service.START_STICKY;
	}

	@Override
	public void onTaskRemoved(Intent intent) {
		super.onTaskRemoved(intent);
		if (intent != null) {
			wasStoppedOnPurpose = intent.getBooleanExtra("wasStoppedOnPurpose", false);
			if (wasStoppedOnPurpose) {
				this.shutDownService();
			} else {
				this.onDestroy();
			}
		}
	}

	@Override
	public void onDestroy() {
		if (this.wasStoppedOnPurpose == false) {
			Intent intent = new Intent("org.rootio.services.restartServices");
			sendBroadcast(intent);
		} else {
			this.shutDownService();
		}
		super.onDestroy();
	}

	private void shutDownService() {
		if (this.isRunning) {
			this.isRunning = false;
			Utils.doNotification(this, "RootIO", "Synchronization Service Stopped");
			this.sendEventBroadcast();
		}
	}

	/**
	 * Sends out broadcasts informing listeners of changes in service status
	 */
	private void sendEventBroadcast() {
		Intent intent = new Intent();
		intent.putExtra("serviceId", this.serviceId);
		intent.putExtra("isRunning", this.isRunning);
		intent.setAction("org.rootio.services.synchronization.EVENT");
		this.sendBroadcast(intent);
	}

	@Override
	public boolean isRunning() {
		return this.isRunning;
	}

	@Override
	public int getServiceId() {
		return this.serviceId;
	}

}
