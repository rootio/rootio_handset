/**
 * 
 */
package org.rootio.services.synchronization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.cloud.Cloud;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

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
		JSONObject snglist = this.getSongList();
		this.writeToFile(snglist.toString());
		return snglist;
		// return this.getSongList();
	}

	private void writeToFile(String data) {
		File music_json = new File("/mnt/extSdCard/Music/music.json");
		FileWriter fwr;
		try {
			fwr = new FileWriter(music_json);
			fwr.write(data);
			fwr.flush();
			fwr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

			if (cur != null) {
				count = cur.getCount();

				if (count > 0) {
					while (cur.moveToNext()) {
						JSONObject song = new JSONObject();
						song.put("title", cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)));
						song.put("duration", cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));

						if (!music.has(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)))) {
							music.put(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)), new JSONObject());
						}

						if (!music.getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST))).has(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)))) {
							music.getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST))).put(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)), new JSONObject());
							music.getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST))).getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM))).put("songs", new JSONArray());
						}

						music.getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST))).getJSONObject(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM))).getJSONArray("songs").put(song);

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
