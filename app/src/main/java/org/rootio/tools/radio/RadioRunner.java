package org.rootio.tools.radio;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.rootio.activities.services.TelephonyEventNotifiable;
import org.rootio.handset.R;
import org.rootio.tools.media.Program;
import org.rootio.tools.media.ScheduleChangeNotifiable;
import org.rootio.tools.media.ScheduleNotifiable;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.esotericsoftware.kryo.util.Util;

enum State {
    PLAYING, PAUSED, STOPPED
}

@SuppressLint("SimpleDateFormat")
public class RadioRunner implements Runnable, TelephonyEventNotifiable, ScheduleNotifiable, ScheduleChangeNotifiable {
    private AlarmManager am;
    private ScheduleBroadcastHandler br;
    private ArrayList<PendingIntent> pis;
    private final Context parent;
    private ArrayList<Program> programs;
    private IntentFilter intentFilter;
    private Integer runningProgramIndex = null;
    private State state;
    private TelephonyEventBroadcastReceiver telephonyEventBroadcastReceiver;
    private ScheduleChangeBroadcastHandler scheduleChangeNotificationReceiver;

    public RadioRunner(Context parent) {
        this.parent = parent;
        this.setUpAlarming();
        this.listenForTelephonyEvents();
        this.listenForScheduleChangeNotifications();
    }

    private void listenForScheduleChangeNotifications() {
        this.scheduleChangeNotificationReceiver = new ScheduleChangeBroadcastHandler(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("org.rootio.services.synchronization.SCHEDULE_CHANGE_EVENT");
        this.parent.registerReceiver(scheduleChangeNotificationReceiver, intentFilter);
    }

    private void listenForTelephonyEvents()  {
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
        this.br = new ScheduleBroadcastHandler(this);
    }

    @Override
    public void run() {
        initialiseSchedule();
    }

    private void initialiseSchedule() {
        this.programs = fetchPrograms();
        this.schedulePrograms(programs);
    }

    /**
     * Runs the program whose index is specified from the programs lined up
     *
     * @param index The index of the program to run
     */
    public synchronized void runProgram(int index) {
        if (this.runningProgramIndex != null && !this.isExpired(this.runningProgramIndex)) {
            this.stopProgram(this.runningProgramIndex);
        }
        this.runningProgramIndex = index;
        // Check to see that we are not in a phone call before launching program
        if (this.state != State.PAUSED) {
            this.state = State.PLAYING;
            this.programs.get(index).run();

        }
    }

    /**
     * Pauses the running program
     */
    private void pauseProgram() {
        if (this.runningProgramIndex != null) {
            this.programs.get(this.runningProgramIndex).pause();
        }
    }

    /**
     * Resumes the program that is currently playing if it was paused before
     */
    private void resumeProgram() {
        if (this.runningProgramIndex != null) {
            this.programs.get(this.runningProgramIndex).resume();
        }
    }

    /**
     * Stops the program that is currently running
     */
    public void stopProgram(Integer index) {
         if (index != null) {
            try {
                this.programs.get(this.runningProgramIndex).stop();
            } catch (NullPointerException e) {
                Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null ? "Null pointer exception(RadioRunner.stopProgram)" : e.getMessage());
            }
        }
        if (this.state != State.PAUSED) {
            this.state = State.STOPPED;
        }
    }

    public void stop() {
        this.stopProgram(this.runningProgramIndex);
        this.finalize();

    }

