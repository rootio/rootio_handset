package org.rootio.services;

public interface ServiceInformationPublisher {

	/**
	 * Gets whether or not the service is running
	 * 
	 * @return Boolean indicating if service is running or not. True: running,
	 *         False: not running
	 */
	public boolean isRunning();

	/**
	 * Gets the ID of the service
	 * 
	 * @return Integer representing ID of the service
	 */
	public int getServiceId();

}
