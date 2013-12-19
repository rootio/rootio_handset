package org.rootio.tools.radio;

import java.io.Serializable;
import java.util.Date;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.text.format.DateFormat;

public class TimeSpan implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private Date startDate;
	private Date endDate;
	private EventTime[] eventTimes;
	private boolean isRepeating;
	private Long id;

	public TimeSpan(Date startDate, Date endDate, EventTime[] eventTimes,
			boolean isRepeating) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.eventTimes = eventTimes;
		this.isRepeating = isRepeating;
		this.id = Utils.getTimeSpanId(name, startDate, endDate);
		if (this.id == null) {
			this.id = this.persist();
			this.persistTimeSpanEventTimes();
		}
	}

	/**
	 * Get the start date of this Timespan object
	 * 
	 * @return Date representation of the start date
	 */
	public Date getStartDate() {
		return this.startDate;
	}

	/**
	 * Get the end date of this Timespan object of this Timespan object
	 * 
	 * @return Date representation of the end date of this Timespan object
	 */
	public Date getEndDate() {
		return this.endDate;
	}

	/**
	 * Return the EventTime objects associated with this Timespan
	 * 
	 * @return Array of EventTime objects
	 */
	public EventTime[] getEventTimes() {
		return this.eventTimes;
	}

	/**
	 * Save this TimeSpan object to the Rootio Database in case it is not yet
	 * saved
	 * 
	 * @return Long id of the row stored in the Rootio Database
	 */
	private Long persist() {
		String tableName = "timespan";
		ContentValues data = new ContentValues();
		data.put("startdate",
				(String) DateFormat.format("yyyyMMdd", this.startDate));
		data.put("enddate",
				(String) DateFormat.format("yyyyMMdd", this.endDate));
		data.put("isrepeating", this.isRepeating);
		DBAgent agent = new DBAgent();
		return agent.saveData(tableName, null, data);
	}

	/**
	 * Save the relationship between the Timespan and its constituent EventTime
	 * objects
	 */
	private void persistTimeSpanEventTimes() {
		String tableName = "timespaneventtimes";
		DBAgent agent = new DBAgent();
		for (EventTime eventTime : this.eventTimes) {
			ContentValues data = new ContentValues();
			data.put("timespanid", this.id);
			data.put("eventtimeid", eventTime.getId());
			agent.saveData(tableName, null, data);
		}
	}
}
