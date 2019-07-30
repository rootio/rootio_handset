package org.rootio.tools.sms;

import android.content.ContentValues;
import android.content.Context;
import android.telephony.SmsManager;

import org.rootio.tools.utils.Utils;

import java.util.Date;

/**
 * THis Class handles IDs and Timestamp that are used to synchronize calls, SMS and music to the cloud
 * resetting these ids results in a sync of records with an id/date_added greater than the supplied value
 */
class MarkHandler implements MessageProcessor {
    private final Context parent;
    private final String from;
    private final String[] messageParts;

    public MarkHandler(Context parent, String from, String[] messageParts) {
        this.parent = parent;
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public boolean ProcessMessage() {
        if (messageParts.length != 3) {
            return false;
        }

        switch (messageParts[1]) {
            case "call":
                return setId("call_id", messageParts[2]);
            case "sms":
                return setId("sms_id", messageParts[2]);
            case "music":
                return setId("media_max_date_added", String.valueOf(getUsefulMinDate(messageParts[2])));
            default:
                return false;
        }
    }

    long getUsefulMinDate(String dateStr)
    {
        Date dt = Utils.getDateFromString(dateStr, "yyyy-MM-dd HH:mm:ss");
        if(dt == null)
        {
            return (long)Utils.getPreference("media_max_date_added", long.class, this.parent);
        }
        else
        {
            return dt.getTime()/1000;
        }

    }

    private boolean setId(String param, String value) {
        try {
            ContentValues values = new ContentValues();
            Utils.toastOnScreen(value, this.parent);
            values.put(param, Long.parseLong(value));
            Utils.savePreferences(values, this.parent);
            this.respondAsyncStatusRequest(this.from, "mark " + messageParts[1] + " " + messageParts[2] + " ok");
        } catch (Exception ex) {
            this.respondAsyncStatusRequest(this.from, "mark " + messageParts[1] + " " + messageParts[2] + " fail");
            return false;
        }
        return true;
    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(from, null, data, null, null);
    }
}
