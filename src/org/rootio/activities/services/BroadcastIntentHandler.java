package org.rootio.activities.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives broadcasts about change in status of the various services and
 * notifies the services activity to reflect the same on the display
 * 
 * @author HP Envy
 * 
 */
public class BroadcastIntentHandler extends BroadcastReceiver {

	private ServicesActivity servicesActivity;

	BroadcastIntentHandler(ServicesActivity servicesActivity) {
		this.servicesActivity = servicesActivity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		this.servicesActivity.updateDisplay(intent.getIntExtra("serviceId", 0), intent.getBooleanExtra("isRunning", false));
	}
}
