package org.rootio.tools.media;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.tools.radio.ScheduleBroadcastHandler;
import org.rootio.tools.utils.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class Program  implements Comparable<Program>, ScheduleNotifiable{
	
	private String title;
	private Long id;
	private Date startDate, endDate;
	private int duration,playingIndex;
	private String structure;
	final Context parent;
	private ArrayList<ProgramAction> programActions;
	private final ScheduleBroadcastHandler alertHandler;

	
	public Program(Context parent, String title, Date start, Date end, String structure)  {
		this.parent = parent;
		this.title = title;
		this.startDate = start;
		this.endDate = end;
		this.structure = structure;
		this.alertHandler = new ScheduleBroadcastHandler(this);
		this.loadProgramActions(structure);
	}
	
	public void runProgramAction(int index)
	{
		this.programActions.get(this.playingIndex).stop();
		this.playingIndex = index;
		this.programActions.get(index).run();
	}
	
	public void stop()
	{
		this.programActions.get(this.playingIndex).stop();
		//unregister listeners, finalize()
	}
	
	public void pause()
	{
		
	}
	
	public void resume()
	{
		
	}
	
	private void loadProgramActions(String structure)
	{
		this.programActions = new ArrayList<ProgramAction>();
		JSONObject programStructure;
		Utils.toastOnScreen("str is "+structure, parent);
		try {
			programStructure = new JSONObject(structure);
		
	    for(int i = 0; i < programStructure.names().length(); i++)
	    {
	    	if(((String)programStructure.names().get(i)).toLowerCase().equals("outcall"))//no implementation yet, will default to music with argument "random"
	    	{
	    		this.programActions.add(new ProgramAction(this.parent, programStructure.getJSONObject((String)programStructure.names().get(i)),ProgramActionType.Outcall));
	    	}
	    	else if(((String)programStructure.names().get(i)).toLowerCase().equals("music"))
	    	{
	    		JSONObject music = (JSONObject) programStructure.getJSONArray((String) programStructure.names().get(i)).get(0);
	    		this.programActions.add(new ProgramAction(this.parent, music,ProgramActionType.Music));
	    	}
	    	else if(((String)programStructure.names().get(i)).toLowerCase().equals("media"))
	    	{
	    		this.programActions.add(new ProgramAction(this.parent, programStructure.getJSONObject((String)programStructure.names().get(i)),ProgramActionType.Media));
	    	}
	    	else if(((String)programStructure.names().get(i)).toLowerCase().equals("jingle"))
	    	{
	    		this.programActions.add(new ProgramAction(this.parent, programStructure.getJSONObject((String)programStructure.names().get(i)),ProgramActionType.Jingle));
	    	}
	    	else if(((String)programStructure.names().get(i)).toLowerCase().equals("interlude"))//no implementation yet, will default to music with argument "random"
	    	{
	    		this.programActions.add(new ProgramAction(this.parent, programStructure.getJSONObject((String)programStructure.names().get(i)),ProgramActionType.Interlude));
	    	}
	    	else if(((String)programStructure.names().get(i)).toLowerCase().equals("stream"))//no implementation yet, will default to music with argument "random"
	    	{
	    		this.programActions.add(new ProgramAction(this.parent, programStructure.getJSONObject((String)programStructure.names().get(i)),ProgramActionType.Stream));
	    	}
	    }
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

	public ArrayList<ProgramAction> getProgramActions()
	{
		return this.programActions;
	}
	
	public Date getStartDate()
	{
		return this.startDate;
	}
	
	public Date getEndDate()
	{
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
			intentFilter.addAction(this.title + String.valueOf(i));
		}
		this.parent.registerReceiver(alertHandler, intentFilter);
		for (int i = 0; i < programActions.size(); i++) {
			// problem is here
			Intent intent = new Intent(this.title + String.valueOf(i));
			intent.putExtra("index", i);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this.parent, 0, intent, 0);
			am.set(0, this.getStartTime(programActions.get(i).getStartTime()), pendingIntent);
		}
	}
	
	private long getStartTime(Date programActionDate) {
		Date baseDate = this.startDate;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(baseDate);
		calendar.add(Calendar.HOUR_OF_DAY, programActionDate.getHours());
		calendar.add(Calendar.MINUTE, programActionDate.getMinutes());
		calendar.add(Calendar.SECOND, programActionDate.getSeconds());
		return calendar.getTimeInMillis();
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
	public
	boolean isExpired(int index) {
		Calendar referenceCalendar = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.programActions.get(index).getStartTime());
		cal.add(Calendar.MINUTE, this.programActions.get(index).getDuration() - 1);
		return cal.compareTo(referenceCalendar) <= 0;
	}
	
}
