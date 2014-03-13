package org.rootio.tools.media;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;

/**
 * Class for the definition of Playlists
 * 
 * @author UTL051109
 * 
 */
public class PlayList implements OnCompletionListener {

	private ProgramType programType;
	private String tag;
	private HashSet<Media> mediaList;
	private Uri streamUrl;
	private Iterator<Media> mediaIterator;
	private MediaPlayer mediaPlayer;
	private Context parent;

	/**
	 * Constructor for the playlist class
	 * 
	 * @param tag
	 *            The tag to be used to construct the playlist
	 */
	public PlayList(Context parent, String tag, ProgramType programType) {
		this.tag = tag;
		this.parent = parent;
		this.programType = programType;
	}

	/**
	 * Load media for this playlist from the database
	 */
	public void load() {
		Utils.toastOnScreen("program type is "+ this.programType);
		if(this.programType == ProgramType.Media)
		{
		mediaList = loadMedia(this.tag);
		mediaIterator = mediaList.iterator();
		}
		if(this.programType == ProgramType.Stream)
		{
			String url  = this.getStreamingUrl();
			this.streamUrl = Uri.parse(url);
			Utils.toastOnScreen("tuning in to "+this.streamUrl);
		}
	}

	/**
	 * Play the media loaded in this playlist
	 */
	public void play() {
		
		try {
			if(this.programType == ProgramType.Media)
			{
			if (mediaIterator.hasNext()) {
				Media media = mediaIterator.next();
				mediaPlayer = MediaPlayer.create(this.parent,
						Uri.fromFile(new File(media.getFileLocation())));
				if (mediaPlayer != null) {
					mediaPlayer.setOnCompletionListener(this);
					mediaPlayer.start();
				} else {
					this.play();
				}
			}
			}
			else if(this.programType == ProgramType.Stream)
			{
				mediaPlayer = MediaPlayer.create(this.parent, this.streamUrl);
				mediaPlayer.start();
			}

		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			Utils.toastOnScreen("Illegal state");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Utils.toastOnScreen(e.getMessage());
			e.printStackTrace();
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
				mediaPlayer = null;
			} catch (Exception ex) {
				// medi player not in stoppable state
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

		mediaPlayer.start();
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
		;
		HashSet<Artist> artists = new HashSet<Artist>();
		HashSet<String> tags = new HashSet<String>();
		String query = "select media.title, media.filelocation,media.wiki, genre.title, genre.id, artist.name, artist.id, artist.wiki, country.title, mediatag.tag from media left outer join mediagenre on media.id = mediagenre.mediaid left outer join genre on mediagenre.genreid = genre.id join mediaartist on media.id = mediaartist.mediaid join artist on mediaartist.artistid = artist.id join country on artist.countryid = country.id join mediatag on media.id = mediatag.mediaid where mediatag.tag = ?";
		String[] args = new String[] { tag };
		DBAgent dbagent = new DBAgent(this.parent);
		String[][] data = dbagent.getData(query, args);
		HashSet<Media> media = new HashSet<Media>();
		for (int i = 0; i < data.length; i++) {
			genres.add(new Genre(this.parent, data[i][3]));
			artists.add(new Artist(this.parent, data[i][5], data[i][7],
					data[i][8]));
			tags.add(data[i][8]);
			if (i == 0 || !data[i][0].equals(data[i - 1][0])) {
				media.add(new Media(this.parent, data[i][1], data[i][0], genres
						.toArray(new Genre[genres.size()]), tags
						.toArray(new String[tags.size()]), artists
						.toArray(new Artist[artists.size()])));
				artists.clear();
				genres.clear();
				tags.clear();
			}
		}

		return media;
	}
	
	private String getStreamingUrl()
	{
		String tableName = "streamingConfiguration";
		String[] columns = new String[]{"ipaddress", "port", "path"	};
		String orderBy = "id desc";
		String limit = "1";
		DBAgent dbAgent = new DBAgent(this.parent);
		String result[][] = dbAgent.getData(true, tableName, columns, null, null, null, null, orderBy, limit);
		return result.length > 0? String.format("http://%s:%s/%s", result[0][0], result[0][1], result[0][2]): null;
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
	public String getTag() {
		return this.tag;
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		mediaPlayer.release();
		this.play();

	}

}
