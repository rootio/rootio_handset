/**
 *
 */
package org.rootio.services.synchronization;

import java.io.File;
import java.io.FileOutputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
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
     * Handles information received from the cloud server pertaining to frequency for synchronization and diagnostics
     *
     * @param synchronizationResponse The JSON info containing frequency in seconds for measuring diagnostics and communicating to the cloud
     */
    public void processJSONResponse(JSONObject synchronizationResponse) {
        if (synchronizationResponse != null) {
            ContentValues values = new ContentValues();
            values.put("frequencies", synchronizationResponse.toString());
            Utils.savePreferences(values, this.parent);
        }

    }

    @Override
    public String getSynchronizationURL() {
        return String.format("https://%s:%s/%s/%s/frequency_update?api_key=%s", this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey());
    }
}