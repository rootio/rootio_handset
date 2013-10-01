package org.rootio.tools.radio;

import java.util.Date;

import org.rootio.radioClient.EventTime;

public class TimeSpan {
	private Date startDate;
	private Date endDate;
	private EventTime[] eventTimes;
	private boolean isRepeating;

	public TimeSpan() {

	}

	public Date getStartDate() {
		return this.startDate;
	}
	
	public Date getEndDate()
	{
		return this.endDate;
	}
	
	public EventTime[] getEventTimes()
	{
		return this.eventTimes;
	}
}
