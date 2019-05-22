/**
 *
 */
package org.rootio.services.synchronization;

import org.json.JSONObject;
import org.rootio.handset.BuildConfig;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

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
     * @param synchronizationResponse The response returned from the cloud server
     */
    public void processJSONResponse(JSONObject synchronizationResponse) {
        boolean isConfigChanged = this.isConfigChanged(synchronizationResponse.toString());
        //Lazy approach: save all JSON to prefs in one field, get it out and parse it later :-D
        ContentValues values  = new ContentValues();
        values.put("station_information", synchronizationResponse.toString());
        Utils.savePreferences(values, this.parent);
        if(isConfigChanged)
        {
            this.announceSIPChange();
        }
        //Utils.saveJSONToFile(this.parent, synchronizationResponse, this.parent.getFilesDir().getAbsolutePath() + "/station.json");
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/information?api_key=%s&version=%s_%s", this.cloud.getServerScheme(), this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey(), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    private boolean isConfigChanged(String newConfiguration)
    {
        String currentConfiguration = (String) Utils.getPreference("station_information", String.class, this.parent);
        return (!currentConfiguration.equals(newConfiguration));
    }

    private void announceSIPChange()
    {
        //This is lazy -- any station change will result in re-registration. Ideal is to extract SIP components and compare them
        try {
               Intent intent = new Intent("org.rootio.handset.SIP.CONFIGURATION_CHANGE");
                this.parent.sendBroadcast(intent);
        }
        catch(Exception ex){
            //todo: log this
        }
    }

}
