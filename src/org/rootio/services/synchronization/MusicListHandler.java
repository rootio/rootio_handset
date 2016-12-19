/**
 * 
 */
package org.rootio.services.synchronization;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rootio.tools.cloud.Cloud;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 * 
 */
public class MusicListHandler implements SynchronizationHandler {

	private Context parent;
	private Cloud cloud;
	
	MusicListHandler(Context parent, Cloud cloud) {
		this.parent = parent;
		this.cloud = cloud;
	}

	@Override
	public JSONObject getSynchronizationData() {
		return this.getSongList();
	}

	@Override
	public void processJSONResponse(JSONObject synchronizationResponse) {
		// do nothing with the response
	}

	@Override
	public String getSynchronizationURL() {
		return String.format("http://%s:%s/api/station/%s/music?api_key=%s", cloud.getServerAddress(), cloud.getHTTPPort(), cloud.getStationId(), cloud.getServerKey());
	}

	private JSONObject getSongList() {
		JSONObject music = new JSONObject();
		Cursor cur = null;
		try {
			ContentResolver cr = this.parent.getContentResolver();
			Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
			String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
			cur = cr.query(uri, null, selection, null, sortOrder);
			int count = 0;

			//JSONObject albums = new JSONObject(), songs = new JSONObject(), artists = new JSONObject();
			if (cur != null) {
				count = cur.getCount();

				if (count > 0) {
					while (cur.moveToNext()) {
						JSONObject song = new JSONObject();
						song.put("title", cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)));
						song.put("duration", cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));

						if (!music.has(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST))));
						music.put(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)), new JSONObject());

						if (!music.getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST))).has(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)))) {
							music.getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST))).put(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)), new JSONObject());
							music.getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST))).getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM))).put("songs", new JSONArray());
						}

						music.getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST))).getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM))).getJSONArray("songs").put(song);

						// albums.accumulate(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
						// song);
						// artists.accumulate(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
						// albums);
						// this.music = albums;
					}
				}
			}
		
		} catch (Exception ex) {

		} finally {
			try {
				cur.close();
			} catch (Exception ex) {
				// log
			}
		}
		return music;

	}

}
