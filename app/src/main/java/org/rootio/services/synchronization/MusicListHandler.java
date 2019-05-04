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

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class MusicListHandler implements SynchronizationHandler {

    private Context parent;
    private Cloud cloud;
    private int offset, limit = 100;
    private long maxDateadded;

    MusicListHandler(Context parent, Cloud cloud) {
        this.parent = parent;
        this.cloud = cloud;
        this.maxDateadded = this.getMaxDateAdded();
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
            if(synchronizationResponse.getBoolean("status"))
            {
                this.logMaxDateAdded(this.maxDateadded);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSynchronizationURL() {
        return String.format("%s://%s:%s/%s/%s/music?api_key=%s", this.cloud.getServerScheme(), this.cloud.getServerAddress(), this.cloud.getHTTPPort(), "api/station", this.cloud.getStationId(), this.cloud.getServerKey());
  }

    private long getMaxDateAdded()
    {
        return (long)Utils.getPreference("media_max_date_added", long.class, this.parent);
    }

    private void logMaxDateAdded(long maxDate) {
        ContentValues values = new ContentValues();
        values.put("media_max_date_added", maxDate);
        Utils.savePreferences(values, this.parent);
    }

    private JSONObject getSongList() {
        JSONObject music = new JSONObject();
        String artist, album;
        Cursor cur = null;
        try {
            ContentResolver cr = this.parent.getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 and date_added > ?";
            //uncomment last bit to turn on paging in case of very many records, but in this case sort by date_added asc
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC"; // limit   " + String.valueOf(limit) + " offset "+String.valueOf(offset);
            cur = cr.query(uri, null, selection, new String[]{String.valueOf(this.maxDateadded)}, sortOrder);
            offset += limit;
            int count;

            if (cur != null) {
                count = cur.getCount();


                if (count > 0) {
                    if(count < limit) // we have reached the end of all records!
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
                        if(dateAdded > maxDateadded)
                        {
                            maxDateadded = dateAdded;
                        }

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
