package org.rootio.tools.media;

import java.util.HashMap;
import java.util.HashSet;

import org.rootio.tools.utils.Utils;

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
		cursor.close();
	}

	Media getMedia(String title) {
		if(this.mediaCatalog.get(title) == null)
		{
			Utils.toastOnScreen("is null for "+title, this.parent);
			for(String k : mediaCatalog.keySet())
			{
				Utils.toastOnScreen(k, this.parent);
			}
		}
		return this.mediaCatalog.get(title);
	}
	
	HashSet<Media> getMediaForArtist(String artistTitle)
	{
		HashSet<Media> songs = getAudioContent(artistTitle, Audio.Media.ARTIST);
		return songs;
	}

	HashSet<Media> getMediaForAlbum(String albumTitle)
	{
		HashSet<Media> songs = getAudioContent(albumTitle, Audio.Media.ALBUM);
		return songs;
		}

	private HashSet<Media> getAudioContent(String value, String column) {
		HashSet<Media> songs = new HashSet<Media>();
		ContentResolver cr = this.parent.getContentResolver();
		Uri mediaURI = Audio.Media.EXTERNAL_CONTENT_URI;
		String filter = column  + " = ?";
		String[] arguments = new String[] { value};
		String[] columns = new String[] { Audio.Media.TITLE, Audio.Media.DATA, Audio.Media.DURATION, Audio.Media.ARTIST };
		Cursor cursor = cr.query(mediaURI, columns, filter, arguments, null);
		while (cursor.moveToNext()) {
			songs.add(new Media(cursor.getString(0), cursor.getString(1), cursor.getLong(2), cursor.getString(3)));
		}
		return songs;
	}
}
