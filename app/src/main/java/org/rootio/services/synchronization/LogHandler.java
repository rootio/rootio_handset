package org.rootio.services.synchronization;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

public class LogHandler implements SynchronizationHandler {

    private Context parent;
    private Cloud cloud;

    LogHandler(Context parent, Cloud cloud) {
        this.parent = parent;
        this.cloud = cloud;
    }

    public JSONObject getSynchronizationData() {
        return this.getRecords();
    }

    /**
     * Breaks down the information in the JSON file for program and schedule
     * information
     *
     * @param synchronizationResponse The JSON program definition received from the cloud server
     */
    public void processJSONResponse(JSONObject synchronizationResponse) {
        try {
            JSONArray objects = synchronizationResponse.getJSONArray("results");

            for (int i = 0; i < objects.length(); i++) {
                if (objects.getJSONObject(i).getBoolean("status")) {
                    this.deleteSyncedRecord(objects.getJSONObject(i).getString("id"));
                }
            }
        } catch (Exception e) {
            Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null ? "Null pointer[ProgramHandler.processJSONObject]" : e.getMessage());
        }
    }

    private int deleteSyncedRecord(String id) {
        String tableName = "activitylog";
        String whereClause = "id = ?";
        String[] filterArgs = new String[]{id};
        DBAgent agent = new DBAgent(this.parent);
        return agent.deleteRecords(tableName, whereClause, filterArgs);
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("https://%s:%s/%s/%s/log?api_key=%s", this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey());
    }

    private JSONObject getRecords() {
        String query = "select id, category, argument, event, eventdate  from activitylog";
        String[] filterArgs = new String[]{};
        DBAgent agent = new DBAgent(this.parent);
        String[][] results = agent.getData(query, filterArgs);
        JSONObject parent = new JSONObject();
        JSONArray logData = new JSONArray();
        try {
            for (int index = 0; index < results.length; index++) {
                JSONObject record = new JSONObject();
                record.put("id", results[index][0]);
                record.put("category", results[index][1]);
                record.put("argument", results[index][2]);
                record.put("event", results[index][3]);
                record.put("eventdate", results[index][4]);
                logData.put(record);
            }
            parent.put("log_data", logData);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Utils.writeToFile(this.parent, parent.toString());
        return parent;
    }
}
