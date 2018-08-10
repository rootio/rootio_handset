package org.rootio.services;

import org.rootio.handset.R;
import org.rootio.services.synchronization.SynchronizationDaemon;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SynchronizationService extends Service implements ServiceInformationPublisher {

    private final int serviceId = 5;
    private boolean isRunning;

    @Override
    public IBinder onBind(Intent arg0) {
        return new BindingAgent(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!this.isRunning) {
            SynchronizationDaemon synchronizationDaemon = new SynchronizationDaemon(this);
            Thread thread = new Thread(synchronizationDaemon);
            this.isRunning = true;
            thread.start();
            this.sendEventBroadcast();
            Utils.doNotification(this, "RootIO", "Synchronization Service Started");
        }
        this.startForeground(this.serviceId, Utils.getNotification(this, "RootIO", "Synchronization service is running", R.drawable.icon, false, null, null));
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        this.stopForeground(true);
        this.shutDownService();
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
