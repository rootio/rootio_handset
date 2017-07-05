package org.rootio.tools.media;

import org.rootio.tools.persistence.DBAgent;

import android.content.ContentValues;
import android.content.Context;

public class EpisodeManager {

	private Context parent;
	private int episodeNumber;
	private Program program;
	
	EpisodeManager(Context parent, Program program)
	{
		this.parent = parent;
		this.program = program;
	}
	
	/**
	 * Gets the string identifying the name of the episode to be aired
	 * @return The file name of the episode to be played
	 */
	String getEpisodeTag()
	{
		this.episodeNumber = this.getEpisodeNumber();
		return String.format("%s_%s", this.program.getId(), this.episodeNumber);
	}
	
	/**
	 * Gets the serial number for this episode
	 * @return The serial number for this episode
	 */
	private int getEpisodeNumber()
	{
	    int maxEpisode = this.getMaxEpisode();
	    int scheduledIndex = this.program.getScheduledIndex();
	    boolean isRepeat = this.program.getEventTimes()[scheduledIndex].isRepeat();
	    maxEpisode = isRepeat?maxEpisode : maxEpisode + 1;
	    this.logThisEpisode(this.program.getId(), this.program.getEventTimes()[scheduledIndex].getId(), maxEpisode);
	    if(isRepeat)
	    {
	    	return maxEpisode;
	    }
	    else
	    {
	    	return maxEpisode + 1;
	    }
	    
	}
	
	/**
	 * Logs the airing of this episode
	 * @param programId The ID of the Program with which this episode is associated
	 * @param eventtimeid The ID of the event time with which this program is associated
	 * @param episodeNumber The serial number of this episode
	 */
	private void logThisEpisode(long programId, long eventtimeid, int episodeNumber)
	{
		String tableName = "programlog";
		ContentValues data = new ContentValues();
		data.put("programid", this.program.getId());
		data.put("eventtimeid", eventtimeid);
		data.put("episode", episodeNumber);
		DBAgent dbAgent = new DBAgent(this.parent);
		dbAgent.saveData(tableName, null, data);
	}
	
	/**
	 * Gets the last episode to be played for this program
	 * @return Episode number of last episode
	 */
	private int getMaxEpisode()
	{
		String query = "select coalesce(max(episode), 0) from programlog join eventtime on programlog.eventtimeid = eventtime.id where programlog.programid = ? and isrepeat = 0";
		String[] whereArgs = new String[]{String.valueOf(this.program.getId())};
		DBAgent agent = new DBAgent(this.parent);
		String[][] results = agent.getData(query, whereArgs);
		return Integer.parseInt(results[0][0]);
	}
	
	
}
