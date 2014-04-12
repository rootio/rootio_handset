package org.rootio.services;

import android.telephony.SmsMessage;

public interface IncomingSMSNotifiable {

	void notifyIncomingSMS(SmsMessage message);
}
