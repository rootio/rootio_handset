package org.rootio.activities;

import org.rootio.activities.services.ServiceExitInformable;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Listens for exit announcements from the radio service and informs bound activities to unbind
 * so the service can shut down.
 * @author Jude Mukundane
 *
 */
public class RadioServiceExitBroadcastHandler extends BroadcastReceiver{

	private ServiceExitInformable radioActivity;
	
	RadioServiceExitBroadcastHandler(ServiceExitInformable radioActivity)
	{
		this.radioActivity = radioActivity;
	}
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		radioActivity.disconnectFromRadioService(); 
	}

}
