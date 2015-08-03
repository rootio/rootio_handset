package org.rootio.tools.media;

import java.io.File;

import org.rootio.tools.persistence.DBAgent;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;

public class JingleManager implements OnCompletionListener {

	private final Program program;
	private final Context parent;
	private MediaPlayer mediaPlayer;

	JingleManager(Context parent, Program program) {
		this.parent = parent;
		this.program = program;
	}

	/**
	 * Gets the path of the Jingle for a specific program
	 * 
	 * @param programId
	 *            The ID of the program for which a jingle is sought
	 * @return The path to the audio file of the jingle for the specified
	 *         program
	 */
	private String getJingle(long programId) {
		String tableName = "jingles";
		String[] columns = new String[] { "filelocation" };
		String whereClause = "programid = ?";
		String[] whereArgs = new String[] { String.valueOf(programId) };
		String limit = "1";
		DBAgent dbAgent = new DBAgent(this.parent);
		String[][] results = dbAgent.getData(true, tableName, columns, whereClause, whereArgs, null, null, null, limit);
		return results.length > 0 ? results[0][0] : null;
	}

	/**
	 * Plays the jingle for the specified program and then launches the program
	 */
	void playJingle() {
		String jingleFile = this.getJingle(this.program.getId());
		if (jingleFile != null) {
			mediaPlayer = MediaPlayer.create(this.parent, Uri.fromFile(new File(String.format("/mnt/extSdCard/jingle/%s", jingleFile))));
			if (mediaPlayer != null) {
				mediaPlayer.setOnCompletionListener(this);
				mediaPlayer.start();
			}
		}
	}

	void play() {
		this.mediaPlayer.start();
	}

	void pause() {
		this.mediaPlayer.pause();
	}

	void stop() {
		if (this.mediaPlayer != null)
			this.mediaPlayer.stop();
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		//
	}

}
