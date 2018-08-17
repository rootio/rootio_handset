package org.rootio.activities.launch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.rootio.tools.utils.Utils;

/**
 * This class listens for boot incidents and restores the services to the state
 * they were in before the phone shut down
 *
 * @author Jude Mukundane
 */
public class LaunchMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        Intent intent = new Intent(context, Utils.isConnectedToStation(context) ? LauncherActivity.class : SplashScreen.class);
        context.startActivity(intent);
    }
}
