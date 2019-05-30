package org.rootio.services;

import org.rootio.handset.R;
import org.rootio.services.synchronization.SynchronizationDaemon;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SynchronizationService extends Service implements ServiceInformationPublisher {

    private final int serviceId = 5;
    private boolean isRunning;

    @Override
    public IBinder onBind(Intent arg0) {
        return new BindingAgent(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.logEvent(this, Utils.EventCategory.SERVICES, Utils.EventAction.START, "Synchronization Service");
        if (!this.isRunning) {
            SynchronizationDaemon synchronizationDaemon = new SynchronizationDaemon(this);
            Thread thread = new Thread(synchronizationDaemon);
            this.isRunning = true;
            thread.start();
            this.sendEventBroadcast();
            Utils.doNotification(this, "RootIO", "Synchronization Service Started");
        }
        this.startForeground(this.serviceId, Utils.getNotification(this, "RootIO", "Synchronization service is running", R.drawable.icon, false, null, null));
        new ServiceState(this, 5,"Synchronization", 1).save();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Utils.logEvent(this, Utils.EventCategory.SERVICES, Utils.EventAction.STOP, "Synchronization Service");
        this.stopForeground(true);
        try {
            this.shutDownService();
        }
        catch(Exception ex)
        {
            Log.e(this.getString(R.string.app_name), String.format("[SynchronizationService.onDestroy] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
        }
        new ServiceState(this, 5,"Synchronization", 0).save();
        super.onDestroy();
    }

    private void shutDownService() {
        if (this.isRunning) {
            this.isRunning = false;
            Utils.doNotification(this, "RootIO", "Synchronization Service Stopped");
            this.sendEventBroadcast();
        }
    }

    /**
     * Sends out broadcasts informing listeners of changes in service status
     */
    public void sendEventBroadcast() {
        Intent intent = new Intent();
        intent.putExtra("serviceId", this.serviceId);
        intent.putExtra("isRunning", this.isRunning);
        intent.setAction("org.rootio.services.synchronization.EVENT");
        this.sendBroadcast(intent);
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public int getServiceId() {
        return this.serviceId;
    }

}
