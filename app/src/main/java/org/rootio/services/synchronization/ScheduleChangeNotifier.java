package org.rootio.services.synchronization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.rootio.services.RadioService;

public class ScheduleChangeNotifier extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent programIntent = new Intent(context, RadioService.class);
        context.stopService(programIntent);
        //sleep for 5, probably fading out
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        context.startService(programIntent);
    }
}
