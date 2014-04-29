package org.rootio.services.synchronization;

import java.util.Date;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.activities.DiagnosticStatistics;
import org.rootio.radioClient.R;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

public class DiagnosticsHandler {

	private Context parent;
	private SynchronizationUtils synchronizationUtils;
	private DiagnosticStatistics diagnosticStatistics;
	
	DiagnosticsHandler(Context parent, SynchronizationUtils synchronizationUtils)
	{
		this.parent = parent;
		this.synchronizationUtils = synchronizationUtils;
		Date sinceDate = this.synchronizationUtils.getLastUpdateDate(SynchronizationType.Diagnostic);
		this.diagnosticStatistics = new DiagnosticStatistics(this.parent, sinceDate);
	}
	
	public ContentValues getSynchronizationData(int index) {
		ContentValues data = new ContentValues();
		for (Entry<String, Object> entry : diagnosticStatistics.getRecord(index).entrySet()) {
			data.put(entry.getKey(), entry.getValue().toString());
		}
		return data;
	}
	
	public int getSize()
	{
		return diagnosticStatistics.getSize();
	}

	public void processDiagnosticSynchronizationResponse(String response, int index) {
		JSONObject object = this.parseJSON(response);
		if(object != null)
		{
		this.processJSONObject(object, index);
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
	private void processJSONObject(JSONObject synchronizationResponse, int index)
	{
	     try {
	    	 String response = synchronizationResponse.has("message")?synchronizationResponse.getString("message"):"";
			this.synchronizationUtils.logSynchronization(SynchronizationType.Diagnostic, (Integer)this.diagnosticStatistics.getRecord(index).get("ID"), response.equals("success")?1:0, (Date)this.diagnosticStatistics.getRecord(index).get("Record Date"));
			} catch (Exception e) {
			Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null?"Null pointer[ProgramHandler.processJSONObject]":e.getMessage());
		}
	}
	
	
	
	
}
