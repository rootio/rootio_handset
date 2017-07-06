package org.rootio.tools.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.rootio.tools.media.ScheduleChangeNotifiable;

/**
 * Created by Jude Mukundane on 7/5/2017.
 */

public class ScheduleChangeBroadcastHandler extends BroadcastReceiver {
    private final ScheduleChangeNotifiable notifiable;
    private Integer currentIndex = null; // prevent initial assignment to 0

    public ScheduleChangeBroadcastHandler(ScheduleChangeNotifiable notifiable) {
        this.notifiable = notifiable;
    }


    @Override
    public void onReceive(Context c, Intent i) {
        this.notifiable.notifyScheduleChange();
    }
}
