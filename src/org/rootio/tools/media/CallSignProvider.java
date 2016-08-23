package org.rootio.tools.media;

import java.util.HashSet;
import java.util.Iterator;

import org.rootio.tools.persistence.DBAgent;

import android.content.Context;

public class CallSignProvider implements Runnable {

	private final Context parent;
	private final PlayList playlist;
	private final HashSet<Media> callSigns;
	Iterator<Media> mediaIterator;
	private boolean isRunning;

	CallSignProvider(Context parent, PlayList playlist) {
		this.parent = parent;
		this.playlist = playlist;
		this.callSigns = new HashSet<Media>();
		this.isRunning = false;
	}

	private void loadCallSigns() {
		HashSet<Genre> genres = new HashSet<Genre>();
		HashSet<Artist> artists = new HashSet<Artist>();
		HashSet<String> tags = new HashSet<String>();
		String query = "select media.title, media.filelocation,media.wiki, genre.title, genre.id, artist.name, artist.id, artist.wiki, country.title, mediatag.tag from media left outer join mediagenre on media.id = mediagenre.mediaid left outer join genre on mediagenre.genreid = genre.id left outer join mediaartist on media.id = mediaartist.mediaid left outer join artist on mediaartist.artistid = artist.id left outer join country on artist.countryid = country.id join mediatag on media.id = mediatag.mediaid where mediatag.tag = ?";
		String[] args = new String[] { "callsign" };
		DBAgent dbagent = new DBAgent(this.parent);
		String[][] data = dbagent.getData(query, args);
		for (int i = 0; i < data.length; i++) {
			genres.add(new Genre(this.parent, data[i][3]));
			artists.add(new Artist(this.parent, data[i][5], data[i][7], data[i][8]));
			tags.add(data[i][8]);
			if (i == 0 || !data[i][0].equals(data[i - 1][0])) {
				callSigns.add(new Media(this.parent, data[i][1], data[i][0], genres.toArray(new Genre[genres.size()]), tags.toArray(new String[tags.size()]), artists.toArray(new Artist[artists.size()])));
				artists.clear();
				genres.clear();
				tags.clear();
			}
		}
	}

	@Override
	public void run() {
		this.isRunning = true;
		this.loadCallSigns();

		this.mediaIterator = callSigns.iterator();
		while (this.isRunning) {
			try {
				this.playCallSign();
				Thread.sleep(1200000);// 20 mins				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	void stop() {
		this.isRunning = false;
	}

	private void playCallSign() {
		this.playlist.onReceiveCallSign("/mnt/extSdCard/callsign/jingle.mp3");
		/*if (mediaIterator.hasNext()) {
			this.playlist.onReceiveCallSign(String.format("/mnt/extSdCard/callsign/%s", mediaIterator.next().getTitle()));
		} else {
			if (callSigns.size() > 0) {
				mediaIterator = callSigns.iterator(); // reset the iterator to 0
				this.playlist.onReceiveCallSign(String.format("/mnt/extSdCard/callsign/%s", mediaIterator.next().getTitle()));
			}
		}*/
	}

	public void start() {
		new Thread(this).start();

	}
}
