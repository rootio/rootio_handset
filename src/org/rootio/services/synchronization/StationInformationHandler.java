package org.rootio.services.synchronization;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.activities.diagnostics.DiagnosticsConfiguration;
import org.rootio.activities.synchronization.SynchronizationConfiguration;
import org.rootio.radioClient.R;
import org.rootio.tools.radio.Station;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.util.Log;

public class StationInformationHandler {

	private Context parent;
	private String JSON;
	private SynchronizationUtils synchronizationUtils;
	private Station station;
	private SynchronizationConfiguration synchronizationConfiguration;
	private DiagnosticsConfiguration diagnosticsConfiguration;
	
	StationInformationHandler(Context parent, String JSON, SynchronizationUtils synchronizationUtils)
	{
		this.parent = parent;
		this.JSON = JSON;
		this.synchronizationUtils = synchronizationUtils;
		this.synchronizationConfiguration = new SynchronizationConfiguration(this.parent);
		this.station = new Station(this.parent);
	}
	
	public void processStationInformation() {
		JSONObject object = this.parseJSON(this.JSON);
		if (object != null) {
			this.processJSONObject(object);
		}
		
	}

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
	
	private void processJSONObject(JSONObject stationInformation) {
		try {
			this.processStationInformation(stationInformation);
			this.processDiagnosticsFrequencyInformation(stationInformation);
			this.processSynchronizationFrequencyInformation(stationInformation);
		} catch (Exception e) {
			Log.e(this.parent.getString(R.string.app_name),	e.getMessage() == null ? "Null pointer[ProgramHandler.processJSONObject]" : e.getMessage());
		}
	}

	private void processStationInformation(JSONObject stationInformation)  {
		try
		{
			this.station.setFrequency(stationInformation.has("frequency") ? Utils.parseFloatFromString(stationInformation.getString("frequency")):this.station.getFrequency());
			this.station.setLocation(stationInformation.has("location") ? stationInformation.getJSONObject("location").getString("addressline1"):this.station.getLocation());
			this.station.setName(stationInformation.has("name") ? stationInformation.getString("name") : this.station.getName());
			this.station.setTelephoneNumber(stationInformation.has("transmitter_phone") ? stationInformation.getString("transmitter_phone") : this.station.getTelephoneNumber());
			this.station.setOwner(stationInformation.has("owner_id") ? stationInformation.getString("owner_id") : this.station.getOwner());
			this.station.persist();
	        this.synchronizationUtils.logSynchronization(SynchronizationType.Station, 1, 1, this.getLastUpdatedDate(stationInformation));
		} catch (JSONException e) {
			Log.e(this.parent.getResources().getString(R.string.app_name),	e.getMessage() == null? "Null pointer[StationINformationHandler.processStationInformation]": e.getMessage());
		}
	}
	
	private void processSynchronizationFrequencyInformation(JSONObject stationInformation)
	{
		try
		{
			if(stationInformation.has("client_update_frequency"))
			{
			this.synchronizationConfiguration.setQuantity(stationInformation.getInt("client_update_frequency"));
			this.synchronizationConfiguration.setUnitId(1);
			this.synchronizationConfiguration.save();
			this.synchronizationUtils.logSynchronization(SynchronizationType.SynchronizationFrequency, 1, 1, this.getLastUpdatedDate(stationInformation));
			}
		} catch (JSONException ex)
		{
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage());
		}
	}
	
	private void processDiagnosticsFrequencyInformation(JSONObject stationInformation)
	{
		try
		{
			if(stationInformation.has("analytic_update_frequency"))
			{
			this.diagnosticsConfiguration.setQuantity(stationInformation.getInt("client_update_frequency"));
			this.diagnosticsConfiguration.setUnitId(1);
			this.diagnosticsConfiguration.persist();
			this.synchronizationUtils.logSynchronization(SynchronizationType.DiagnosticsFrequency, 1, 1, this.getLastUpdatedDate(stationInformation));
			}
		} catch (JSONException ex)
		{
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage());
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
