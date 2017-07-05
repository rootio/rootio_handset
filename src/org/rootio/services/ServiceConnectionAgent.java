package org.rootio.services;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * This class helps activities to communicate with services through instances of the BindingAgent class
 * @author Jude Mukundane
 *
 */
public class ServiceConnectionAgent implements ServiceConnection{

	private ServiceInformationPublisher service;
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
        this.service = (ServiceInformationPublisher)bindingAgent.getService();
        this.servicesActivity.notifyServiceConnection(this.serviceId);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
	
	}
	
	/**
	 * Gets the Service information publisher for the service to which this object is connected
	 * @return ServiceInformationPublisher object containing information about the service connected to
	 */
	public  ServiceInformationPublisher getService()
	{
		return this.service;
	}

	
}
