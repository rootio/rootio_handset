/**
 *
 */
package org.rootio.services.synchronization;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.handset.BuildConfig;
import org.rootio.handset.R;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class MusicListHandler implements SynchronizationHandler {

    private Context parent;
    private Cloud cloud;
    private int offset, limit = 100;

    MusicListHandler(Context parent, Cloud cloud) {
        this.parent = parent;
        this.cloud = cloud;
    }

    @Override
    public JSONObject getSynchronizationData() {
        JSONObject snglist = this.getSongList();
        return snglist;
        // return this.getSongList();
    }

    @Override
    public void processJSONResponse(JSONObject synchronizationResponse) {
        // do nothing with the response
        try {
            if (synchronizationResponse.getBoolean("status")) {
                this.logMaxDateAdded(synchronizationResponse.getString("date"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSynchronizationURL() {
        try {
            if (isSyncDue()) {
                return String.format("%s://%s:%s/%s/%s/music?api_key=%s&version=%s_%s", this.cloud.getServerScheme(), this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey(), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isSyncDue() throws JSONException {
        List<Integer> result = getMediaStatus();
        if (result == null) {
            return false; //something wrong, needs looking into
        }
        String response = Utils.doPostHTTP(getPreSynchronizationURL(), "");
        JSONObject status = new JSONObject(response);

        //if the number of songs, albums or artists is different from what is on the server
        //and the sync date is different from what is on the server, a sync is in order
        boolean syncDue = !(status.getJSONObject("songs").getLong("count") == (long) result.get(0) && status.getJSONObject("songs").getString("max_date").equals(getMaxDateAdded())
                && status.getJSONObject("albums").getLong("count") == (long) result.get(1) && status.getJSONObject("albums").getString("max_date").equals(getMaxDateAdded())
                && status.getJSONObject("artists").getLong("count") == (long) result.get(2) && status.getJSONObject("artists").getString("max_date").equals(getMaxDateAdded()));
        Log.i("RootIO", "Media Sync Due: " + syncDue + " ( " + status + " ) and " + result.get(0) + ", " + getMaxDateAdded());
        return syncDue;
    }

    private String getPreSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/music_status?api_key=%s&version=%s_%s", this.cloud.getServerScheme(), this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey(), BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    private String getMaxDateAdded() {
        String maxDate = (String) Utils.getPreference("media_max_date_added", String.class, this.parent);
        return maxDate != null && maxDate.contains(".") ? maxDate.substring(0, maxDate.indexOf(".")) : maxDate;
    }

    private void logMaxDateAdded(String maxDate) {
        ContentValues values = new ContentValues();
        values.put("media_max_date_added", maxDate);
        Utils.savePreferences(values, this.parent);
    }

    private List<Integer> getMediaStatus() {
        List<Integer> result = new ArrayList<>();
        for (String column : new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST}) {
            result.add(getCount(column));
        }
        return result;
    }

    /**
     * For content providers not using SQLite, getting distinct row counts is not that straightforward :-(
     * @param column The column for which you are trying to get a distinct count
     * @return number of distinct values in this column
     */
    private int getCount(String column) {
        int result = 0;
        String previous = null;
        Cursor cur;
        try {
            ContentResolver cr = this.parent.getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String[] projection = new String[]{column};
            cur = cr.query(uri, projection, selection, new String[]{}, column + " ASC");
            while (cur.moveToNext()) {
                if (!cur.getString(0).equals(previous)) {
                    result++;
                }
                previous = cur.getString(0);
            }
        } catch (Exception ex) {
            Log.e("RootIO", "getCount: " + ex.getMessage());
        }
        return result;
    }

    private JSONObject getSongList() {
        JSONObject music = new JSONObject();
        String artist, album;
        Cursor cur = null;
        try {
            ContentResolver cr = this.parent.getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 ";
            //uncomment last bit to turn on paging in case of very many records, but in this case sort by date_added asc
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC"; // limit   " + String.valueOf(limit) + " offset "+String.valueOf(offset);
            cur = cr.query(uri, null, selection, new String[]{}, sortOrder);
            offset += limit;
            int count;

            if (cur != null) {
                count = cur.getCount();
                //Utils.toastOnScreen("records is "+count, this.parent);


                if (count > 0) {
                    if (count < limit) // we have reached the end of all records!
                    {
                        offset = 0;
                    }
                    while (cur.moveToNext()) {
                        JSONObject song = new JSONObject();


                        song.put("title", cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)).replace("\u2019", "'").replace("\u2018", "'"));
                        song.put("duration", cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                        long dateAdded = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                        song.put("date_added", dateAdded);
                        song.put("date_modified", cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)));


                        artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)) == null ? "unknown" : cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        artist = artist.replace("\u2019", "'").replace("\u2018", "'");
                        if (!music.has(artist)) {
                            music.put(artist, new JSONObject());
                        }

                        album = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)) == null ? "unknown" : cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                        album = album.replace("\u2019", "'").replace("\u2018", "'");

                        if (!music.getJSONObject(artist).has(album)) {
                            music.getJSONObject(artist).put(album, new JSONObject());
                            music.getJSONObject(artist).getJSONObject(album).put("songs", new JSONArray());
                        }

                        music.getJSONObject(artist).getJSONObject(album).getJSONArray("songs").put(song);

                    }
                }
            }

        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(MusicListHandler.getSongList)" : ex.getMessage());
        } finally {
            try {
                cur.close();
            } catch (Exception ex) {
                Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(MusicListHandler.getSongList)" : ex.getMessage());
            }
        }
        return music;

    }

}
