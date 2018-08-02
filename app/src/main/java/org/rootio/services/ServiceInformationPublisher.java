package org.rootio.services;

public interface ServiceInformationPublisher {

    /**
     * Gets whether or not the service is running
     *
     * @return Boolean indicating if service is running or not. True: running,
     * False: not running
     */
    boolean isRunning();

    /**
     * Gets the ID of the service
     *
     * @return Integer representing ID of the service
     */
    int getServiceId();

    /**
     * Sends out broadcasts informing listeners of change in service state
     */
    void sendEventBroadcast();

}
