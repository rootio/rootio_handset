package org.rootio.services;

import org.rootio.tools.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;

import com.esotericsoftware.kryo.util.Util;

/**
 * This class listens for boot incidents and restores the services to the state
 * they were in before the phone shut down
 *
 * @author Jude Mukundane
 */
public class BootMonitor extends BroadcastReceiver {

    @Override
    public synchronized void onReceive(Context context, Intent arg1) {
        if(arg1.getBooleanExtra("isRestart", false))
        {
            this.stopAllServices(context);
        }
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startAllServices(context, arg1.getBooleanExtra("isRestart", false));

    }

    private void startAllServices(Context context, boolean isRestart) {
        if(Utils.isConnectedToStation(context)) {
            for (int serviceId : new int[]{/*1, 2,*/ 3, 4, 5 /*, 6*/}) {
                ServiceState serviceState = new ServiceState(context, serviceId);
                if (isRestart || serviceState.getServiceState() > 0)// service was started
                {
                    Intent intent = this.getIntentToLaunch(context, serviceId);
                    context.startForegroundService(intent);
                }
            }
        }
    }

    /**
     * Gets the intent to be used to launch the service with the specified
     * serviceId
     *
     * @param context   The context to be used in creating the intent
     * @param serviceId The ID of the service for which to create the intent
     * @return
     */
    private Intent getIntentToLaunch(Context context, int serviceId) {
        Intent intent = null;
        switch (serviceId) {
           case 3: // Diagnostic Service
                intent = new Intent(context, DiagnosticsService.class);
                break;
            case 4: // Program Service
                intent = new Intent(context, RadioService.class);
                break;
            case 5: // Sync Service
                intent = new Intent(context, SynchronizationService.class);
                break;
        }
        return intent;
    }

    private void stopAllServices(Context context)
    {
        if(Utils.isConnectedToStation(context)) {
            for (int serviceId : new int[]{/*1,*/ 2, 3, 4, 5 /*, 6*/}) {
                ServiceState serviceState = new ServiceState(context, serviceId);
                serviceState.setServiceState(0);
                serviceState.save();
                // if (serviceState.getServiceState() > 0)// service was started
                // {
                Intent intent = this.getIntentToLaunch(context, serviceId);
                context.stopService(intent);
                // }
            }
        }
    }

}
