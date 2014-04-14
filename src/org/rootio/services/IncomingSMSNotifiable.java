package org.rootio.services;

import android.telephony.SmsMessage;

/**
 * This interface is used to decorate all classes that may be notified when an incoming SMS is received
 * @author Jude Mukundane
 *
 */
public interface IncomingSMSNotifiable {

	/**
	 * Fired off when the phone receives an SMS message
	 * @param message An SmsMessage Object representing the received SMS
	 */
	void notifyIncomingSMS(SmsMessage message);
}
