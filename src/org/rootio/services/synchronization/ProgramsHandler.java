package org.rootio.services.synchronization;

import java.util.Date;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.radioClient.R;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.util.Log;

public class ProgramsHandler {

	private Context parent;
	private HashSet<Integer> programIds;
	private int stationId;
	private String serverKey;
	private String serverAddress;
	private int port;
	private Date since;
	
	ProgramsHandler(Context parent, String serverAddress, int port, int stationId, String serverKey, Date since)
	{
		this.parent = parent;
		this.stationId = stationId;
		this.serverKey = serverKey;
		this.serverAddress = serverAddress;
		this.port = port;
		this.since = since;
	}
	
	HashSet<Integer> getProgramIds()
	{
		String httpUrl = this.getHttpUrl(serverAddress, port, stationId, serverKey, since);
		String JSON = this.getProgramsJSON(httpUrl);
		this.programIds = this.getProgramIds(JSON);
		return this.programIds;
	}
	
	private HashSet<Integer> getProgramIds(String JSON) {
		HashSet<Integer> programIds = new HashSet<Integer>();
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(JSON);
			JSONArray objects = jsonObject.getJSONArray("objects");
			for (int i = 0; i < objects.length(); i++) {
				JSONObject tmp = objects.getJSONObject(i);
				if (tmp.has("id")) {
					programIds.add(tmp.getInt("id"));
				}
			}
		} catch (JSONException e) {
			Log.e(this.parent.getString(R.string.app_name), e.getMessage());
		}
//		catch (NullPointerException e) {
//			Log.e(this.parent.getString(R.string.app_name), e.getMessage()==null?"NullPointerException(ProgramsHandler.getProgramIds)":e.getMessage());
//		}
		return programIds;
	}
	
	private String getProgramsJSON(String httpUrl)
	{
		String response = Utils.doHTTP(httpUrl);
		return response;
	}
	
	private String getHttpUrl(String serverAddress, int port, int stationId, String serverKey, Date since)
	{
		String sincePart = this.since == null?"all=1":String.format("updated_since=%s",Utils.getDateString(since, "yyyy-MM-dd'T'HH:mm:ss"));
		//String httpUrl = String.format("http://%s:%s/api/station/%s/programs?api_key=%s&%s", serverAddress, port, stationId,serverKey,sincePart);
		String httpUrl = String.format("http://%s:%s/api/program?api_key=%s&%s", serverAddress, port,serverKey,sincePart);
		return httpUrl;
	}
}


