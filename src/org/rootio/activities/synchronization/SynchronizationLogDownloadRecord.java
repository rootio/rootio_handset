package org.rootio.activities.synchronization;

import java.util.Date;

import org.rootio.tools.utils.Utils;

public class SynchronizationLogDownloadRecord {

	private Date changeDate;
	private int downloadStatusId;
	private int changeTypeId;
	private int changeId;
	
	SynchronizationLogDownloadRecord(String changeId, String changeTypeId, String changeDate, String downloadStatusId)
	{
		this.changeDate = Utils.getDateFromString(changeDate, "yyyy-MM-dd HH:mm:ss");
		this.downloadStatusId = Utils.parseIntFromString(downloadStatusId);
		this.changeTypeId = Utils.parseIntFromString(changeTypeId);
		this.changeId = Utils.parseIntFromString(changeId);
		
    }
	
	/**
	 * Gets the date when the record was written
	 * @return Date object representing the date when the record was persisted
	 */
	public Date getChangeDate()
	{
		return this.changeDate;
	}
	
	/**
	 * Gets the integer ID of this record
	 * @return integer representing database ID of this record as persisted in the database
	 */
	public int getChangeId()
	{
		return this.changeId;
	}
	
	/**
	 * Gets the ID of the status of this download
	 * @return Integer representing the status of this download 
	 */
	public int getDownloadStatusId()
	{
		return this.downloadStatusId;
	}
	
	/**
	 * Gets the ID of the type of change that this synchronization record was
	 * @return Integer representing the type of change that this download is
	 */
	public int getChangeTypeId()
	{
		return this.changeTypeId;
	}
}

