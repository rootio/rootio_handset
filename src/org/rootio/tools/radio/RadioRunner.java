package org.rootio.tools.radio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.rootio.tools.media.Program;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

@SuppressLint("SimpleDateFormat")
public class RadioRunner implements Runnable {

	private AlarmManager am;
	private BroadcastHandler br;
	private PendingIntent pi;
	private Context parent;
	private ArrayList<ProgramSlot> programSlots;
	private RadioRunnerExitIntentListener broadcastReceiver;
	private Integer runningProgramIndex = null;

	public RadioRunner(Context parent) {
		this.parent = parent;
		this.setUpAlarming();
		this.programSlots = new ArrayList<ProgramSlot>();
		this.broadcastReceiver = new RadioRunnerExitIntentListener();
		this.parent.registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
	}

	private void setUpAlarming() {
		am = (AlarmManager) this.parent.getSystemService(Context.ALARM_SERVICE);
		br = new BroadcastHandler(this);
	}

	@Override
	public void run() {
		ArrayList<Program> programs = fetchPrograms();
		this.addPadding();
		this.schedulePrograms(programs);
	}

	public void stop() {

	}

	public void runProgram(int index) {
		if (this.runningProgramIndex != null) {
			this.programSlots.get(this.runningProgramIndex).getProgram().stop();
			this.programSlots.get(runningProgramIndex).setFinishedRunning();
		}
		this.runningProgramIndex = index;
		this.programSlots.get(this.runningProgramIndex).setRunning();
		this.programSlots.get(this.runningProgramIndex).getProgram().run();
	}

	public void pauseProgram() {
		if (this.runningProgramIndex != null) {
			this.programSlots.get(this.runningProgramIndex).getProgram().pause();
		}
	}

	public void resumeProgram() {
		if (this.runningProgramIndex != null) {
			this.programSlots.get(this.runningProgramIndex).getProgram().resume();
		}
	}

	public void stopProgram() {
		if (this.runningProgramIndex != null) {
			this.programSlots.get(this.runningProgramIndex).getProgram().stop();
		}
	}

	public ArrayList<ProgramSlot> getProgramSlots() {
		return this.programSlots;
	}
	
	public Program getRunningProgram()
	{
		return this.programSlots.get(this.runningProgramIndex).getProgram();
	}

	private void schedulePrograms(ArrayList<Program> programs) {
		for (int i = 0; i < programs.size(); i++) {
			EventTime[] eventTimes = programs.get(i).getEventTimes();
			for (int j = 0; j < eventTimes.length; j++) {
					programs.get(i).setScheduledIndex(j);
					this.programSlots.add(new ProgramSlot(programs.get(i), j));
				}
		}
		Collections.sort(this.programSlots);
		for(int i = 0 ; i < this.programSlots.size(); i++)
		{
			addAlarmEvent(i, this.programSlots.get(i).getProgram().getEventTimes()[this.programSlots.get(i).getScheduledIndex()].getScheduledDate());
			//System.out.printf("%s -> %s\n",i, this.programSlots.get(i).getProgram().getEventTimes()[this.programSlots.get(i).getScheduledIndex()].getScheduledDate());
		}
	}

	private void addAlarmEvent(int index, Date startTime) { 
		try {
			pi = PendingIntent.getBroadcast(parent, 0, new Intent(String.valueOf(index)), 0);
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(String.valueOf(index));
			this.parent.registerReceiver(new BroadcastHandler(this), intentFilter);
			this.am.set(AlarmManager.RTC_WAKEUP, startTime.getTime(), this.pi);
			} catch (Exception e) {
			//log this
		}
	}

	private void addPadding() {

	}

	private ArrayList<Program> fetchPrograms() {
		DBAgent agent = new DBAgent(this.parent);
		String query = "select program.id, program.title, program.cloudid, programtypeid , tag from program where program.id in (select programid from eventtime where date(scheduledate) = date())";
		String[] args = new String[] {};
		String[][] data = agent.getData(query, args);
		ArrayList<Program> programs = new ArrayList<Program>();
		for (int i = 0; i < data.length; i++) {
				Program program;
			    program = new Program(this.parent, Utils.parseLongFromString(data[i][2]), data[i][1], Utils.parseIntFromString(data[i][3]), data[i][4]);
				programs.add(program);
		}
		return programs;
	}

	@Override
	public void finalize() {
		
		try {
			this.parent.unregisterReceiver(this.broadcastReceiver);
			this.parent.unregisterReceiver(this.br);
			super.finalize();
		} catch (Throwable e) {

		}
	}

	class RadioRunnerExitIntentListener extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			stopProgram();
		}

	}
}
