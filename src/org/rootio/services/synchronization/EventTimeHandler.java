package org.rootio.services.synchronization;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.radioClient.R;
import org.rootio.tools.media.Program;
import org.rootio.tools.radio.EventTime;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.util.Log;

public class EventTimeHandler {

	private Context parent;
	private String JSON;
	private SynchronizationUtils synchronizationUtils;
	
	
	EventTimeHandler(Context parent, String JSON, SynchronizationUtils synchronizationUtils)
	{
		this.parent = parent;
		this.JSON = JSON;
		this.synchronizationUtils = synchronizationUtils;
	}
	

	/**
	 * Processes the JSON that was specified in the creation of this object
	 */
	public void processEventTimes() {
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
	private void processJSONObject(JSONObject eventTimesDefinition) {
		try {
			JSONArray eventTimes = eventTimesDefinition.getJSONArray("objects");
			for (int i = 0; i < eventTimes.length(); i++) {
				EventTime eventTime = getEventTime(eventTimes.getJSONObject(i));
				this.synchronizationUtils.logSynchronization(SynchronizationType.EventTime, eventTime.getId(), 1, this.getLastUpdatedDate(eventTimes.getJSONObject(i)));
			} 
	} catch (Exception e) {
			Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null?"Null pointer[ProgramHandler.processJSONObject]":e.getMessage());
		}
	}

	/**
	 * Gets program information from the specified JSON input
	 * @param programSchedules JSON input containing program information
	 * @return Program object represented by the JSON input
	 */
	private EventTime getEventTime(JSONObject eventTimeObject)  {
		try
		{
		//int cloudId = eventTimeObject.getInt("id");
		long programId = eventTimeObject.getLong("program_id");
		Date scheduleDate = Utils.getDateFromString(eventTimeObject.getString("start"), "yyyy-MM-dd'T'HH:mm:ss");
		Date endDate = Utils.getDateFromString(eventTimeObject.getString("end"), "yyyy-MM-dd'T'HH:mm:ss");
		boolean isRepeat = eventTimeObject.has("is_repeat") ? eventTimeObject.getBoolean("is_repeat") : false;
		EventTime eventTime = new EventTime(this.parent, new Program(this.parent, programId).getId(), scheduleDate, this.getEventTimeDuration(scheduleDate, endDate), isRepeat);
		return eventTime;
		} catch (JSONException e) {
			Log.e(this.parent.getResources().getString(R.string.app_name),	e.getMessage());
			return null;
		}
	}
	
	private int getEventTimeDuration(Date startDate, Date endDate)
	{
		long diff = endDate.getTime() - startDate.getTime();
		int minutes = Math.round(diff/60000);
		return minutes;
	}
	
	private Date getLastUpdatedDate(JSONObject object)
	{
		
		try {
			String dateString;
			dateString = object.getString("updated_at");
			return Utils.getDateFromString(dateString, "yyyy-MM-dd'T'HH:mm:ss");
		} catch (JSONException e) {
			Log.e(this.parent.getString(R.string.app_name), e.getMessage());
			return null;
		}
		
	}
}
