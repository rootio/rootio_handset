package org.rootio.tools.radio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.rootio.activities.services.TelephonyEventNotifiable;
import org.rootio.tools.media.Program;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

enum State {
	PLAYING, PAUSED, STOPPED
};

@SuppressLint("SimpleDateFormat")
public class RadioRunner implements Runnable, TelephonyEventNotifiable {

	private AlarmManager am;
	private BroadcastHandler br;
	private PendingIntent pi;
	private final Context parent;
	private final ArrayList<ProgramSlot> programSlots;
	private IntentFilter intentFilter;
	private Integer runningProgramIndex = null;
	private State state;
	private final TelephonyEventBroadcastReceiver telephonyEventBroadcastReceiver;

	public RadioRunner(Context parent) {
		this.parent = parent;
		this.setUpAlarming();
		this.programSlots = new ArrayList<ProgramSlot>();

		this.telephonyEventBroadcastReceiver = new TelephonyEventBroadcastReceiver(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("org.rootio.services.telephony.TELEPHONY_EVENT");
		this.parent.registerReceiver(telephonyEventBroadcastReceiver, intentFilter);
	}

	/**
	 * Sets up the alarming to handle the timing of the broadcasts
	 */
	private void setUpAlarming() {
		this.am = (AlarmManager) this.parent.getSystemService(Context.ALARM_SERVICE);
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction("org.rootio.RadioRunner");
		this.br = new BroadcastHandler(this);
		this.parent.registerReceiver(this.br, this.intentFilter);
	}

	@Override
	public void run() {
		ArrayList<Program> programs = fetchPrograms();
		this.addPadding();
		this.schedulePrograms(programs);
	}

	public void stop() {

	}

	/**
	 * Runs the program whose index is specified from the programs lined up
	 * 
	 * @param index
	 *            The index of the program to run
	 */
	public void runProgram(int index) {
		if (this.runningProgramIndex != null) {
			this.programSlots.get(this.runningProgramIndex).getProgram().getProgramManager().stop();
			this.programSlots.get(runningProgramIndex).setFinishedRunning();
		}
		this.runningProgramIndex = index;
		this.programSlots.get(this.runningProgramIndex);
		Utils.toastOnScreen("the state is " + this.state);

		// Check to see that we are not in a phone call before launching program
		if (this.state != State.PAUSED) {
			this.programSlots.get(this.runningProgramIndex).setRunning();
			this.programSlots.get(this.runningProgramIndex).getProgram().getProgramManager().runProgram();
			this.state = State.PLAYING;
		}
	}

	/**
	 * Pauses the running program
	 */
	public void pauseProgram() {
		if (this.runningProgramIndex != null) {
			this.programSlots.get(this.runningProgramIndex).getProgram().getProgramManager().pause();
		}
	}

	/**
	 * Resumes the program that is currently playing if it was paused before
	 */
	public void resumeProgram() {
		if (this.runningProgramIndex != null) {
			this.programSlots.get(this.runningProgramIndex).getProgram().getProgramManager().play();
		}
	}

	/**
	 * Stops the program that is currently running
	 */
	public void stopProgram() {
		if (this.runningProgramIndex != null) {
			this.programSlots.get(this.runningProgramIndex).getProgram().getProgramManager().stop();
			if (this.state != State.PAUSED) {
				this.state = State.STOPPED;
			}
		}
	}

	/**
	 * Returns all the program slots scheduled
	 * 
	 * @return ArrayList of ProgramSlot objects each representing a scheduled
	 *         program
	 */
	public ArrayList<ProgramSlot> getProgramSlots() {
		return this.programSlots;
	}

	/**
	 * Gets the running program
	 * 
	 * @return The curently running program
	 */
	public Program getRunningProgram() {
		return this.programSlots.get(this.runningProgramIndex).getProgram();
	}

	/**
	 * Schedules the supplied programs according to their schedule information
	 * 
	 * @param programs
	 *            ArrayList of the programs to be scheduled
	 */
	private void schedulePrograms(ArrayList<Program> programs) {
		for (int i = 0; i < programs.size(); i++) {
			EventTime[] eventTimes = programs.get(i).getEventTimes();
			for (int j = 0; j < eventTimes.length; j++) {
				programs.get(i).setScheduledIndex(j);
				this.programSlots.add(new ProgramSlot(programs.get(i), j));
			}
		}
		Collections.sort(this.programSlots);
		for (int i = 0; i < this.programSlots.size(); i++) {
			addAlarmEvent(i, this.programSlots.get(i).getProgram().getEventTimes()[this.programSlots.get(i).getScheduledIndex()].getScheduledDate());
		}
	}

	/**
	 * Adds the element at the supplied index to the Alarm as per the supplied
	 * time
	 * 
	 * @param index
	 *            The index of the event to be added to the Alarm Manager
	 * @param startTime
	 *            The time at which the event is supposed to start
	 */
	private void addAlarmEvent(int index, Date startTime) {
		try {
			Intent intent = new Intent("org.rootio.RadioRunner");
			intent.putExtra("index", index);
			this.pi = PendingIntent.getBroadcast(parent, 0, intent, 0);
			this.am.set(AlarmManager.RTC_WAKEUP, startTime.getTime(), this.pi);
		} catch (Exception e) {
			// log this
		}
	}

	private void addPadding() {

	}

	/**
	 * Fetches program information as stored in the database
	 * 
	 * @return ArrayList of Program objects each representing a database record
	 */
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
	public void notifyTelephonyStatus(boolean isInCall) {
		if (isInCall) {
			this.pauseProgram();
			this.state = State.PAUSED;
		} else { // notification that the call has ended
			if (this.state == State.PAUSED) {
				int state = this.programSlots.get(this.runningProgramIndex).getRunState();
				if (state == 0) {// The program had not begun, was waiting for
									// the call to end in order to begin
					this.programSlots.get(this.runningProgramIndex).setRunning();
					this.programSlots.get(this.runningProgramIndex).getProgram().getProgramManager().runProgram();
				} else { // The program had begun, it was paused by the call
					this.resumeProgram();
					this.state = State.PLAYING;
				}
			}
		}

	}
}
