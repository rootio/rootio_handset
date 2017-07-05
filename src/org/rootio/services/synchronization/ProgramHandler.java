package org.rootio.services.synchronization;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.radioClient.R;
import org.rootio.tools.media.Program;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.util.Log;

public class ProgramHandler {

	private Context parent;
	private SynchronizationUtils synchronizationUtils;
	private String JSON;

	ProgramHandler(Context parent, String JSON, SynchronizationUtils synchronizationUtils) {
		this.parent = parent;
		this.JSON = JSON;
		this.synchronizationUtils = synchronizationUtils;
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
			this.synchronizationUtils.logSynchronization(SynchronizationType.Program, program.getId(), 1, this.getLastUpdatedDate(programDefinition));
			} catch (Exception e) {
			Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null?"Null pointer[ProgramHandler.processJSONObject]":e.getMessage());
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
