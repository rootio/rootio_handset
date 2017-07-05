package org.rootio.tools.sms;

public interface MessageProcessor {

	
	public boolean ProcessMessage();
	
	/**
	 * Sends an SMS response to the SMS query 
	 * @param from The number that originally sent the SMS message
	 * @param data The body of the SMS to be sent
	 */
	public void respondAsyncStatusRequest(String from, String data);
}
