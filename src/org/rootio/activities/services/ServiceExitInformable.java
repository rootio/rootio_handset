package org.rootio.activities.services;

public interface ServiceExitInformable {

	/**
	 * Ends the connection by an activity to the program service allowing the
	 * program service to shut down.
	 */
	public void disconnectFromRadioService();
}
