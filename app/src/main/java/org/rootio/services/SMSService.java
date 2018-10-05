package org.rootio.services;

import org.rootio.handset.R;
import org.rootio.tools.sms.MessageProcessor;
import org.rootio.tools.sms.SMSSwitch;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;

import static android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION;

public class SMSService extends Service implements IncomingSMSNotifiable, ServiceInformationPublisher {

    private boolean isRunning;
    private final int serviceId = 2;
    private IncomingSMSReceiver incomingSMSReceiver;

    @Override
    public IBinder onBind(Intent arg0) {
       return new BindingAgent(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.incomingSMSReceiver = new IncomingSMSReceiver(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        Utils.doNotification(this, "RootIO", "SMS Service started");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SMS_RECEIVED_ACTION);
        this.registerReceiver(this.incomingSMSReceiver, intentFilter);
        this.isRunning = true;
        this.sendEventBroadcast();
        this.startForeground(this.serviceId, Utils.getNotification(this, "RootIO", "SMS service is running", R.drawable.icon, false, null, null));
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
            this.stopForeground(true);
            this.isRunning = false;
            try {
                 this.unregisterReceiver(this.incomingSMSReceiver);
            } catch (Exception ex) {
                Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer" : ex.getMessage());
            }
            this.sendEventBroadcast();
            Utils.doNotification(this, "RootIO", "SMS Service stopped");
        }
    }

    @Override
    public void notifyIncomingSMS(SmsMessage message) {
        SMSSwitch smsSwitch = new SMSSwitch(this, message);
        MessageProcessor messageProcessor = smsSwitch.getMessageProcessor();
        if (messageProcessor != null) {
            messageProcessor.ProcessMessage();
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Sends out broadcasts to listeners informing them of service status
     * changes
     */
    @Override
    public void sendEventBroadcast() {
        Intent intent = new Intent();
        intent.putExtra("serviceId", this.serviceId);
        intent.putExtra("isRunning", this.isRunning);
        intent.setAction("org.rootio.services.sms.EVENT");
        this.sendBroadcast(intent);
    }

    @Override
    public int getServiceId() {
        return this.serviceId;
    }

}