    public void finalize() {
        try {
            unregisterReceivers();
            super.finalize();
        } catch (Throwable e) {
            Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null ? "Null pointer exception(RadioRunner.finalize)" : e.getMessage());
        }
    }

    private void unregisterReceivers() {
        this.parent.unregisterReceiver(telephonyEventBroadcastReceiver);
        this.parent.unregisterReceiver(br);
        this.parent.unregisterReceiver(scheduleChangeNotificationReceiver);
    }

    /**
     * Returns all the program slots scheduled
     *
     * @return ArrayList of ProgramSlot objects each representing a scheduled
     * program
     */
    public ArrayList<Program> getPrograms() {
        return this.programs;
    }

    /**
     * Gets the running program
     *
     * @return The currently running program
     */
    public Program getRunningProgram() {
        return this.programs.get(this.runningProgramIndex);
    }

    /**
     * Schedules the supplied programs according to their schedule information
     *
     * @param programs ArrayList of the programs to be scheduled
     */
    private void schedulePrograms(ArrayList<Program> programs) {
        IntentFilter intentFilter = new IntentFilter();
        this.pis = new ArrayList<>();
        for (int i = 0; i < programs.size(); i++) {
            intentFilter.addAction("org.rootio.RadioRunner" + String.valueOf(i));
        }
        this.parent.registerReceiver(br, intentFilter);

        // Sort the program slots by time at which they will play
        Collections.sort(programs);

        // Schedule the program slots
        for (int i = 0; i < programs.size(); i++) {
            if(programs.get(i).isLocal()) { // no point scheduling non local progs
                if(i == 0 || (i > 0 && programs.get(i).getStartDate() != programs.get(i-1).getStartDate())) //do not double schedule at same time.
                addAlarmEvent(i, programs.get(i).getStartDate());
            }
        }
    }

    /**
     * Adds the element at the supplied index to the Alarm as per the supplied
     * time
     *
     * @param index     The index of the event to be added to the Alarm Manager
     * @param startTime The time at which the event is supposed to start
     */
    private void addAlarmEvent(int index, Date startTime) {
        try {
            Intent intent = new Intent("org.rootio.RadioRunner" + String.valueOf(index));
            intent.putExtra("index", index);
            PendingIntent pi = PendingIntent.getBroadcast(parent, 0, intent, 0);
            this.am.set(AlarmManager.RTC_WAKEUP, startTime.getTime(), pi);
            this.pis.add(pi);
        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(RadioRunner.addAlarmEvent)" : ex.getMessage());
        }
    }

    /**
     * This clears all scheduled events
     */
    private void resetSchedule() {
        for (PendingIntent pi : this.pis) {
            this.am.cancel(pi);
        }

        //this.runningProgramIndex = null;
        this.pis = new ArrayList<>();
    }

    /**
     * Fetches program information as stored in the database
     *
     * @return ArrayList of Program objects each representing a database record
     */
    private ArrayList<Program> fetchPrograms() {
        DBAgent agent = new DBAgent(this.parent);
        String query = "select id, name, start, end, structure, programtypeid from scheduledprogram where date(start) = date(current_timestamp,'localtime')";
        String[] args = new String[]{};
        String[][] data = agent.getData(query, args);
        ArrayList<Program> programs = new ArrayList<>();
        for(String[] row : data) {
            Program program;
            program = new Program(this.parent, row[1], Utils.getDateFromString(row[2], "yyyy-MM-dd HH:mm:ss"), Utils.getDateFromString(row[3], "yyyy-MM-dd HH:mm:ss"), row[4]);
            programs.add(program);
        }
        return programs;
    }

    @Override
    public void notifyTelephonyStatus(boolean isInCall) {
        if (isInCall) {
            //it is important to set and check state ASAP. These events may be fired more than once in quick succession
            if(this.state != State.PAUSED) {
                this.pauseProgram();
                this.state = State.PAUSED;
            }
        } else { // notification that the call has ended
            if (this.state == State.PAUSED) {
                // The program had begun, it was paused by the call
                this.state = State.PLAYING;
                this.resumeProgram();

            }
        }
    }

    @Override
    public boolean isExpired(int index) {
        Calendar referenceCalendar = Calendar.getInstance();
        boolean isExpired = this.programs.get(index).getEndDate().compareTo(referenceCalendar.getTime()) <= 0;
        return this.programs.get(index).getEndDate().compareTo(referenceCalendar.getTime()) <= 0;
    }

    @Override
    public void notifyScheduleChange() {
        this.resetSchedule();
        this.initialiseSchedule();
    }
}
