package org.rootio.services;

import org.rootio.tools.utils.Utils;

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
public class CrashMonitor extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		Utils.setContext(context);
		for (int serviceId : new int[] { 1, 2, 4 }) // only vitals
		{
			//ServiceState serviceState = new ServiceState(context, serviceId);
			// if(serviceState.getServiceState() > 0)//service was started
			// {
			Intent intent = this.getIntentToLaunch(context, serviceId);
			context.startService(intent);
			// }
		}

	}

	/**
	 * Gets the intent to be used to launch the service with the specified
	 * serviceId
	 * 
	 * @param context
	 *            The context to be used in creating the intent
	 * @param serviceId
	 *            The ID of the service for which to create the intent
	 * @return
	 */
	private Intent getIntentToLaunch(Context context, int serviceId) {
		Intent intent = null;
		switch (serviceId) {
		case 1: // telephony service
			intent = new Intent(context, TelephonyService.class);
			break;
		case 2: // SMS service
			intent = new Intent(context, SMSService.class);
			break;
		case 3: // Diagnostic Service
			intent = new Intent(context, DiagnosticsService.class);
			break;
		case 4: // Program Service
			intent = new Intent(context, ProgramService.class);
			break;
		case 5: // Sync Service
			intent = new Intent(context, SynchronizationService.class);
			break;
		case 6: // Discovery Service
			intent = new Intent(context, DiscoveryService.class);
			break;
		}
		return intent;
	}

}
