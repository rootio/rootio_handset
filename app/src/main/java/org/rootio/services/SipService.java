package org.rootio.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.rootio.services.SIP.CallState;
import org.rootio.services.SIP.RegistrationState;
import org.rootio.tools.utils.Utils;

import java.text.ParseException;

import static android.content.ContentValues.TAG;

public class SipService extends Service implements ServiceInformationPublisher {

    private final int serviceId = 5;
    private SipManager sipManager;
    private SipProfile sipProfile;
    private SipAudioCall sipCall;
    private CallState callState = CallState.IDLE;
    private RegistrationState registrationState = RegistrationState.UNREGISTERED;
    private String username, password, domain;
    private SharedPreferences prefs;
    private boolean isRunning;
    private boolean wasStoppedOnPurpose;


    @Override
    public void onCreate() {
        this.sipManager = SipManager.newInstance(this);
        this.prefs = this.getSharedPreferences("org.rootio.rootio_handset", Context.MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            this.register();
            Utils.doNotification(this, "RootIO", "SIP Service Started");
            this.sendEventBroadcast();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        if (intent != null) {
            wasStoppedOnPurpose = intent.getBooleanExtra("wasStoppedOnPurpose", false);
            if (wasStoppedOnPurpose) {
                this.shutDownService();
            } else {
                this.onDestroy();
            }
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        BindingAgent bindingAgent = new BindingAgent(this);
        return bindingAgent;
    }

    @Override
    public void onDestroy() {
        if (this.wasStoppedOnPurpose == false) {
            Intent intent = new Intent("org.rootio.services.restartServices");
            sendBroadcast(intent);
        } else {
            this.shutDownService();
        }
        super.onDestroy();
    }

    private void loadConfig() {
        if (this.prefs != null) {
            this.domain = prefs.getString("org.rootio.sipjunior.domain", "");
            this.username = prefs.getString("org.rootio.sipjunior.username", "");
            this.password = prefs.getString("org.rootio.sipjunior.password", "");
        }
    }

    private void listenForIncomingCalls() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("org.rootio.sipjunior.INCOMING_CALL");
        CallReceiver receiver = new CallReceiver();
        this.registerReceiver(receiver, filter);
    }

    private void prepareSipProfile() {
        try {
            SipProfile.Builder builder = new SipProfile.Builder(username, domain);
            builder.setPassword(password);
            builder.setPort(5060);
            this.sipProfile = builder.build();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    void register() {
        try {
            this.loadConfig();
            this.prepareSipProfile();

            if (this.username == "" || this.password == "" || this.domain == "") //Some servers may take blank username or passwords. modify accordingly..
            {
                //ideally this only happen in an unreged state
                ContentValues values = new ContentValues();
                values.put("errorCode", 0);
                values.put("errorMessage", "No config information found");
                this.notifyRegistrationEvent(this.registrationState, values);
                return;
            }

            Intent intent = new Intent();
            intent.setAction("org.rootio.sipjunior.INCOMING_CALL");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
            this.sipManager = SipManager.newInstance(this);
            this.sipManager.open(this.sipProfile, pendingIntent, null);
            this.sipManager.register(this.sipProfile, 30, new RegistrationListener());
            this.listenForIncomingCalls();
        } catch (SipException e) {
            e.printStackTrace();
            //Ideally if this happens, then reg fails, the status should still be DEREGISTERED
            ContentValues values = new ContentValues();
            values.put("errorCode", 0);
            values.put("errorMessage", "SIP Error occurred. Please check config and network availability");
            this.notifyRegistrationEvent(this.registrationState, values);
        }
    }

    public void deregister() {
        try {
            this.sipManager.unregister(this.sipProfile, new UnregistrationListener());

        } catch (SipException e) {
            e.printStackTrace();
            this.notifyRegistrationEvent(this.registrationState, null); //potential conflict of handling to the receiver
        }
    }

    public void hangup() {
        try {
            this.sipCall.endCall();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    public void answer() {
        try {
            this.sipCall.answerCall(30);
            this.sipCall.startAudio();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    private void notifyRegistrationEvent(final RegistrationState registrationState, final ContentValues values) {
        SipService.this.registrationState = registrationState;
    }

    private void notifyCallEvent(final CallState callState, final ContentValues values) {

        SipService.this.callState = callState;

    }

    class CallReceiver extends BroadcastReceiver {

        private CallListener listener;

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                this.listener = new CallListener();
                SipService.this.sipCall = SipService.this.sipManager.takeAudioCall(intent, listener);
            } catch (SipException e) {
                e.printStackTrace();

            }
        }
    }


    class CallListener extends SipAudioCall.Listener {

        @Override
        public void onError(SipAudioCall call, int errorCode, String message) {
            try {
                SipService.this.notifyCallEvent(CallState.IDLE, null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCallEnded(SipAudioCall call) {
            try {
                SipService.this.sipCall = null;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCallEstablished(SipAudioCall call) {
            SipService.this.sipCall = call;
            SipService.this.sipCall.startAudio();
            ContentValues values = new ContentValues();
            values.put("otherParty", call.getPeerProfile().getUriString());
            SipService.this.notifyCallEvent(CallState.IDLE.INCALL, values);
        }

        @Override
        public void onRinging(SipAudioCall call, SipProfile caller) {
            try {
                SipService.this.sipCall = call;
                SipService.this.sipCall.answerCall(30);
                ContentValues values = new ContentValues();
                values.put("otherParty", call.getPeerProfile().getUriString());
                SipService.this.notifyCallEvent(CallState.RINGING, values);
            } catch (SipException e) {
                e.printStackTrace();
            }
        }

    }

    class RegistrationListener implements SipRegistrationListener {

        @Override
        public void onRegistering(String localProfileUri) {
            notifyRegistrationEvent(RegistrationState.REGISTERING, null);
        }

        @Override
        public void onRegistrationDone(String localProfileUri, final long expiryTime) {

            notifyRegistrationEvent(RegistrationState.REGISTERED, null);
        }

        @Override
        public void onRegistrationFailed(final String localProfileUri, final int errorCode, final String errorMessage) {

            notifyRegistrationEvent(RegistrationState.UNREGISTERED, null);
        }
    }

    class UnregistrationListener implements SipRegistrationListener {


        @Override
        public void onRegistering(String localProfileUri) {
            notifyRegistrationEvent(RegistrationState.DEREGISTERING, null);
        }

        @Override
        public void onRegistrationDone(String localProfileUri, final long expiryTime) {
            ContentValues values = new ContentValues();
            values.put("localProfileUri", localProfileUri);
            notifyRegistrationEvent(RegistrationState.UNREGISTERED, values);
        }

        @Override
        public void onRegistrationFailed(final String localProfileUri, final int errorCode, final String errorMessage) {
            ContentValues values = new ContentValues();
            values.put("errorCode", errorCode);
            values.put("errorMessage", errorMessage);
            Log.i(TAG, "onRegistrationFailed: " + errorMessage + errorCode);
            notifyRegistrationEvent(RegistrationState.REGISTERED, values);
        }
    }

    class BindingAgent extends Binder {

        private Service service;

        BindingAgent(Service service) {
            this.service = service;
        }

        /**
         * Returns the service for which this class is providing a binding
         * connection
         *
         * @return Service object for the service bound to
         */
        Service getService() {
            return this.service;
        }
    }


    private void shutDownService() {
        if (this.isRunning) {
            this.isRunning = false;
            this.deregister();
            Utils.doNotification(this, "RootIO", "SIP Service Stopped");
            this.sendEventBroadcast();
        }
    }

    /**
     * Gets the ID of this service
     *
     * @return Integer representation of the ID of this service
     */
    @Override
    public int getServiceId() {
        return this.serviceId;
    }

    @Override
    public void sendEventBroadcast() {
        Intent intent = new Intent();
        intent.putExtra("serviceId", this.serviceId);
        intent.putExtra("isRunning", this.isRunning);
        intent.setAction("org.rootio.services.SIP.EVENT");
        this.sendBroadcast(intent);
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }


}
