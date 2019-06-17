package org.rootio.tools.sms;

import org.rootio.handset.BuildConfig;
import org.rootio.services.DiagnosticsService;
import org.rootio.services.LinSipService;
import org.rootio.services.Notifiable;
import org.rootio.services.ProgramService;
import org.rootio.services.SMSService;
import org.rootio.services.ServiceConnectionAgent;
import org.rootio.services.ServiceInformationPublisher;
import org.rootio.services.SipService;
import org.rootio.services.SynchronizationService;
import org.rootio.services.TelephonyService;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class ServicesSMSHandler implements MessageProcessor, Notifiable {

    private final Context parent;
    private final String from;
    private final String[] messageParts;
    private ServiceConnectionAgent serviceConnectionAgent;

    ServicesSMSHandler(Context parent, String from, String[] messageParts) {
        this.parent = parent;
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public boolean ProcessMessage() {
        if (messageParts.length != 3) {
            return false;
        }

        // stopping a service
        if (messageParts[1].equals("stop")) {
            try {
                return this.stopService(Integer.parseInt(messageParts[2]));
            } catch (Exception ex) {
                return false;
            }
        }

        // starting a srevice
        if (messageParts[1].equals("start")) {
            try {
                if(BuildConfig.DEBUG) Utils.toastOnScreen("starting service " + messageParts[2], this.parent);
                return this.startService(Integer.parseInt(messageParts[2]));
            } catch (Exception ex) {
                return false;
            }
        }

        //restarting a service
        if (messageParts[1].equals("restart")) {
            try {
                if(BuildConfig.DEBUG) Utils.toastOnScreen("starting service " + messageParts[2], this.parent);
                return this.restartService(Integer.parseInt(messageParts[2]));
            } catch (Exception ex) {
                return false;
            }
        }

        // getting the service status
        if (messageParts[1].equals("status")) {
            try {
                return this.getServiceStatus(Integer.parseInt(messageParts[2]));
            } catch (Exception ex) {
                return false;
            }
        }



        return false;
    }

    private boolean restartService(int i) {
        this.stopService(i);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.startService(i);
        return true;
    }

    /**
     * Starts the service whose ID is specified
     *
     * @param serviceId The ID of the service to start
     * @return Boolean indicating whether or not the operation was successful
     */
    private boolean startService(int serviceId) {
        if(serviceId==0)//all services
        {
            for(int i : new int[]{1,2,3,4,5,6})
            {
                Intent intent = this.getServiceIntent(i);
                if (intent == null) {
                    return false;
                }
                this.parent.startForegroundService(intent);
            }
            this.respondAsyncStatusRequest("start all ok", from);
        }
        else {
            Intent intent = this.getServiceIntent(serviceId);
            if (intent == null) {
                return false;
            }
            this.parent.startForegroundService(intent);
            this.respondAsyncStatusRequest("start ok", from);
        }


            return true;

    }

    /**
     * Stops the Service whose ID is specified
     *
     * @param serviceId The ID of the service to be stopped
     * @return Boolean indicating whether or not the operation was successful
     */
    private boolean stopService(int serviceId) {
        if(serviceId==0) {
            for(int i : new int[] {/*1,*/ 2, 3, 4, 5 /*, 6*/})
            {
                Intent intent = new Intent();
                intent.setAction("org.rootio.services.STOP_EVENT");
                intent.putExtra("serviceId", i);
                this.parent.sendBroadcast(intent);
                // try to shutdown
                Intent intent2 = this.getServiceIntent(i);
                if (intent2 == null) {
                    return false;
                }
                this.parent.stopService(intent2);
            }
            this.respondAsyncStatusRequest("stop all ok", from);
        }
        else
        {
            Intent intent = new Intent();
            intent.setAction("org.rootio.services.STOP_EVENT");
            intent.putExtra("serviceId", serviceId);
            this.parent.sendBroadcast(intent);
            // try to shutdown
            Intent intent2 = this.getServiceIntent(serviceId);
            if (intent2 == null) {
                return false;
            }
            this.parent.stopService(intent2);
            this.respondAsyncStatusRequest(from,"stop " + serviceId +" ok");
        }
        return true;
    }

    /**
     * Gets the status of the service whose ID is specified
     *
     * @param serviceId The ID of the service whose status to return
     * @return Boolean indicating whether or not the service is running. True:
     * Running, False: Not running
     */
    private boolean getServiceStatus(int serviceId) {
        this.bindToService(serviceId);
        return true;
    }

    /**
     * Gets the Intent to be used to communicate with the intended service
     *
     * @param serviceId The ID of the service with which to communicate
     * @return The intent to be used in communicating with the desired service
     */
    private Intent getServiceIntent(int serviceId) {
        Intent intent = null;
        switch (serviceId) {
            case 1:
                intent = new Intent(this.parent, TelephonyService.class);
                break;
            case 2:
                intent = new Intent(this.parent, SMSService.class);
                break;
            case 3:
                intent = new Intent(this.parent, DiagnosticsService.class);
                break;
            case 4:
                intent = new Intent(this.parent, ProgramService.class);
                break;
            case 5:
                intent = new Intent(this.parent, SynchronizationService.class);
                break;
            case 6:
                intent = new Intent(this.parent, LinSipService.class);
                break;
        }
        return intent;
    }

    /**
     * Binds to the program service to get status of programs that are displayed
     * on the home radio screen
     */
    private void bindToService(int serviceId) {
        serviceConnectionAgent = new ServiceConnectionAgent(this, 4);
        Intent intent = this.getServiceIntent(serviceId);
        if (this.parent.bindService(intent, serviceConnectionAgent, Context.BIND_AUTO_CREATE)) {
            // just wait for the async call
        }
    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(from, null, data, null, null);
    }

    @Override
    public void notifyServiceConnection(int serviceId) {
        ServiceInformationPublisher service = this.serviceConnectionAgent.getService();
        this.notifyServiceStatus(serviceId, service.isRunning());

    }

    private void notifyServiceStatus(int serviceId, boolean running) {
        this.respondAsyncStatusRequest(this.from, running ? String.format("Service %s running", serviceId) : String.format("Service %s not running", serviceId));
    }

    @Override
    public void notifyServiceDisconnection(int serviceId) {
        // TODO Auto-generated method stub

    }
}
