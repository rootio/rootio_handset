package org.rootio.services;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ServiceConnectionAgent implements ServiceConnection{

	private RunningStatusPublished service;
	private int serviceId;
	private Notifiable servicesActivity;
	
	public ServiceConnectionAgent(Notifiable servicesActivity, int serviceId)
	{
		this.servicesActivity = servicesActivity;
		this.serviceId = serviceId;
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		BindingAgent bindingAgent = (BindingAgent) service;
        this.service = (RunningStatusPublished)bindingAgent.getService();
        this.servicesActivity.notifyServiceConnection(this.serviceId);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
	
	}
	
	public  RunningStatusPublished getService()
	{
		return this.service;
	}

	
}
