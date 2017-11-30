package org.rootio.tools.utils;

import android.app.PendingIntent;

public class NotificationAction {

    private int iconId;
    private String text;
    private PendingIntent pendingIntent;

    NotificationAction(int iconId, String text, PendingIntent pendingIntent) {
        this.iconId = iconId;
        this.text = text;
        this.pendingIntent = pendingIntent;
    }

    /**
     * Gets the title of this Notification action
     *
     * @return Title of the notification action
     */
    public String getTitle() {
        return this.text;
    }

    /**
     * Gets the ID of the icon associated with this notification action
     *
     * @return Integer ID of the resource that is the icon for this notification
     */
    public int getIconId() {
        return this.iconId;
    }

    /**
     * Gets the pending intent associated with this Notification Action
     *
     * @return The pending intent associated with this notification
     */
    public PendingIntent getPendingIntent() {
        return this.pendingIntent;
    }
}
