package org.rootio.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStopReceiver extends BroadcastReceiver {

    ServiceStopNotifiable connectedActivity;

    public ServiceStopReceiver(ServiceStopNotifiable connectedActivity) {
        this.connectedActivity = connectedActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.connectedActivity.notifyServiceStop(intent.getIntExtra("serviceId", 0));

    }

}
