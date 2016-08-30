package org.rootio.activities.synchronization;

import java.util.Date;

import org.rootio.tools.utils.Utils;

public class SynchronizationRecord {

	private Date synchronizationDate;
	private int HTTPresponseCode;
	private int changeCount;
	private int id;

	SynchronizationRecord(String id, String synchronizationDateString, String HTTPResponseCodeString, String changeCountString) {
		this.HTTPresponseCode = Utils.parseIntFromString(HTTPResponseCodeString);
		this.changeCount = Utils.parseIntFromString(changeCountString);
		this.synchronizationDate = Utils.getDateFromString(synchronizationDateString, "yyyy-MM-dd HH:mm:ss");
		this.id = Utils.parseIntFromString(id);
	}

	/**
	 * Gets the HTTP response code that the request for this synchronization
	 * received from the server
	 * 
	 * @return Integer representing HTTP response code received from cloud
	 *         server
	 */
	int getHTTPResponseCode() {
		return this.HTTPresponseCode;
	}

	/**
	 * Gets the ID of this synchronization record
	 * 
	 * @return Integer representing the ID of this synchronization record
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Gets the number of changes that are involved in this particular
	 * synchronization
	 * 
	 * @return Integer representing the number of changes in this
	 *         synchronization
	 */
	int getChangeCount() {
		return this.changeCount;
	}

	/**
	 * Gets the date on which this synchronization record was persisted
	 * 
	 * @return Date object representing date when this record was written in the
	 *         database
	 */
	Date getSynchronizationDate() {
		return this.synchronizationDate;
	}
}
