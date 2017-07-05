package org.rootio.activities.synchronization;

import java.util.Date;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

public class SynchronizationConfiguration {

	private String title;
	private Date changeDate;
	private boolean syncDuringParticularTimes;
	private String syncStartTime;
	private String syncEndTime;
	private int unitId;
    private int quantity;
    private Context context;
	
	public SynchronizationConfiguration(Context context)
	{
		this.context = context;
		String[][] configuration = this.loadConfiguration();
		this.loadSavedState(configuration);
	}
	
	/**
	 * Loads the synchronization as retrieved from the database
	 * @param configuration An array of String arrays containing the configuration as loaded from the database.
	 */
	private void loadSavedState(String[][] configuration)
	{
		if(configuration.length > 0)
		{
		this.title = configuration[0][0];
		this.changeDate = Utils.getDateFromString(configuration[0][1], "yyyy-MM-dd HH:mm:ss");
		this.syncDuringParticularTimes = configuration[0][2].equals(new String("1")); 
		this.syncStartTime = configuration[0][3];
		this.syncEndTime = configuration[0][4];
		this.quantity = Utils.parseIntFromString(configuration[0][5]);
		this.unitId = Utils.parseIntFromString(configuration[0][6]);
		}
	}
	
	/**
	 * Fetches the synchronization frequency stored in the database
	 * @return An array of String Arrays each being a representation of a record in the database
	 */
	private String[][] loadConfiguration()
	{
		String tableName = "frequencyconfiguration";
		String[] columnsToReturn = new String[] {"title","changedate","syncduringparticulartime","syncstarttime","syncendtime","quantity","frequencyunitid"};
		String whereClause = "title = ?";
		String[] whereArgs = new String[] {"synchronization"};
		DBAgent agent = new DBAgent(this.context);
		return agent.getData(true, tableName, columnsToReturn, whereClause, whereArgs, null, null, null, null);
	}
	
	/**
	 * Gets the date when this configuration was last modified
	 * @return The date of last modification of this configuration
	 */
	public Date getChangeDate()
	{
		return this.changeDate;
	}
	
	/**
	 * Gets whether or not synchronizations should be limited to a given time frame
	 * @return Boolean of whether synchronizations are limited to a given time or not. True: limited, False: not limited
	 */
	public boolean syncDuringParticularTimes()
	{
		return this.syncDuringParticularTimes;
	}
	
	/**
	 * Gets the start time of the synchronization window
	 * @return String representation of the start time of the Sync window in HH:mm:ss format
	 */
	public String getSyncStartTime()
	{
		return this.syncStartTime;
	}
	
	/**
	 * Gets the end time of the synchronization window
	 * @return String representation of the end time of the Sync window in HH:mm:ss format
	 */
	public String getSyncEndTime()
	{
		return this.syncEndTime;
	}
	
	/**
	 * The ID of the unit used to define the frequency of Synchronization
	 * @return Integer representing the ID of the unit used to define the frequency of synchronization
	 */
	public int getUnitId()
	{
		return this.unitId;
	}
	
	/**
	 * Gets the quantity of time in specified units at which to do the synchronization
	 * @return Integer quantity of time units
	 */
	public int getQuantity()
	{
		return this.quantity;
	}
	
	/**
	 * Sets whether or not to synchronize in aparticular time window
	 * @param syncDuringParticularTimes Boolean specifying whether to sync during particular time window. True:yes, False:no
	 */
    public void setSyncDuringParticularTimes(boolean syncDuringParticularTimes)
    {
    	this.syncDuringParticularTimes = syncDuringParticularTimes;
    }
    
    /**
     * Sets the time at which the synchronization time window starts
     * @param syncStartTime String representation of the Synchronization start time in the format HH:mm:ss
     */
    public void setSyncStartTime(String syncStartTime)
    {
    	this.syncStartTime = syncStartTime;
    }
    
    /**
     * 
     * Sets the time at which the synchronization time window ends
     * @param syncEndTime String representation of the Synchronization end time in the format HH:mm:ss
     */
    public void setSyncEndTime(String syncEndTime)
    {
    	this.syncEndTime = syncEndTime;
    }
    
    /**
     * Sets the ID of the unit used to measure the duration of the synchronization intervals
     * @param unitId Integer ID of the unit used to measure the time interval between synchronizations
     */
    public void setUnitId(int unitId)
    {
    	this.unitId = unitId;
    }
    
    /**
     * Sets the quantity of the time units specifying the interval between synchronizations
     * @param quantity Integer representation of the number of time units between synchronizations
     */
    public void setQuantity(int quantity)
    {
    	this.quantity = quantity;
    }
    
    /**
     * Saves the frequency configuration details
     * @return integer number of the rows affected by this transaction in the database
     */
    public int save() {
		String tableName = "frequencyconfiguration";
		String whereClause = "title = ?";
		String[] whereArgs = new String[] { title };
		ContentValues data = new ContentValues();
		data.put("frequencyunitid", this.unitId);
		data.put("quantity", this.quantity);
		data.put("changedate",Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
		data.put("syncduringparticulartime", this.syncDuringParticularTimes);
		data.put("syncstarttime", this.syncStartTime);
		data.put("syncendtime", this.syncEndTime);
		DBAgent agent = new DBAgent(this.context);
		return agent.updateRecords(tableName, data, whereClause, whereArgs);
	}
}
