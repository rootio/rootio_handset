package org.rootio.tools.media;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.radio.TimeSpan;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;

public class Program implements Runnable {

	private String title;
	private PlayList playList;
	private TimeSpan timeSpan;
	private ProgramType programType;
	private Long id;

	public Program(String title, TimeSpan timeSpan) {
		this.programType = ProgramType.Call;
		this.title = title;
		this.timeSpan = timeSpan;
		this.id = Utils.getProgramId(title, this.programType.ordinal());
		if(this.id == null)
		{
			this.id = this.persist();
		}
	}

	public Program(String title, TimeSpan timeSpan, String tag) {
		this.programType = ProgramType.Media;
		this.title = title;
		this.playList = new PlayList(tag);
		this.timeSpan = timeSpan;
		this.id = Utils.getProgramId(title, this.programType.ordinal());
		if(this.id == null)
		{
			this.id = this.persist();
		}
	}

	/**
	 * Get the playlist associated with this Program
	 * @return PlayList object of this program's playlist
	 */
	public PlayList getPlayList() {
		return this.playList;
	}

	/**
	 * Get the program type of this program
	 * @return ProgramType object of this program's type
	 */
	public ProgramType getProgramType() {
		return this.programType;
	}
	
	/**
	 * Returns the title of this program
	 * @return String representation of the title of this program
	 */
	public String getTitle()
	{
		return this.title;
	}

	/**
	 * 
	 */
	public void run() {
		if (this.programType == ProgramType.Call) {
			Utils.logOnScreen("Waiting for call in...");
		} else {
			Utils.logOnScreen("Preparing playlist for the show "+this.title);
			playList.load();
			playList.play();
		}
	}
	
	/**
	 * Save this Program to the Rootio Database in case it is not yet persisted
	 * @return Long id of the row stored in the Rootio database
	 */
	private Long persist()
	{
		String tableName = "program";
		ContentValues data = new ContentValues();
		data.put("title", this.title);
		data.put("programtypeid", this.programType.ordinal());
		data.put("tag", this.playList.getTag());
		DBAgent agent = new DBAgent();
		return agent.saveData(tableName, null, data);
	}
}
