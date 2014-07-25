package org.rootio.tools.media;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import org.rootio.radioClient.R;
import org.rootio.tools.media.ProgramManager.ProgramActionType;
import org.rootio.tools.persistence.DBAgent;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.util.Log;

/**
 * Class for the definition of Playlists
 * 
 * @author Jude Mukundane
 * 
 */
public class PlayList implements OnCompletionListener {

	private final ProgramActionType programActionType;
	private final String argument;
	private HashSet<Media> mediaList;
	private Uri streamUrl;
	private Iterator<Media> mediaIterator;
	private MediaPlayer mediaPlayer;
	private final Context parent;

	/**
	 * Constructor for the playlist class
	 * 
	 * @param tag
	 *            The tag to be used to construct the playlist
	 */
	public PlayList(Context parent, String argument, ProgramActionType programActionType) {
		this.argument = argument;
		this.parent = parent;
		this.programActionType = programActionType;
	}

	/**
	 * Load media for this playlist from the database
	 */
	public void load() {
		if (this.programActionType == ProgramActionType.Media || this.programActionType == ProgramActionType.Music) {
			mediaList = loadMedia(this.argument);
			mediaIterator = mediaList.iterator();
		}
		if (this.programActionType == ProgramActionType.Stream) {
			String url = this.argument.isEmpty() ? this.getStreamingUrl() : this.argument;
			this.streamUrl = Uri.parse(url);
		}
	}

	/**
	 * Play the media loaded in this playlist
	 */
	public void play() {

		try {
			if (this.programActionType == ProgramActionType.Media || this.programActionType == ProgramActionType.Music) {
				if (mediaIterator.hasNext()) {
					Media media = mediaIterator.next();
					try {
						mediaPlayer = MediaPlayer.create(this.parent, Uri.fromFile(new File(media.getFileLocation())));
						mediaPlayer.setOnCompletionListener(this);
						mediaPlayer.start();

					} catch (Exception ex) {
						this.play();
					}
				}
			} else if (this.programActionType == ProgramActionType.Stream) {
				mediaPlayer = MediaPlayer.create(this.parent, this.streamUrl);
				mediaPlayer.start();
			}

		} catch (IllegalStateException ex) {
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage());
		} catch (Exception ex) {
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.stop)" : ex.getMessage());
		}
	}

	/**
	 * Stops the media player and disposes it.
	 */
	public void stop() {
		if (mediaPlayer != null) {
			try {
				mediaPlayer.stop();
				mediaPlayer.release();
			} catch (Exception ex) {
				Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.stop)" : ex.getMessage());
			}
		}
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
		// mediaPlayer.start(); //works fine on Galaxy grand duos (4.2.2), fails
		// on Galaxy pocket (4.0.2) because Media player is reclaimed by system
		this.play();
	}

	/**
	 * Loads media with the specified tag into the playlist
	 * 
	 * @param tag
	 *            The tag to be matched for media to be loaded into the playlist
	 * @return Array of Media objects matching specified tag
	 */
	private HashSet<Media> loadMedia(String tag) {
		HashSet<Genre> genres = new HashSet<Genre>();
		HashSet<Artist> artists = new HashSet<Artist>();
		HashSet<String> tags = new HashSet<String>();
		String query = "select media.title, media.filelocation,media.wiki, genre.title, genre.id, artist.name, artist.id, artist.wiki, country.title, mediatag.tag from media left outer join mediagenre on media.id = mediagenre.mediaid left outer join genre on mediagenre.genreid = genre.id join mediaartist on media.id = mediaartist.mediaid join artist on mediaartist.artistid = artist.id join country on artist.countryid = country.id join mediatag on media.id = mediatag.mediaid where mediatag.tag = ?";
		String[] args = new String[] { tag };
		DBAgent dbagent = new DBAgent(this.parent);
		String[][] data = dbagent.getData(query, args);
		HashSet<Media> media = new HashSet<Media>();
		for (int i = 0; i < data.length; i++) {
			genres.add(new Genre(this.parent, data[i][3]));
			artists.add(new Artist(this.parent, data[i][5], data[i][7], data[i][8]));
			tags.add(data[i][8]);
			if (i == 0 || !data[i][0].equals(data[i - 1][0])) {
				media.add(new Media(this.parent, data[i][1], data[i][0], genres.toArray(new Genre[genres.size()]), tags.toArray(new String[tags.size()]), artists.toArray(new Artist[artists.size()])));
				artists.clear();
				genres.clear();
				tags.clear();
			}
		}
		return media;
	}

	private String getStreamingUrl() {
		String tableName = "streamingConfiguration";
		String[] columns = new String[] { "ipaddress", "port", "path" };
		String orderBy = "id desc";
		String limit = "1";
		DBAgent dbAgent = new DBAgent(this.parent);
		String result[][] = dbAgent.getData(true, tableName, columns, null, null, null, null, orderBy, limit);
		return result.length > 0 ? String.format("http://%s:%s/%s", result[0][0], result[0][1], result[0][2]) : null;
	}

	/**
	 * Gets the media currently loaded in this playlist
	 * 
	 * @return Array of Media objects loaded in this playlist
	 */
	public HashSet<Media> getMedia() {
		return this.mediaList;
	}

	/**
	 * Gets the tag used to construct this playlist
	 * 
	 * @return String representation of the tag used to construct this playlist
	 */
	public String getArgument() {
		return this.argument;
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		mediaPlayer.release();
		this.play();

	}

}
