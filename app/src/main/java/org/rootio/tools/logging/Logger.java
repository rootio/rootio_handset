package org.rootio.tools.logging;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

public class Logger extends BroadcastReceiver {


    private long log(Context context, Intent intent)
    {
        Utils.toastOnScreen("received logging event..", context);
        try {
            ContentValues values = new ContentValues();
            values.put("category", intent.getStringExtra("category"));
            values.put("argument", intent.getStringExtra("argument"));
            values.put("event", intent.getStringExtra("event"));
            values.put("eventdate", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
            return DBAgent.saveData("activitylog", null, values);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.log(context, intent);
    }
}
