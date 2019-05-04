package org.rootio.tools.radio;

import org.rootio.tools.media.ScheduleNotifiable;
import org.rootio.tools.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScheduleBroadcastHandler extends BroadcastReceiver implements Runnable {

    private final ScheduleNotifiable notifiable;
    private Integer currentIndex = -1; // prevent initial assignment to 0

    public ScheduleBroadcastHandler(ScheduleNotifiable notifiable) {
        this.notifiable = notifiable;
    }

    @Override
    public void run() {
        if (!this.notifiable.isExpired(currentIndex)) {
            Log.d("org.rootio.handset, ", "run: not expired!");
            this.notifiable.runProgram(currentIndex);
        }
        else
        {
            Log.d("org.rootio.handset, ", "run: expired!");
        }
    }

    @Override
    public void onReceive(Context c, Intent i) {

        Integer possibleIndex = i.getIntExtra("index", -1);
        //Utils.toastOnScreen("Launching program",c);
        if (possibleIndex <= currentIndex) { //sometimes two programs will be started at the same time
            return; // intents are thrown twice sometimes
        }
        currentIndex = possibleIndex;
        new Thread(this).start();
    }


}
