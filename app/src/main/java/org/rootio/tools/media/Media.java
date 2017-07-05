package org.rootio.tools.media;

public class Media {
	private final String title;
	private final String fileLocation;
	private long duration;
	private final String artists;

	public Media(String title, String fileLocation, long duration, String artists) {
		this.fileLocation = fileLocation;
		this.title = title;
		this.duration = duration;
		this.artists = artists;
	}

	/**
	 * Gets the artists associated with this media
	 * 
	 * @return Array of Artist objects
	 */
	public String getArtists() {
		return this.artists;
	}

	/**
	 * Gets the duration of this media in seconds
	 * 
	 * @return Integer representing the duration of this media
	 */
	public long getDuration() {
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
		return this.fileLocation;
		// return String.format("%s/music/%s", "/mnt/extSdCard", this.title);
	}

}
