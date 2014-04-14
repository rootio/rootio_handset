package org.rootio.services.synchronization;

import java.util.Date;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.radioClient.R;
import org.rootio.tools.media.Program;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.radio.EventTime;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

public class ProgramHandler {

	private Context parent;
	private HashSet<EventTime> eventTimes;
	private String JSON;

	ProgramHandler(Context parent, String JSON) {
		this.parent = parent;
		this.JSON = JSON;
	}
	
	/**
	 * Processes the JSON that was specified in the creation of this object
	 */
	public void processProgram() {
		JSONObject object = this.parseJSON(this.JSON);
		if(object != null)
		{
		this.processJSONObject(object);
		}
		
	}

	/**
	 * Constructs JSON object from string input
	 * @param input The string representing JSON input received from the cloud server
	 * @return JSON object constructed from the Specified input
	 */
	private JSONObject parseJSON(String input)
	{
		try
		{
			return new JSONObject(input);
		}
		catch(JSONException ex)
		{
			return null;
		}
		catch( NullPointerException ex)
		{
			return null;
		}
	}
	
	/**
	 * Breaks down the information in the JSON file for program and schedule information
	 * @param programDefinition The JSON program definition received from the cloud server
	 */
	private void processJSONObject(JSONObject programDefinition)
	{
	     try {
			Program program = getProgram(programDefinition);
			
			 int duration = this.getMinutesDurationFromTime(programDefinition.getString("duration"));
			 JSONArray programSchedules = programDefinition.getJSONArray("scheduled_programs");
			 this.createEventTimes(program.getId(), duration, programSchedules);
		} catch (JSONException e) {
			 Utils.toastOnScreen("invalid JSON!");
		}
	}

	/**
	 * Gets program information from the specified JSON input
	 * @param programSchedules JSON input containing program information
	 * @return Program object represented by the JSON input
	 */
	private Program getProgram(JSONObject programSchedules)  {
		try
		{
		int cloudId = programSchedules.getInt("id");
	     String title = programSchedules.getString("name");
	     int programTypeId = programSchedules.getInt("program_type_id");
	     //this.logSynchronization(SynchronizationType.Program, program.getId(), 1);
	     
	     return new Program(this.parent, cloudId, title, programTypeId);
		} catch (JSONException e) {
			Log.e(this.parent.getResources().getString(R.string.app_name),	e.getMessage());
			return null;
		}
	}
	
	/**
	 * Gets the duration of the program from the supplied String representing the duration of the program
	 * @param time String representing the duration of the program in the format HH:mm:ss
	 * @return Integer representing the duration in minutes of the program
	 */
	private int getMinutesDurationFromTime(String time) {
		try {
			Date date = Utils.getDateFromString(time, "HH:mm:ss");
			return date.getHours() * 60 + date.getMinutes(); // ignore seconds
		} catch (Exception ex) {
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null?"Null pointer":ex.getMessage());
			return 0;
		}
	}

	/**
	 * Gets event times from the supplied duration
	 * @param programId The ID of the program with which these event times are associated
	 * @param duration The duration of these event times
	 * @param scheduledPrograms The JSON containing event time information
	 */
	private void createEventTimes(long programId,int duration, JSONArray scheduledPrograms) {
		this.eventTimes = new HashSet<EventTime>();
		for (int i = 0; i < scheduledPrograms.length(); i++) {
			try {
				JSONObject scheduledProgram = scheduledPrograms.getJSONObject(i);
				Date scheduleDate = Utils.getDateFromString(scheduledProgram.getString("start"), "yyyy-MM-dd'T'HH:mm:ss");
				boolean isRepeat = scheduledProgram.has("is_repeat") ? scheduledProgram.getBoolean("is_repeat") : false;
				EventTime eventTime = new EventTime(this.parent, programId, scheduleDate, duration, isRepeat);
				this.eventTimes.add(eventTime);
				this.logSynchronization(SynchronizationType.EventTime, eventTime.getId(), 1);
			} catch (JSONException e) {
				Log.e(this.parent.getResources().getString(R.string.app_name),e.getMessage());
			}
		}
	}

	/**
	 * Logs the result of this synchronization session
	 * @param synchronizationType The type of content that was being synchronizes
	 * @param id The ID of the object created by this synchronization
	 * @param status Integer representing the status of the synchronization. 
	 */
	private void logSynchronization(SynchronizationType synchronizationType, long id, int status) {
		String tableName = "downloadBacklog";
		ContentValues data = new ContentValues();
		data.put("changetypeid", synchronizationType.ordinal());
		data.put("changeid", id);
		data.put("downloadstatusid", status);
		DBAgent dbAgent = new DBAgent(this.parent);
		dbAgent.saveData(tableName, null, data);
	}

	
}
