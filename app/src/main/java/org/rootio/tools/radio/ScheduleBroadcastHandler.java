package org.rootio.tools.radio;

import org.rootio.tools.media.ScheduleNotifiable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduleBroadcastHandler extends BroadcastReceiver implements Runnable {

    private final ScheduleNotifiable notifiable;
    private Integer currentIndex = null; // prevent initial assignment to 0

    public ScheduleBroadcastHandler(ScheduleNotifiable notifiable) {
        this.notifiable = notifiable;
    }

    @Override
    public void run() {
        if (!this.notifiable.isExpired(currentIndex)) {
            this.notifiable.runProgram(currentIndex);
        }
    }

    @Override
    public void onReceive(Context c, Intent i) {
        Integer possibleIndex = i.getIntExtra("index", 0);
        if (possibleIndex == currentIndex) {
            return; // intents are thrown twice sometimes
        }
        if (this.currentIndex != null) {
            this.notifiable.stopProgram(currentIndex);
        }
        currentIndex = possibleIndex;
        new Thread(this).start();
    }


}
