/**
 * 
 */
package org.rootio.services.synchronization;

import java.io.File;
import java.io.FileOutputStream;

import org.json.JSONObject;
import org.rootio.radioClient.R;
import org.rootio.tools.cloud.Cloud;

import android.content.Context;
import android.util.Log;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 *
 */
public class FrequencyHandler implements SynchronizationHandler {
	private Context parent;
	private Cloud cloud;

	FrequencyHandler(Context parent, Cloud cloud) {
		this.parent = parent;
		this.cloud = cloud;
	}

	public JSONObject getSynchronizationData() {
		return new JSONObject();
	}

	/**
	 * Breaks down the information in the JSON file for program and schedule information
	 * 
	 * @param programDefinition The JSON program definition received from the cloud server
	 */
	public void processJSONResponse(JSONObject synchronizationResponse) {
		FileOutputStream str = null;
		try {
			File whitelistFile = new File(this.parent.getFilesDir().getAbsolutePath() + "/frequency.json");
			str = new FileOutputStream(whitelistFile);
			str.write(synchronizationResponse.toString().getBytes());
		} catch (Exception e) {
			Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null ? "Null pointer[FrequencyHandler.processJSONObject]" : e.getMessage());
		}
		finally
		{
			try
			{
				str.close();
			}
			catch(Exception e)
			{
				Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null ? "Null pointer[FrequencyHandler.processJSONObject]" : e.getMessage());
			}
		}
	}

	@Override
	public String getSynchronizationURL() {
		return String.format("http://%s:%s/%s/%s/whitelist?api_key=%s", this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station/frequency", this.cloud.getStationId(), this.cloud.getServerKey());	
	}
}