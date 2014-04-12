package org.rootio.services;

import java.util.ArrayList;

import org.rootio.activities.services.TelephonyEventNotifiable;
import org.rootio.tools.radio.ProgramSlot;
import org.rootio.tools.radio.RadioRunner;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class ProgramService extends Service implements TelephonyEventNotifiable, RunningStatusPublished {

	private int serviceId = 4;
	private boolean isRunning;
	private Thread runnerThread;
	private RadioRunner radioRunner;
	private TelephonyEventBroadcastReceiver telephonyEventBroadcastReceiver;

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
			this.telephonyEventBroadcastReceiver = new TelephonyEventBroadcastReceiver(this);
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction("org.rootio.services.telephony.TELEPHONY_EVENT");
			this.registerReceiver(telephonyEventBroadcastReceiver, intentFilter);
			this.sendEventBroadcast();
		}
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		Utils.doNotification(this, "RootIO", "Radio Service Stopped");
		if (radioRunner != null) {
			super.onDestroy();
			radioRunner.stopProgram();
			this.isRunning = false;
			this.sendEventBroadcast();
		}
	}

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

	public ArrayList<ProgramSlot> getProgramSlots() {
		return radioRunner == null ? new ArrayList<ProgramSlot>() : radioRunner.getProgramSlots();
	}

	@Override
	public void notifyTelephonyStatus(boolean isInCall) {
		if(isInCall)
		{
			this.radioRunner.pauseProgram();
		}
		else
		{
			this.radioRunner.resumeProgram();
		}
		
	}

}
