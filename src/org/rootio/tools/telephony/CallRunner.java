package org.rootio.tools.telephony;

import android.app.Activity;

public class CallRunner implements Runnable{

	private CallManager callManager;
	
	public CallRunner(Activity parentActivity)
	{
		//this.callManager = new CallManager(parentActivity);
	}
	
	@Override
	public void run()
	{
		callManager.run();
	}
}
