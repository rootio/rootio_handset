package org.rootio.services.synchronization;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.handset.BuildConfig;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class ProgramsHandler implements SynchronizationHandler {

    private Context parent;
    private Cloud cloud;
    private int records=2000;

    ProgramsHandler(Context parent, Cloud cloud) {
        this.parent = parent;
        this.cloud = cloud;
    }

    @Override
    public JSONObject getSynchronizationData() {
        return new JSONObject();
    }

    private Date getTodayBaseDate() {
        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    @Override
    public void processJSONResponse(JSONObject synchronizationResponse) {
        boolean hasChanges = false;
        boolean shouldRestart = false;
        boolean result = false;
        ArrayList<ContentValues> values = new ArrayList();
        //long result = 0;
        JSONArray results;
        try {
            results = synchronizationResponse.getJSONArray("scheduled_programs");

            for (int i = 0; i < results.length(); i++) {
                //if the record did not exist in the DB, then it is new ;-)
                int recs = this.deleteRecord(results.getJSONObject(i).getLong("scheduled_program_id"));
                if ((recs < 1 || results.getJSONObject(i).getBoolean("deleted")) && (isCurrentOrFutureChange(results.getJSONObject(i).getString("start"), results.getJSONObject(i).getString("end")))) {
                    hasChanges = true;
                    if(results.getJSONObject(i).getInt("program_type_id") == 2 && isCurrent(results.getJSONObject(i).getString("start"), results.getJSONObject(i).getString("end")))
                    {
                       shouldRestart = true;
                    }
                }

                values.add(getContentValues(results.getJSONObject(i).getInt("scheduled_program_id"), results.getJSONObject(i).getString("name"), Utils.getDateFromString(results.getJSONObject(i).getString("start"), "yyyy-MM-dd'T'HH:mm:ss"), Utils.getDateFromString(results.getJSONObject(i).getString("end"), "yyyy-MM-dd'T'HH:mm:ss"), results.getJSONObject(i).getString("structure"), Utils.getDateFromString(results.getJSONObject(i).getString("updated_at"), "yyyy-MM-dd'T'HH:mm:ss"), results.getJSONObject(i).getString("program_type_id"), results.getJSONObject(i).getBoolean("deleted")));
            }
            if(values.size() > 0) {
                result = this.saveRecords(values);
            }
            if (result && hasChanges) {
                this.announceScheduleChange(shouldRestart);
            }
            if(results.length() == this.records) // we had a full page, maybe more records..
            {
                 this.requestSync(true);
            }
            else {
                this.requestSync(false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestSync(boolean isStarting) {
        Intent intent = new Intent();
        intent.setAction("org.rootio.services.synchronization.SYNC_REQUEST");
        intent.putExtra("category", 2);
        intent.putExtra("sync", isStarting?"start": "end");
        this.parent.sendBroadcast(intent);
    }

    private boolean isCurrent(String startDateStr, String endDateStr)
    {
        try {
            Date now = Calendar.getInstance().getTime();
            Date startDate = Utils.getDateFromString(startDateStr, "yyyy-MM-dd'T'HH:mm:ss");
            Date endDate = Utils.getDateFromString(endDateStr, "yyyy-MM-dd'T'HH:mm:ss");
            return (startDate.compareTo(now) <= 0 && endDate.compareTo(now) >= 0); //yet to begin today, or is running
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isCurrentOrFutureChange(String startDateStr, String endDateStr) {
        try {
            Date now = Calendar.getInstance().getTime();
            Date startDate = Utils.getDateFromString(startDateStr, "yyyy-MM-dd'T'HH:mm:ss");
            Date endDate = Utils.getDateFromString(endDateStr, "yyyy-MM-dd'T'HH:mm:ss");
            Boolean isToday = now.getYear() == startDate.getYear() && now.getMonth() == startDate.getMonth() && now.getDate() == startDate.getDate();
            return (isToday && startDate.compareTo(now) >= 0) || (startDate.compareTo(now) <= 0 && endDate.compareTo(now) >= 0); //yet to begin today, or is running
        } catch (Exception ex) {
            return false;
        }
    }

    private void announceScheduleChange(boolean shouldRestart) {
        Intent intent = new Intent("org.rootio.services.synchronization.SCHEDULE_CHANGE_EVENT");
        intent.putExtra("shouldRestart", shouldRestart);
        this.parent.sendBroadcast(intent);
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/programs?api_key=%s&%s&version=%s_%s", this.cloud.getServerScheme(), this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey(), this.getSincePart(), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    private String getSincePart() {
        String query = "select max(updatedat) from scheduledprogram";
        String[][] result = DBAgent.getData(query, new String[]{});
        if (result == null || result.length == 0 || result[0][0] == null) {
            return String.format("all=1&records=%s", this.records); //Todo: implement start and end based filtering to not fetch very old records
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(Utils.getDateFromString(result[0][0], "yyyy-MM-dd HH:mm:ss"));
        cal.add(Calendar.SECOND, 1); //Add 1 second, server side compares using greater or equal
        return String.format("updated_since=%s&records=%s", Utils.getDateString(cal.getTime(), "yyyy-MM-dd'T'HH:mm:ss"), records);
    }

    private boolean saveRecords(ArrayList<ContentValues> values) {
        String tableName = "scheduledprogram";
        return DBAgent.bulkSaveData(tableName, null, values.toArray(new ContentValues[values.size()]));
    }

    @NonNull
    private ContentValues getContentValues(int id, String name, Date start, Date end, String structure, Date updatedAt, String programTypeId, Boolean deleted) {
        ContentValues data = new ContentValues();
        data.put("id", id);
        data.put("name", name);
        data.put("start", Utils.getDateString(start, "yyyy-MM-dd HH:mm:ss"));
        data.put("end", Utils.getDateString(end, "yyyy-MM-dd HH:mm:ss"));
        data.put("structure", structure);
        data.put("programtypeid", programTypeId);
        data.put("updatedat", Utils.getDateString(updatedAt, "yyyy-MM-dd HH:mm:ss"));
        data.put("deleted", deleted);
        return data;
    }

    private int deleteRecord(long id) {
        String tableName = "scheduledprogram";
        String whereClause = "id = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return DBAgent.deleteRecords(tableName, whereClause, whereArgs);

    }
}
