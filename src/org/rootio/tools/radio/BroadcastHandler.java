package org.rootio.tools.radio;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadcastHandler extends BroadcastReceiver implements Runnable {

	private RadioRunner radioRunner;
	private int currentIndex;

	public BroadcastHandler(RadioRunner radioRunner) {
		this.radioRunner = radioRunner;
	}

	@Override
	public void run() {
		ProgramSlot programSlot = radioRunner.getProgramSlots().get(currentIndex);
		EventTime eventTime = programSlot.getProgram().getEventTimes()[programSlot.getScheduledIndex()];
		if (!isExpired(eventTime)) {
			     this.radioRunner.runProgram(currentIndex);
			}
		else
		{
			this.radioRunner.getProgramSlots().get(currentIndex).setFinishedRunning();
		}
	}

	@Override
	public void onReceive(Context c, Intent i) {
		String possibleIndex = i.getAction();
		if(Integer.parseInt(possibleIndex) == currentIndex)
		{
			return;
		}
		try {
			currentIndex = Integer.parseInt(possibleIndex);
			new Thread(this).start();
		} catch (NumberFormatException ex) {

		}
	}

	private boolean isExpired(EventTime eventTime) {

		Calendar referenceCalendar = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		cal.setTime(eventTime.getScheduledDate());
		cal.add(Calendar.MINUTE, eventTime.getDuration());
		return cal.compareTo(referenceCalendar) <= 0;

	}

}
