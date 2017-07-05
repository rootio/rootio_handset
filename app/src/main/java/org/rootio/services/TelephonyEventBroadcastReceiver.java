package org.rootio.services;

import org.rootio.activities.services.TelephonyEventNotifiable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class is used to listen for changes in Telephony service to notify other
 * components to halt activity that is not desired duroing phone calls
 * 
 * @author HP Envy
 * 
 */
public class TelephonyEventBroadcastReceiver extends BroadcastReceiver {

	private TelephonyEventNotifiable notifiable;

	TelephonyEventBroadcastReceiver(TelephonyEventNotifiable notifiable) {
		this.notifiable = notifiable;
	}

	@Override
	public void onReceive(Context arg0, Intent intent) {
		boolean isInCall = intent.getBooleanExtra("Incall", false);
		notifiable.notifyTelephonyStatus(isInCall);

	}

}
