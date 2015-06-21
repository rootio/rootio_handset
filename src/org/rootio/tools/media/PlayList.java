package org.rootio.tools.media;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import org.rootio.radioClient.R;
import org.rootio.tools.media.ProgramManager.ProgramActionType;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.util.Log;

/**
 * Class for the definition of Playlists
 * 
 * @author Jude Mukundane
 * 
 */
public class PlayList implements OnCompletionListener, OnPreparedListener, OnErrorListener {

	private final ProgramActionType programActionType;
	private final String argument;
	private HashSet<Media> mediaList;
	private Uri streamUrl;
	private Iterator<Media> mediaIterator;
	private MediaPlayer mediaPlayer;
	private MediaPlayer callSignPlayer;
	private final CallSignProvider callSignProvider;
	private final Context parent;
	private Media currentMedia;
	private int mediaPosition;

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
		this.callSignProvider = new CallSignProvider(this.parent, this);
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
		startPlayer();
		this.callSignProvider.start();
	}

	private void startPlayer() {

		AudioManager audioManager = (AudioManager) this.parent.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 2, AudioManager.FLAG_SHOW_UI);

		try {
			if (this.programActionType == ProgramActionType.Media || this.programActionType == ProgramActionType.Music) {
				if (mediaIterator.hasNext()) {
					currentMedia = mediaIterator.next();
					try {
						mediaPlayer = new MediaPlayer();
						mediaPlayer.setDataSource(this.parent, Uri.fromFile(new File(currentMedia.getFileLocation())));
						Utils.toastOnScreen("media player created for " + currentMedia.getFileLocation());
						mediaPlayer.setOnPreparedListener(this);
						mediaPlayer.setOnCompletionListener(this);
						mediaPlayer.setOnErrorListener(this);
						mediaPlayer.prepareAsync();

					} catch (Exception ex) {
						Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.startPlayer)" : ex.getMessage());
						this.onCompletion(mediaPlayer);
					}
				} else {
					if (mediaList.size() > 0) // reload playlist if only ther
												// were some songs in it
					{
						this.load();
						this.startPlayer();
					}
				}
			} else if (this.programActionType == ProgramActionType.Stream) {
				mediaPlayer = MediaPlayer.create(this.parent, this.streamUrl);
				mediaPlayer.start();
			}

		} catch (IllegalStateException ex) {
			Utils.toastOnScreen(ex.getMessage());
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage());
		} catch (Exception ex) {
			Utils.toastOnScreen(ex.getMessage());
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.play)" : ex.getMessage());
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// check that a callsign is not playing before upping
		// the volume
		try {
			if (this.callSignPlayer != null || this.callSignPlayer.isPlaying()) {
				this.mediaPlayer.setVolume(0.07f, 0.07f);
			} else {
				this.mediaPlayer.setVolume(1f, 1f);
			}

		} catch (Exception ex) {
			this.mediaPlayer.setVolume(1f, 1f);
		}
		mediaPlayer.start();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Stops the media player and disposes it.
	 */
	public void stop() {
		this.callSignProvider.stop();
		if (this.callSignPlayer != null)
			try {
				this.callSignPlayer.stop();
				this.callSignPlayer.release();
			} catch (Exception ex) {
				Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(PlayList.stop)" : ex.getMessage());
			}

		if (mediaPlayer != null) {
			try {
				Utils.toastOnScreen("Stopping media player in " + this.argument);
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
		try {
			if (mediaPlayer.isPlaying()) {
				this.mediaPosition = this.mediaPlayer.getCurrentPosition();
				mediaPlayer.pause();

				this.callSignPlayer.release();
				this.callSignProvider.stop();
			}
		} catch (Exception ex) {
			// investiate tis
		}
	}

	/**
	 * Resumes playback after it has been paused
	 */
	public void resume() {
		try {
			// raise the volume Android levels it after phone call
			AudioManager audioManager = (AudioManager) this.parent.getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 2, AudioManager.FLAG_SHOW_UI);

			// mediaPlayer.start(); //works fine on Galaxy grand duos (4.2.2),
			// fails
			// on Galaxy pocket (4.0.2) because Media player is reclaimed by
			// system
			this.mediaPlayer = MediaPlayer.create(this.parent, Uri.fromFile(new File(this.currentMedia.getFileLocation())));
			this.mediaPlayer.setOnCompletionListener(this);
			this.mediaPlayer.seekTo(mediaPosition);
			this.mediaPlayer.start();

			// resume the callSign provider
			this.callSignProvider.start();
		} catch (Exception ex) {
			// investiate tois
		}
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

		String query = "select media.title, media.filelocation,media.wiki, genre.title, genre.id, artist.name, artist.id, artist.wiki, country.title, mediatag.tag from media left outer join mediagenre on media.id = mediagenre.mediaid left outer join genre on mediagenre.genreid = genre.id left outer join mediaartist on media.id = mediaartist.mediaid left outer join artist on mediaartist.artistid = artist.id left outer join country on artist.countryid = country.id join mediatag on media.id = mediatag.mediaid where mediatag.tag = ?";
		String[] args = new String[] { tag };
		if (argument.equals("random")) {
			query = "select media.title, media.filelocation,media.wiki, genre.title, genre.id, artist.name, artist.id, artist.wiki, country.title, mediatag.tag from media left outer join mediagenre on media.id = mediagenre.mediaid left outer join genre on mediagenre.genreid = genre.id left outer join mediaartist on media.id = mediaartist.mediaid left outer join artist on mediaartist.artistid = artist.id left outer join country on artist.countryid = country.id left outer join mediatag on media.id = mediatag.mediaid";
			args = new String[] {};
		}

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

	void onReceiveCallSign(String Url) {

		Utils.toastOnScreen("playing " + Url);
		try {
			if (this.mediaPlayer.isPlaying()) {
				try {
					callSignPlayer = MediaPlayer.create(this.parent, Uri.fromFile(new File(Url)));
					if (callSignPlayer == null) {
						return;
					}
				} catch (Exception ex) {
					Utils.toastOnScreen(ex.getMessage());
					return;
				}

				this.mediaPlayer.setVolume(0.07f, 0.07f);
				callSignPlayer.setVolume(1.0f, 1.0f);
				callSignPlayer.start();
				callSignPlayer.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer arg0) {
						try {
							PlayList.this.mediaPlayer.setVolume(1.0f, 1.0f);
							callSignPlayer.release();
							callSignPlayer = null;
						} catch (Exception ex) {
							// claims callsign player is null
						}
					}
				});
			}
		} catch (Exception ex) {
			// investigate this
		}

	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		try {

			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		} catch (Exception ex) {

		}
		this.startPlayer();
	}

}
