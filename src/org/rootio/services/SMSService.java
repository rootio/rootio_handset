package org.rootio.services;

import org.rootio.radioClient.R;
import org.rootio.tools.sms.MessageProcessor;
import org.rootio.tools.sms.SMSSwitch;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSService extends Service implements IncomingSMSNotifiable, ServiceInformationPublisher {

	private boolean isRunning;
	private int serviceId = 2;
	private IncomingSMSReceiver incomingSMSReceiver;
	
	@Override
	public IBinder onBind(Intent arg0) {
		BindingAgent bindingAgent = new BindingAgent(this);
		return bindingAgent;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		this.incomingSMSReceiver = new IncomingSMSReceiver(this);
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startID)
	{
		Utils.doNotification(this,"RootIO","SMS Service started");
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		this.registerReceiver(this.incomingSMSReceiver, intentFilter);
		this.isRunning = true;
		this.sendEventBroadcast();
		return Service.START_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
	this.isRunning = false;
		try
		{
			this.unregisterReceiver(this.incomingSMSReceiver);
		}
		catch(Exception ex)
		{
		    Log.e(this.getString(R.string.app_name), ex.getMessage() == null?"Null pointer":ex.getMessage());
		}
		this.sendEventBroadcast();
		Utils.doNotification(this,"RootIO","SMS Service started");
	}
	
	
	@Override
	public void notifyIncomingSMS(SmsMessage message) {
		SMSSwitch smsSwitch = new SMSSwitch(this, message);
		MessageProcessor messageProcessor = smsSwitch.getMessageProcessor();
		messageProcessor.ProcessMessage();
	}
	
	@Override
	public boolean isRunning()
	{
		return this.isRunning;
	}
	
	/**
	 *Sends out broadcasts to listeners informing them of service status changes
	 */
	private void sendEventBroadcast()
	{
		Intent intent = new Intent();
		intent.putExtra("serviceId", this.serviceId);
		intent.putExtra("isRunning", this.isRunning);
		intent.setAction("org.rootio.services.sms.EVENT");
		this.sendBroadcast(intent);
	}

	@Override
	public int getServiceId() {
		return this.serviceId;
	}

}
