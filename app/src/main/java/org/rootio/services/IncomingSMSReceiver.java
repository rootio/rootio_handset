package org.rootio.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * This class is a listener for incoming SMS
 *
 * @author HP Envy
 */
public class IncomingSMSReceiver extends BroadcastReceiver {

    private IncomingSMSNotifiable incomingSMSNotifiable;

    IncomingSMSReceiver(IncomingSMSNotifiable incomingSMSNotifiable) {
        this.incomingSMSNotifiable = incomingSMSNotifiable;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            this.incomingSMSNotifiable.notifyIncomingSMS(smsMessage);
        }

    }

}
