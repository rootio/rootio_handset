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
import org.rootio.tools.utils.Utils;

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
		String artist, album, title;
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


						song.put("title", cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)).replace("\u2019", "'").replace("\u2018","'"));
						song.put("duration", cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));

						artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)) == null ? "unknown" : cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
						artist = artist.replace("\u2019", "'").replace("\u2018","'");
						if (!music.has(artist)) {
							music.put(artist, new JSONObject());
						}

						album = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)) == null? "unknown" : cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM));
						album = album.replace("\u2019", "'").replace("\u2018","'");

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
