/**
 * 
 */
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

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 *
 */
public class SMSLogHandler implements SynchronizationHandler {

	private Context parent;
	private ContentResolver cr;
	private Uri uri;
	private Cloud cloud;

	SMSLogHandler(Context parent, Cloud cloud) {
		this.parent = parent;
		this.cloud = cloud;
		this.prepareContentResolver();
	}

	private void prepareContentResolver() {
		cr = this.parent.getContentResolver();
		uri = Uri.parse("content://sms");

	}

	@Override
	public JSONObject getSynchronizationData() {
		JSONObject data = new JSONObject();
		JSONArray sms = new JSONArray();
		String[] columns = new String[] { "_id", "address", "body", "date", "type" };
		try {
			Cursor cur = cr.query(uri, columns, null, null, null);
            if (cur != null && cur.getCount() > 0) {
				while (cur.moveToNext()) {
					JSONObject smsRecord = new JSONObject();
					smsRecord.put("message_uuid", cur.getLong(0));
					if(cur.getInt(4)== 1)
					{
						smsRecord.put("from_phonenumber", cur.getString(1));
						smsRecord.put("to_phonenumber", "");
					}
					else
					{
						smsRecord.put("from_phonenumber", "");
						smsRecord.put("to_phonenumber", cur.getString(1));
					}
					smsRecord.put("text", cur.getString(2));
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(cur.getLong(3));
					smsRecord.put("sendtime", Utils.getDateString(cal.getTime(), "yyyy-MM-dd HH:mm:ss"));
					sms.put(smsRecord);
				}
			}
			cur.close();
			data.put("message_data", sms);
		} catch (JSONException e) {
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
					Utils.toastOnScreen("Deleting record with ID" + String.valueOf(results.getJSONObject(i).getLong("id")), this.parent);
					this.parent.getContentResolver().delete(uri,  "_id = ? ", new String[] { String.valueOf(results.getJSONObject(i).getLong("id")) });
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getSynchronizationURL() {
		return String.format("http://%s:%s/%s/%s/message?api_key=%s", this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey());	
	}
}