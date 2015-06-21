package org.rootio.tools.media;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

public class Media {
	private Long id;
	private final String title;
	private final String fileLocation;
	private String wiki;
	private int duration;
	private byte[] content;
	private final Genre[] genres;
	private final String[] tags;
	private final Artist[] artists;
	private final Context context;

	public Media(Context context, String fileLocation, String title, Genre[] genres, String[] tags, Artist[] artists) {
		this.context = context;
		this.fileLocation = fileLocation;
		this.title = title;
		this.genres = genres;
		this.tags = tags;
		this.artists = artists;
		this.id = Utils.getMediaId(title);
		if (this.id == null) {
			this.id = this.persist();
			this.persistMediaArtists();
			this.persistMediaGenres();
		}
	}

	/**
	 * Gets the artists associated with this media
	 * 
	 * @return Array of Artist objects
	 */
	public Artist[] getArtists() {
		return this.artists;
	}

	/**
	 * Gets the genres associated with this media
	 * 
	 * @return Array of Genre objects
	 */
	public Genre[] getGenre() {
		return this.genres;
	}

	/**
	 * Gets the content of this media
	 * 
	 * @return Array of bytes for media
	 */
	public byte[] getContent() {
		return this.content;
	}

	/**
	 * Gets the tags associated with this media
	 * 
	 * @return String array of tags
	 */
	public String[] getTags() {
		return this.tags;
	}

	/**
	 * Gets the duration of this media in seconds
	 * 
	 * @return Integer representing the duration of this media
	 */
	public int getDuration() {
		return this.duration;
	}

	/**
	 * Gets the title of this media
	 * 
	 * @return String representation of the title of this media
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Gets the location of this media
	 * 
	 * @return String representation of the title of this media
	 */
	public String getFileLocation() {
		// return this.fileLocation;
		return String.format("%s/music/%s", "/mnt/extSdCard", this.title);
	}

	/**
	 * Store the artist in case they are not yet saved
	 * 
	 * @return The rowid of the record stored in the Rootio database
	 */
	private Long persist() {
		String tableName = "media";
		ContentValues data = new ContentValues();
		data.put("title", this.title);
		data.put("wiki", this.wiki);
		data.put("filelocation", this.fileLocation);
		DBAgent dbagent = new DBAgent(this.context);
		return dbagent.saveData(tableName, null, data);
	}

	/**
	 * save the artists associated with this Media
	 */
	private void persistMediaArtists() {
		DBAgent dbagent = new DBAgent(this.context);
		for (Artist artist : this.artists) {
			ContentValues data = new ContentValues();
			String tableName = "mediaartist";
			data.put("mediaid", this.id);
			data.put("artistid", artist.getId());
			dbagent.saveData(tableName, null, data);
		}
	}

	/**
	 * save the genres to which this media belongs
	 */
	private void persistMediaGenres() {
		DBAgent dbagent = new DBAgent(this.context);
		for (Genre genre : this.genres) {
			ContentValues data = new ContentValues();
			String tableName = "mediagenre";
			data.put("mediaid", this.id);
			data.put("genreid", genre.getId());
			dbagent.saveData(tableName, null, data);
		}
	}

}
