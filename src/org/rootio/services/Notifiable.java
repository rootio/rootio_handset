package org.rootio.services;

public interface Notifiable {
	public void notifyServiceConnection(int serviceId);
	
	public void notifyServiceDisconnection(int serviceId);
}
