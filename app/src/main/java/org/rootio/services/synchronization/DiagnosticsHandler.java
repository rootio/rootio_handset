package org.rootio.services.synchronization;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rootio.activities.DiagnosticStatistics;
import org.rootio.handset.R;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.persistence.DBAgent;

import android.content.Context;
import android.util.Log;

public class DiagnosticsHandler implements SynchronizationHandler {

    private Context parent;
    private DiagnosticStatistics diagnosticStatistics;
    private Cloud cloud;

    DiagnosticsHandler(Context parent, Cloud cloud) {
        this.parent = parent;
        this.cloud = cloud;
        this.diagnosticStatistics = new DiagnosticStatistics(this.parent);
    }

    public JSONObject getSynchronizationData() {
        return this.diagnosticStatistics.getJSONRecords();
    }

    /**
     * Breaks down the information in the JSON file for program and schedule
     * information
     *
     * @param programDefinition The JSON program definition received from the cloud server
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
        String tableName = "diagnostic";
        String whereClause = "_id = ?";
        String[] filterArgs = new String[]{id};
        DBAgent agent = new DBAgent(this.parent);
        return agent.deleteRecords(tableName, whereClause, filterArgs);
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("https://%s:%s/%s/%s/analytics?api_key=%s", this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey());
    }

}
