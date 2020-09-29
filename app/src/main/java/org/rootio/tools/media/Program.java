package org.rootio.tools.media;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.radio.ScheduleBroadcastHandler;
import org.rootio.tools.utils.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class Program extends BroadcastReceiver implements Comparable<Program>, ScheduleNotifiable {

    private String title;
    private Date startDate, endDate;
    private int playingIndex;
    final Context parent;
    private ArrayList<ProgramAction> programActions;
    private boolean isLocal;
    private ScheduleBroadcastHandler alertHandler;

    public Program(Context parent, String title, Date start, Date end, String structure) {
        this.parent = parent;
        this.title = title;
        this.startDate = start;
        this.endDate = end;
        this.alertHandler = new ScheduleBroadcastHandler(this);
        this.loadProgramActions(structure);
    }

    public void stop() {
        try {
            this.programActions.get(this.playingIndex).stop();
            this.parent.unregisterReceiver(this.alertHandler);
        } catch (Exception ex) {

        }
    }

    public void pause() {
        this.programActions.get(this.playingIndex).pause();
    }

    public void resume() {
        this.programActions.get(this.playingIndex).resume();
    }

    private void loadProgramActions(String structure) {
        this.programActions = new ArrayList<>();
        JSONArray programStructure;
        try {
            programStructure = new JSONArray(structure);
            ArrayList<String> playlists = new ArrayList<String>();
            ArrayList<String> streams = new ArrayList<String>();
            int duration =0;
            for (int i = 0; i < programStructure.length(); i++) {
                if (programStructure.getJSONObject(i).getString("type").toLowerCase().equals("music"))//redundant, safe
                {
                    //accumulate playlists
                    playlists.add(programStructure.getJSONObject(i).getString("name"));
                    this.isLocal = true;
                }
                if (programStructure.getJSONObject(i).getString("type").toLowerCase().equals("stream"))//redundant, safe
                {
                    //accumulate playlists
                    streams.add(programStructure.getJSONObject(i).getString("stream_url"));
                    this.isLocal = true;
                }
                if(programStructure.getJSONObject(i).has("duration")) { //redundant, using optInt
                    duration = programStructure.getJSONObject(i).optInt("duration");
                }
            }

            this.programActions.add(new ProgramAction(this.parent, playlists, streams, ProgramActionType.Audio, duration));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Returns the title of this program
     *
     * @return String representation of the title of this program
     */
    public String getTitle() {
        return this.title;
    }


    public void run() {
        this.runProgram(0);
        if(isStrictProgramming()) {
            this.setupProgramStop(getEndDate());
        }
    }

    private boolean isStrictProgramming()
    {
        String stationInformation = (String) Utils.getPreference("station_information", String.class, this.parent);
        JSONObject stationJson;
        try {
            stationJson = new JSONObject(stationInformation).optJSONObject("station");
            return stationJson.optBoolean("strict_scheduling", false);
        } catch (JSONException ex) {
            Log.d(parent.getString(R.string.app_name), String.format("[Program.isStrictProgramming] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
            return false;
        }
    }

    public ArrayList<ProgramAction> getProgramActions() {
        return this.programActions;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public boolean isLocal()
    {
        return this.isLocal;
    }

    @Override
    public int compareTo(Program another) {
        return this.startDate.compareTo(another.getStartDate());
    }

    private void setupProgramStop(Date endDate) {
        AlarmManager am = (AlarmManager) this.parent.getSystemService(Context.ALARM_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("org.rootio.RadioRunner." + this.title + endDate.getTime()); //unique
        this.parent.registerReceiver(this, intentFilter);

        Intent intent = new Intent("org.rootio.RadioRunner." + this.title + endDate.getTime());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.parent, 0, intent, 0);
        am.setExact(AlarmManager.RTC_WAKEUP, endDate.getTime(), pendingIntent);
    }


    @Override
    public void runProgram(int currentIndex) {
        this.programActions.get(currentIndex).run();
    }

    @Override
    public void stopProgram(Integer index) {
        this.programActions.get(index).stop();

    }

    @Override
    public boolean isExpired(int index) {
        Calendar referenceCalendar = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, this.programActions.get(index).getDuration() - 1); //fetch the duration from the DB for each program action
        return this.endDate.compareTo(referenceCalendar.getTime()) <= 0;
    }

    @Override
    public void finalize()
    {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.stop();
    }
}
