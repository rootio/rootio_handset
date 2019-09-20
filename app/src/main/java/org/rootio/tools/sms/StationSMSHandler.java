package org.rootio.tools.sms;

import android.content.Context;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.util.Log;

import org.rootio.handset.BuildConfig;
import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

public class StationSMSHandler implements MessageProcessor {

    private Context parent;
    private final String from;
    private final String[] messageParts;

    public StationSMSHandler(Context parent, String from, String[] messageParts) {
        this.parent = parent;
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public boolean ProcessMessage() {
        if (messageParts.length != 2) {
            return false;
        }

        // rebooting the phone
        if (messageParts[1].equals("reboot")) {
            try {
                return this.reboot();
            } catch (Exception ex) {
                return false;
            }
        }

        return false;
    }

    private boolean reboot() {
        try {
            Runtime.getRuntime().exec(new String[] { "su", "-c", "reboot" });
            return true;
        }
        catch(Exception ex)
        {
            Log.e(this.parent.getString(R.string.app_name), String.format("[StationSMSHandler.reboot] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
            return false;
        }
    }


    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(from, null, data, null, null);
    }

}
