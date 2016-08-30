package org.rootio.tools.media;

import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;

public class MediaLibrary {

	private Context parent;
	private HashMap<String, Media> mediaCatalog;

	MediaLibrary(Context parent) {
		this.parent = parent;
		this.loadMedia();
	}

	private void loadMedia() {
		this.mediaCatalog = new HashMap<String, Media>();
		ContentResolver cr = this.parent.getContentResolver();
		Uri mediaURI = Audio.Media.EXTERNAL_CONTENT_URI;
		String filter = Audio.Media.IS_MUSIC + "!= 0";
		String[] columns = new String[] { Audio.Media.TITLE, Audio.Media.DATA, Audio.Media.DURATION, Audio.Media.ARTIST };
		Cursor cursor = cr.query(mediaURI, columns, filter, null, null);
		while (cursor.moveToNext()) {
			this.mediaCatalog.put(cursor.getString(0), new Media(cursor.getString(0), cursor.getString(1), cursor.getLong(2), cursor.getString(3)));
		}
	}

	Media getMedia(String title) {
		return this.mediaCatalog.get(title);
	}
}