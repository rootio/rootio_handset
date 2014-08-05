package org.rootio.services;

import java.util.ArrayList;

import org.rootio.tools.radio.ProgramSlot;
import org.rootio.tools.radio.RadioRunner;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ProgramService extends Service implements ServiceInformationPublisher {

	private final int serviceId = 4;
	private boolean isRunning;
	private Thread runnerThread;
	private RadioRunner radioRunner;

	@Override
	public IBinder onBind(Intent arg0) {
		BindingAgent bindingAgent = new BindingAgent(this);
		return bindingAgent;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (!this.isRunning) {
			Utils.doNotification(this, "RootIO", "Radio Service Started");
			radioRunner = new RadioRunner(this);
			runnerThread = new Thread(radioRunner);
			runnerThread.start();
			this.isRunning = true;

			this.sendEventBroadcast();
		}
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (radioRunner != null && this.isRunning) {
			super.onDestroy();
			radioRunner.stopProgram();
			this.isRunning = false;
			this.sendEventBroadcast();
			Utils.doNotification(this, "RootIO", "Radio Service Stopped");
		}
	}

	/**
	 * Sends out broadcasts informing listeners of change in the status of the
	 * service
	 */
	private void sendEventBroadcast() {
		Intent intent = new Intent();
		intent.putExtra("serviceId", this.serviceId);
		intent.putExtra("isRunning", this.isRunning);
		intent.setAction("org.rootio.services.program.EVENT");
		this.sendBroadcast(intent);
	}

	@Override
	public boolean isRunning() {
		return this.isRunning;
	}

	/**
	 * Gets the program slots that are defined for the current schedule
	 * 
	 * @return An ArrayList of ProgramSlot objects each representing a slot on
	 *         the schedule of the radio
	 */
	public ArrayList<ProgramSlot> getProgramSlots() {
		return radioRunner == null ? new ArrayList<ProgramSlot>() : radioRunner.getProgramSlots();
	}

	@Override
	public int getServiceId() {
		return this.serviceId;
	}

}
