package org.rootio.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.rootio.radioClient.R;
import org.rootio.tools.radio.ProgramSlot;
import org.rootio.tools.radio.RadioRunner;
import org.rootio.tools.utils.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class ProgramService extends Service implements ServiceInformationPublisher {

	private final int serviceId = 4;
	private boolean isRunning;
	private Thread runnerThread;
	private RadioRunner radioRunner;
	private NewDayScheduleHandler newDayScheduleHandler;
	private PendingIntent pi;
	private AlarmManager am;

	@Override
	public IBinder onBind(Intent arg0) {
		BindingAgent bindingAgent = new BindingAgent(this);
		return bindingAgent;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (!this.isRunning) {
			Utils.doNotification(this, "RootIO", "Radio Service Started");
			this.am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			runTodaySchedule();
			this.setupNewDayScheduleListener();
			this.isRunning = true;
			this.sendEventBroadcast();
		}
		return Service.START_STICKY;
	}

	private void setupNewDayScheduleListener() {
		this.newDayScheduleHandler = new NewDayScheduleHandler();
		IntentFilter intentFilter = new IntentFilter("org.rootio.services.program.NEW_DAY_SCHEDULE");
		this.registerReceiver(newDayScheduleHandler, intentFilter);
	}

	private void runTodaySchedule() {
		radioRunner = new RadioRunner(this);
		runnerThread = new Thread(radioRunner);
		runnerThread.start();
		this.scheduleNextDayAlarm();
	}

	@Override
	public void onDestroy() {
		if (radioRunner != null && this.isRunning) {
			super.onDestroy();
			radioRunner.stopProgram();
			this.isRunning = false;
			try {
				this.unregisterReceiver(newDayScheduleHandler);
			} catch (Exception ex) {
				Log.e(this.getString(R.string.app_name), String.format("[ProgramService.onDestroy] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
			}
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

	private void scheduleNextDayAlarm() {
		Date dt = this.getTomorrowBaseDate();
		Intent intent = new Intent("org.rootio.services.program.NEW_DAY_SCHEDULE");

		this.pi = PendingIntent.getBroadcast(this, 0, intent, 0);
		this.am.set(AlarmManager.RTC_WAKEUP, dt.getTime(), this.pi);
	}

	private Date getTomorrowBaseDate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	class NewDayScheduleHandler extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ProgramService.this.radioRunner.stopProgram();
			// ProgramService.this.finalize()
			ProgramService.this.runTodaySchedule();

		}
	}

}
