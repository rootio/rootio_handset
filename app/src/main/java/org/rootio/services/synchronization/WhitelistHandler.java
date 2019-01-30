/**
 *
 */
package org.rootio.services.synchronization;

import java.io.File;
import java.io.FileOutputStream;

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
public class WhitelistHandler implements SynchronizationHandler {
    private Context parent;
    private Cloud cloud;

    WhitelistHandler(Context parent, Cloud cloud) {
        this.parent = parent;
        this.cloud = cloud;
    }

    public JSONObject getSynchronizationData() {
        return new JSONObject();
    }

    /**
     * Breaks down the information in the JSON file for program and schedule information
     *
     */
    public void processJSONResponse(JSONObject synchronizationResponse) {
        ContentValues values = new ContentValues();
        values.put("whitelist", synchronizationResponse.toString());
        Utils.savePreferences(values, this.parent);
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/whitelist?api_key=%s", this.cloud.getServerScheme(), this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey());
    }

}
