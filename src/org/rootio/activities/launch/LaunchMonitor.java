package org.rootio.activities.launch;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class listens for boot incidents and restores the services to the state
 * they were in before the phone shut down
 * 
 * @author Jude Mukundane
 * 
 */
public class LaunchMonitor extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		Intent intent = new Intent(context,  new File(context.getFilesDir().getAbsolutePath() + "/station.json").exists()? LauncherActivity.class : SplashScreen.class);
		context.startActivity(intent);
	}
}
