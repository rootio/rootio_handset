package org.rootio.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.rootio.tools.utils.Utils;

public class MountListener extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equalsIgnoreCase(Intent.ACTION_MEDIA_REMOVED)
                || action.equalsIgnoreCase(Intent.ACTION_MEDIA_UNMOUNTED)
                || action.equalsIgnoreCase(Intent.ACTION_MEDIA_BAD_REMOVAL)
                || action.equalsIgnoreCase(Intent.ACTION_MEDIA_EJECT)) {
            Utils.toastOnScreen("Media Unmounted!! ", context);
            Utils.logEvent(context, Utils.EventCategory.MEDIA, Utils.EventAction.UMOUNT,"storage");
        }
    }
}
