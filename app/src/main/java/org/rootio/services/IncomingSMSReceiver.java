package org.rootio.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import org.rootio.tools.utils.Utils;

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
        try {
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                this.incomingSMSNotifiable.notifyIncomingSMS(smsMessage);
            }
        }
        catch (Exception ex){

        }
    }

}
