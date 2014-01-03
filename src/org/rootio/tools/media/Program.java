package org.rootio.tools.media;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.radio.TimeSpan;
import org.rootio.tools.utils.LogType;
import org.rootio.tools.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;

public class Program implements Runnable {

	private String title;
	private PlayList playList;
	private TimeSpan timeSpan;
	private ProgramType programType;
	private Long id;
	BroadcastReceiver broadcastReceiver;
	private Context parent;

	public Program(Context parent, String title, TimeSpan timeSpan) {
		this.parent = parent;
		this.programType = ProgramType.Call;
		this.title = title;
		this.timeSpan = timeSpan;
		this.id = Utils.getProgramId(title, this.programType.ordinal());
		if (this.id == null) {
			this.id = this.persist();
		}
	}

	public Program(Context parent, String title, TimeSpan timeSpan, String tag) {
		this.parent = parent;
		this.programType = ProgramType.Media;
		this.title = title;
		this.playList = new PlayList(this.parent, tag);
		this.timeSpan = timeSpan;
		this.id = Utils.getProgramId(title, this.programType.ordinal());
		if (this.id == null) {
			this.id = this.persist();
		}
	}

	/**
	 * Get the playlist associated with this Program
	 * 
	 * @return PlayList object of this program's playlist
	 */
	public PlayList getPlayList() {
		return this.playList;
	}

	/**
	 * Get the program type of this program
	 * 
	 * @return ProgramType object of this program's type
	 */
	public ProgramType getProgramType() {
		return this.programType;
	}

	/**
	 * Returns the title of this program
	 * 
	 * @return String representation of the title of this program
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * 
	 */
	public void run() {
		if (this.programType == ProgramType.Call) {
			Utils.logOnScreen("Waiting for call in...", LogType.Radio);
		} else {
			Utils.logOnScreen("Preparing playlist for the show " + this.title,
					LogType.Radio);
			playList.load();
			playList.play();
		}
	}
	
	public void pause()
	{
		playList.pause();
	}
	
	public void resume()
	{
		playList.resume();
	}

	public void stop() {
		playList.stop();
	}

	/**
	 * Gets the timespan associated with this show
	 * 
	 * @return The TimeSpan object associated with this show.
	 */
	public TimeSpan getTimeSpan() {
		return this.timeSpan;
	}

	/**
	 * Save this Program to the Rootio Database in case it is not yet persisted
	 * 
	 * @return Long id of the row stored in the Rootio database
	 */
	private Long persist() {
		String tableName = "program";
		ContentValues data = new ContentValues();
		data.put("title", this.title);
		data.put("programtypeid", this.programType.ordinal());
		data.put("tag", this.playList.getTag());
		DBAgent agent = new DBAgent(this.parent);
		return agent.saveData(tableName, null, data);
	}
}
