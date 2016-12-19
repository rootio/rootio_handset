package org.rootio.services.synchronization;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

public class CallLogHandler implements SynchronizationHandler {

	private Context parent;
	private ContentResolver cr;
	private Uri uri;
	private Cloud cloud;

	CallLogHandler(Context parent, Cloud cloud) {
		this.parent = parent;
		this.cloud = cloud;
		this.prepareContentResolver();
	}

	private void prepareContentResolver() {
		cr = this.parent.getContentResolver();
		uri = CallLog.Calls.CONTENT_URI;

	}

	@Override
	public JSONObject getSynchronizationData() {
		JSONObject data = new JSONObject();
		JSONArray calls = new JSONArray();
		String sortOrder = CallLog.Calls.DATE + " ASC";
		String[] columns = new String[] { CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.DATE, CallLog.Calls.TYPE };
		try {
			Cursor cur = cr.query(uri, columns, null, null, sortOrder);
            if (cur != null && cur.getCount() > 0) {
				while (cur.moveToNext()) {
					JSONObject callRecord = new JSONObject();
					callRecord.put("call_uuid", cur.getLong(0));
					if(cur.getInt(4)== CallLog.Calls.INCOMING_TYPE || cur.getInt(4)== CallLog.Calls.MISSED_TYPE)
					{
						callRecord.put("from_phonenumber", cur.getString(1));
						callRecord.put("to_phonenumber", "");
					}
					else
					{
						callRecord.put("from_phonenumber", "");
						callRecord.put("to_phonenumber", cur.getString(1));
					}
					callRecord.put("duration", cur.getString(2));
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(cur.getLong(3));
					callRecord.put("start_time", Utils.getDateString(cal.getTime(), "yyyy-MM-dd HH:mm:ss"));
					calls.put(callRecord);
				}
			}
			cur.close();
			data.put("call_data", calls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public void processJSONResponse(JSONObject synchronizationResponse) {
		JSONArray results;
		try {
			results = synchronizationResponse.getJSONArray("results");

			for (int i = 0; i < results.length(); i++) {
				if (results.getJSONObject(i).getBoolean("status")) {
					//Utils.toastOnScreen("Deleting record with ID" + String.valueOf(results.getJSONObject(i).getLong("id")), this.parent);
					//this.parent.getContentResolver().delete(uri, CallLog.Calls._ID + " = ? ", new String[] { String.valueOf(results.getJSONObject(i).getLong("id")) });
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getSynchronizationURL() {
		return String.format("http://%s:%s/%s/%s/call?api_key=%s", this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey());	
	}
}
