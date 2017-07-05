package org.rootio.services;

/**
 * This interface is used to decorate all classes that receive information about connections and disconnections to/from services
 * @author Jude Mukundane
 *
 */
public interface Notifiable {
	/**
	 * Notify of a connection to a service
	 * @param serviceId The ID of the service to which the connection was made
	 */
	public void notifyServiceConnection(int serviceId);
	
	/**
	 * Notify of a disconnection from a service
	 * @param serviceId The ID of the service that was disconnected
	 */
	public void notifyServiceDisconnection(int serviceId);
}
