package org.rootio.tools.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import org.rootio.handset.R;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    new SMSSwitch(context, smsMessage).getMessageProcessor().ProcessMessage();
                }
            }
        } catch (Exception ex) {
            Log.e(context.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SMSReceiver.onreceive)" : ex.getMessage());
        }
    }


}
