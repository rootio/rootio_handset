package org.rootio.tools.sms;

import android.content.Context;
import android.telephony.SmsMessage;

public class SMSSwitch {

	private String[] messageParts;
	private String from;
	private Context parent;
	
	public SMSSwitch(Context parent, SmsMessage message){
		this.parent = parent;
		this.from = message.getOriginatingAddress();
		this.messageParts = this.getMessageParts(message.getMessageBody().toLowerCase());
	}
	
	public MessageProcessor getMessageProcessor()
	{
		return this.switchSMS(this.messageParts);
	}
	
	private String[] getMessageParts(String message)
	{
		return message.split("");
	}
	
	private MessageProcessor switchSMS(String[] messageParts) {
		String keyword = messageParts.length > 0? messageParts[0]: "";
		if (keyword.equals("network")) {
             return new NetworkSMSHandler(this.parent, from, messageParts);
		}
		if (keyword.equals("station")) {

			return new StationSMSHandler(this.parent, from, messageParts);
		}
		if (keyword.equals("services")) {

			return new ServicesSMSHandler(this.parent, from, messageParts);
		}
		if (keyword.equals("resources")) {

			return new ResourcesSMSHandler(this.parent, from, messageParts);
		}
		if (keyword.equals("sound")) {

			return new SoundSMSHandler(this.parent, from, messageParts);
		}
		return null;
	}
}
