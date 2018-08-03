/**
 *
 */
package org.rootio.services.synchronization;

import org.json.JSONObject;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class StationHandler implements SynchronizationHandler {
    private Context parent;
    private Cloud cloud;

    public StationHandler(Context parent, Cloud cloud) {
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
        //Lazy approach: save all JSON to prefs in one field, get it out and parse it later :-D
        ContentValues values  = new ContentValues();
        values.put("station_information", synchronizationResponse.toString());
        Utils.savePreferences(values, this.parent);
        //Utils.saveJSONToFile(this.parent, synchronizationResponse, this.parent.getFilesDir().getAbsolutePath() + "/station.json");
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("http://%s:%s/%s/%s/information?api_key=%s", this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey());
    }

}
