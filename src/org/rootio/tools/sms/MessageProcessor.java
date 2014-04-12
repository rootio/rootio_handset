package org.rootio.tools.sms;

public interface MessageProcessor {

	
	public boolean ProcessMessage();
	
	public void respondAsyncStatusRequest(String from, String data);
}
