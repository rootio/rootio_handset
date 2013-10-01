package org.rootio.radioClient;

import java.util.Date;

import org.rootio.tools.utils.DayOfWeek;

public class EventTime {
	private DayOfWeek dayOfWeek;
	private String startTime;
	private int duration;

	public EventTime() {

	}
	
	public DayOfWeek getDayOfWeek()
	{
		return this.dayOfWeek;
	}
	
	private String getStartTime()
	{
		return this.startTime;
	}
	
	private int getDuration()
	{
		return this.duration;
	}
}
