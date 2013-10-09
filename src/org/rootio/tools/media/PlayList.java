package org.rootio.tools.media;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import android.media.MediaPlayer;


/**
 * Class for the definition of Playlists
 * 
 * @author UTL051109
 * 
 */
public class PlayList {
	private String tag;
	private Media[] mediaList;
	private MediaPlayer mediaPlayer;

	/**
	 * Constructor for the playlist class
	 * 
	 * @param tag
	 *            The tag to be used to construct the playlist
	 */
	public PlayList(String tag) {
		this.tag = tag;

	}

	/**
	 * Load media for this playlist from the database
	 */
	public void load() {
		mediaList = loadMedia(this.tag);

	}

	/**
	 * Play the media loaded in this playlist
	 */
	public void play() {
		try {
			mediaPlayer = new MediaPlayer();
			boolean isPrepared = false;
			for (Media media : mediaList) {
				Utils.logOnScreen("Playing Media " + media.getFileLocation());
				
				if (!isPrepared) {
					mediaPlayer.setDataSource(media.getFileLocation());
					mediaPlayer.prepare();
					
					isPrepared = true;
				}
				else
				{
					mediaPlayer.reset();
					mediaPlayer.setDataSource(media.getFileLocation());
					mediaPlayer.prepare();
				}
				mediaPlayer.start();
				try {
					Thread.sleep(mediaPlayer.getDuration());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			Utils.logOnScreen("Illegal state");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Utils.logOnScreen("IOException");
			e.printStackTrace();
		}
	}

	/**
	 * Stops the media player and disposes it.
	 */
	public void stop() {
		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;
	}

	/**
	 * Pauses the currently playing media
	 */
	public void pause() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}

	/**
	 * Resumes playback after it has been paused
	 */
	public void resume() {

		mediaPlayer.start();
	}

	/**
	 * Loads media with the specified tag into the playlist
	 * 
	 * @param tag
	 *            The tag to be matched for media to be loaded into the playlist
	 * @return Array of Media objects matching specified tag
	 */
	private Media[] loadMedia(String tag) {
		HashSet<Genre> genres = new HashSet<Genre>();
		;
		HashSet<Artist> artists = new HashSet<Artist>();
		HashSet<String> tags = new HashSet<String>();
		String query = "select media.title, media.filelocation,media.wiki, genre.title, genre.id, artist.name, artist.id, artist.wiki, country.title, mediatag.tag from media left outer join mediagenre on media.id = mediagenre.mediaid join genre on mediagenre.genreid = genre.id join mediaartist on media.id = mediaartist.mediaid join artist on mediaartist.artistid = artist.id join country on artist.countryid = country.id join mediatag on media.id = mediatag.mediaid where mediatag.tag = ?";
		String[] args = new String[] { tag };
		DBAgent dbagent = new DBAgent();
		String[][] data = dbagent.getData(query, args);
		ArrayList<Media> media = new ArrayList<Media>();
		for (int i = 0; i < data.length; i++) {
			genres.add(new Genre(data[i][3]));
			artists.add(new Artist(data[i][5], data[i][7], data[i][8]));
			tags.add(data[i][8]);
			if (i == 0 || data[i][0] != data[i - 1][0]) {
				media.add(new Media(data[i][1], data[i][0], genres
						.toArray(new Genre[genres.size()]), tags
						.toArray(new String[tags.size()]), artists
						.toArray(new Artist[artists.size()])));
				Utils.logOnScreen("added media " + data[i][0]);
			}
		}

		return media.toArray(new Media[media.size()]);
	}

	/**
	 * Gets the media currently loaded in this playlist
	 * 
	 * @return Array of Media objects loaded in this playlist
	 */
	public Media[] getMedia() {
		return this.mediaList;
	}

	/**
	 * Gets the tag used to construct this playlist
	 * 
	 * @return String representation of the tag used to construct this playlist
	 */
	public String getTag() {
		return this.tag;
	}

}
