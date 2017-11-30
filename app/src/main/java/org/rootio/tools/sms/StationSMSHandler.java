package org.rootio.tools.sms;

import android.content.Context;
import android.telephony.SmsManager;

public class StationSMSHandler implements MessageProcessor {

    private Context parent;
    private String from;

    public StationSMSHandler(Context parent, String from, String[] messageParts) {
        this.parent = parent;
        this.from = from;
    }

    @Override
    public boolean ProcessMessage() {
        return false;

    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(from, null, data, null, null);
    }

}
