package org.rootio.tools.utils;

import android.app.PendingIntent;

public class NotificationAction {

	private int iconId;
	private String text;
	private PendingIntent pendingIntent;
	
	NotificationAction(int iconId, String text, PendingIntent pendingIntent)
	{
		this.iconId = iconId;
		this.text = text;
		this.pendingIntent = pendingIntent;
	}
	
	public String getTitle()
	{
		return this.text;
	}
	
	public int getIconId()
	{
		return this.iconId;
	}
	
	public PendingIntent getPendingIntent()
	{
		return this.pendingIntent;
	}
}
