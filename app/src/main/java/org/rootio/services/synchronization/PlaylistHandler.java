/**
 *
 */
package org.rootio.services.synchronization;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.persistence.DBAgent;

import android.content.Context;
import android.util.Log;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class PlaylistHandler implements SynchronizationHandler {

    private Context parent;
    private Cloud cloud;

    PlaylistHandler(Context parent, Cloud cloud) {
        this.parent = parent;
        this.cloud = cloud;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.rootio.services.synchronization.SynchronizationHandler#
     * getSynchronizationData()
     */
    @Override
    public JSONObject getSynchronizationData() {
        return new JSONObject();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.rootio.services.synchronization.SynchronizationHandler#
     * processJSONResponse(org.json.JSONObject)
     */
    @Override
    public void processJSONResponse(JSONObject synchronizationResponse) {
        ArrayList<String[]> playlistItems = new ArrayList<String[]>();
        try {
            JSONArray playlists = synchronizationResponse.getJSONArray("objects");
            for (int i = 0; i < playlists.length(); i++) {
                JSONObject playlist = playlists.getJSONObject(i);
                // Save the songs
                JSONArray songs = playlist.getJSONArray("songs");
                for (int j = 0; j < songs.length(); j++) {
                    playlistItems.add(new String[]{playlist.getString("title"), songs.getJSONObject(j).getString("title"), "1"});
                }
                // save the Albums
                JSONArray albums = playlist.getJSONArray("albums");
                for (int j = 0; j < albums.length(); j++) {
                    playlistItems.add(new String[]{playlist.getString("title"), albums.getJSONObject(j).getString("title"), "2"});
                }
                // save the Artists
                JSONArray artists = playlist.getJSONArray("artists");
                for (int j = 0; j < artists.length(); j++) {
                    playlistItems.add(new String[]{playlist.getString("title"), artists.getJSONObject(j).getString("title"), "3"});
                }
            }
            DBAgent dbAgent = new DBAgent(this.parent);
            dbAgent.deleteRecords("playlist", null, null);//empty playlist info
            dbAgent.bulkSaveData("playlist", null, new String[]{"title", "item", "itemtypeid"}, playlistItems.toArray(new String[playlistItems.size()][]));
        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlaylistHandler.processJSONResponse)" : ex.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.rootio.services.synchronization.SynchronizationHandler#
     * getSynchronizationURL()
     */
    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/playlists?api_key=%s", this.cloud.getServerScheme(), this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey());
    }

}
