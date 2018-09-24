package org.rootio.services.synchronization;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

public class ProgramsHandler implements SynchronizationHandler {

    private Context parent;
    private Cloud cloud;

    ProgramsHandler(Context parent, Cloud cloud) {
        this.parent = parent;
        this.cloud = cloud;
    }

    @Override
    public JSONObject getSynchronizationData() {
        return new JSONObject();
    }

    @Override
    public void processJSONResponse(JSONObject synchronizationResponse) {
        ArrayList<JSONObject> changes = new ArrayList<>();
        JSONArray results;
        try {
            results = synchronizationResponse.getJSONArray("scheduled_programs");

            for (int i = 0; i < results.length(); i++) {
                //if the record did not exist in the DB, then it is new ;-)
                int recs = this.deleteRecord(results.getJSONObject(i).getLong("scheduled_program_id"));
                if (recs < 1 || results.getJSONObject(i).getBoolean("deleted")) //new record, or deleted record!
                {
                    changes.add(results.getJSONObject(i));
                }
                this.saveRecord(results.getJSONObject(i).getInt("scheduled_program_id"), results.getJSONObject(i).getString("name"), Utils.getDateFromString(results.getJSONObject(i).getString("start"), "yyyy-MM-dd'T'HH:mm:ss"), Utils.getDateFromString(results.getJSONObject(i).getString("end"), "yyyy-MM-dd'T'HH:mm:ss"), results.getJSONObject(i).getString("structure"), Utils.getDateFromString(results.getJSONObject(i).getString("updated_at"), "yyyy-MM-dd'T'HH:mm:ss"), results.getJSONObject(i).getString("program_type_id"), results.getJSONObject(i).getBoolean("deleted"));
            }
            if (changes.size() > 0) {
                Utils.toastOnScreen("changes found", this.parent);
                Date today = Calendar.getInstance().getTime();
                for (JSONObject change : changes) {
                    Date changeDate = Utils.getDateFromString(change.getString("start"), "yyyy-MM-dd'T'HH:mm:ss");
                    if (changeDate.getDate() == today.getDate() && changeDate.getMonth() == today.getMonth() && changeDate.getYear() == today.getYear()) // call this only when changes affect schedule of the day
                    {
                        Utils.toastOnScreen("Announcing", this.parent);
                        this.announceScheduleChange();
                    }
                    else
                    {
                        Utils.toastOnScreen("Not today", this.parent);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void announceScheduleChange() {
        Intent intent = new Intent("org.rootio.services.synchronization.SCHEDULE_CHANGE_EVENT");
        this.parent.sendBroadcast(intent);
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("https://%s:%s/api/station/%s/programs?api_key=%s&%s", cloud.getServerAddress(), cloud.getHTTPPort(), cloud.getStationId(), cloud.getServerKey(), this.getSincePart());
    }

    private String getSincePart() {
        String query = "select max(updatedat) from scheduledprogram";
        String[][] result = new DBAgent(this.parent).getData(query, new String[]{});
        if (result == null || result.length == 0 || result[0][0] == null) {
            return String.format("start=%sT00:00:00", Utils.getDateString(Calendar.getInstance().getTime(), "yyyy-MM-dd"));
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(Utils.getDateFromString(result[0][0], "yyyy-MM-dd HH:mm:ss"));
        cal.add(Calendar.SECOND, 1); //Add 1 second, server side compares using greater or equal
        return String.format("updated_since=%s", Utils.getDateString(cal.getTime(), "yyyy-MM-dd'T'HH:mm:ss"));
    }

    private long saveRecord(int id, String name, Date start, Date end, String structure, Date updatedAt, String programTypeId, boolean deleted) {
        String tableName = "scheduledprogram";
        ContentValues data = new ContentValues();
        data.put("id", id);
        data.put("name", name);
        data.put("start", Utils.getDateString(start, "yyyy-MM-dd HH:mm:ss"));
        data.put("end", Utils.getDateString(end, "yyyy-MM-dd HH:mm:ss"));
        data.put("structure", structure);
        data.put("programtypeid", programTypeId);
        data.put("updatedat", Utils.getDateString(updatedAt, "yyyy-MM-dd HH:mm:ss"));
        data.put("deleted", deleted);
        return new DBAgent(this.parent).saveData(tableName, null, data);
    }

    private int deleteRecord(long id) {
        String tableName = "scheduledprogram";
        String whereClause = "id = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return new DBAgent(this.parent).deleteRecords(tableName, whereClause, whereArgs);

    }
}
