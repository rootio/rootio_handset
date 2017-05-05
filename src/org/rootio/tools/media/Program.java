package org.rootio.tools.media;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.rootio.tools.radio.ScheduleBroadcastHandler;
import org.rootio.tools.utils.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class Program implements Comparable<Program>, ScheduleNotifiable {

	private String title;
	private Long id;
	private Date startDate, endDate;
	private int duration, playingIndex;
	final Context parent;
	private ArrayList<ProgramAction> programActions;
	private final ScheduleBroadcastHandler alertHandler;

	public Program(Context parent, String title, Date start, Date end, String structure, String programTypeId) {
		this.parent = parent;
		this.title = title;
		this.startDate = start;
		this.endDate = end;
		this.alertHandler = new ScheduleBroadcastHandler(this);
		this.loadProgramActions(structure, programTypeId);
	}

	public void runProgramAction(int index) {
		this.programActions.get(this.playingIndex).stop();
		this.playingIndex = index;
		this.programActions.get(index).run();
	}

	public void stop() {
		try
		{
		this.programActions.get(this.playingIndex).stop();
		// unregister listeners, finalize()
		}
		catch(Exception ex)
		{
			
		}
	}

	public void pause() {

	}

	public void resume() {

	}

	private void loadProgramActions(String structure, String programTypeId) {
		this.programActions = new ArrayList<ProgramAction>();
		JSONArray programStructure;
		try {
			programStructure = new JSONArray(structure);
           // if (programTypeId == "2") {
				String[] playlists = new String[programStructure.length()];
				for (int i = 0; i < programStructure.length(); i++) {
					 if (programStructure.getJSONObject(i).getString("type").toLowerCase().equals("music"))//redundant, safe
					 {
						 //acumulate playlists
						playlists[i] = programStructure.getJSONObject(i).getString("name");
					} 
				}
				this.programActions.add(new ProgramAction(this.parent, playlists, ProgramActionType.Music));
			//}				
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

	/**
	 * Gets the ID for this program
	 * 
	 * @return long representation of the ID of this program
	 */
	public long getId() {
		return this.id;
	}

	public void run() {
		this.setupAlertReceiver(alertHandler, programActions);
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

	@Override
	public int compareTo(Program another) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getDuration() {
		return this.duration;
	}

	private void setupAlertReceiver(ScheduleBroadcastHandler alertHandler, ArrayList<ProgramAction> programActions) {
		IntentFilter intentFilter = new IntentFilter();
		AlarmManager am = (AlarmManager) this.parent.getSystemService(Context.ALARM_SERVICE);
		for (int i = 0; i < programActions.size(); i++) {
			intentFilter.addAction("org.rootio.RadioRunner."+this.title + String.valueOf(i));
		}
		this.parent.registerReceiver(alertHandler, intentFilter);
		for (int i = 0; i < programActions.size(); i++) {
			Intent intent = new Intent("org.rootio.RadioRunner."+this.title + String.valueOf(i));
			intent.putExtra("index", i);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this.parent, 0, intent, 0);
			am.set(0, this.startDate.getTime(), pendingIntent);
		}
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
		cal.add(Calendar.MINUTE, this.programActions.get(index).getDuration() - 1);
		return this.endDate.compareTo(referenceCalendar.getTime()) <= 0;
	}

}
