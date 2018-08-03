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
import android.telecom.Call;
import android.util.Log;

import org.rootio.services.SIP.CallState;
import org.rootio.services.SIP.RegistrationState;
import org.rootio.tools.utils.Utils;

import java.text.ParseException;

import static android.content.ContentValues.TAG;

public class SipService extends Service implements ServiceInformationPublisher {

    private final int serviceId = 6;
    private SipManager sipManager;
    private SipProfile sipProfile;
    private SipAudioCall sipCall;
    private CallState callState = CallState.IDLE;
    private RegistrationState registrationState = RegistrationState.UNREGISTERED;
    private String username, password, domain;
    private SharedPreferences prefs;
    private boolean isRunning;
    private boolean wasStoppedOnPurpose;
    private CallListener callListener;
    private RegistrationListener registrationListener;


    @Override
    public void onCreate() {
        this.prefs = this.getSharedPreferences("org.rootio.handset", Context.MODE_PRIVATE);
        this.callListener = new CallListener();
        this.registrationListener = new RegistrationListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            this.listenForIncomingCalls();
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

    /**
     * Load SIP configuration information from the stored credentials
     */
    private void loadConfig() {
        if (this.prefs != null) {
            this.domain = prefs.getString("org.rootio.sipjunior.domain", "");
            this.username = prefs.getString("org.rootio.sipjunior.username", "");
            this.password = prefs.getString("org.rootio.sipjunior.password", "");
        }
    }

    /**
     * Listens for incoming calls on the SIP profile once registered
     */
    private void listenForIncomingCalls() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("org.rootio.handset.SIP.INCOMING_CALL");
        CallReceiver receiver = new CallReceiver();
        this.registerReceiver(receiver, filter);
    }

    /**
     * Creates a SIP profile from the profile information supplied from the cloud platform
     */
    private void prepareSipProfile() {
        try {
            SipProfile.Builder builder = new SipProfile.Builder(this.username, this.domain);
            builder.setPassword(this.password);
            builder.setPort(5060);
            this.sipProfile = builder.build();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    void register() {
        try {
            this.loadConfig();
            if (this.username == "" || this.password == "" || this.domain == "") //Some servers may take blank username or passwords. modify accordingly..
            {
                //ideally this only happen in an unreged state
                Utils.toastOnScreen("Some configuration information is missing. SIP registration not possible", this);
                return;
            }
            this.prepareSipProfile();

            Intent intent = new Intent();
            intent.setAction("org.rootio.handset.SIP.INCOMING_CALL");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
            this.sipManager = SipManager.newInstance(this);
            this.sipManager.open(this.sipProfile, pendingIntent, this.registrationListener);
            this.sipManager.register(this.sipProfile, 30, this.registrationListener);

        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    public void deregister() {
        try {
            this.sipManager.unregister(this.sipProfile, new UnregistrationListener());

        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process an incoming SIP call: Typically check the whitelist to make sure that the number is allowed to call this station
     */
    private void handleCall() {
        //check the whitelist
        if(true)
        {
            this.answer();
        }
        else
        {
            this.hangup();
        }
    }


    /**
     * Terminate a SIP call that has been taken over by this service
     */
    public void hangup() {
        try {
            this.sipCall.endCall();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    /**
     * Answer a SIP call that has been taken over by this service
     */
    public void answer() {
        try {
            this.sipCall.answerCall(30);
            this.sipCall.startAudio();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    class CallReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //take calls only if not in other call
                if(SipService.this.callState == CallState.INCALL)
                {
                    SipService.this.sipManager.takeAudioCall(intent, null).endCall();
                }
                else {
                    SipService.this.sipCall = SipService.this.sipManager.takeAudioCall(intent, SipService.this.callListener);
                    SipService.this.handleCall();
                }
                } catch (SipException e) {
                e.printStackTrace();

            }
        }


    }


    class CallListener extends SipAudioCall.Listener {

        @Override
        public void onError(SipAudioCall call, int errorCode, String message) {
            try {
                SipService.this.callState = CallState.IDLE;

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
            Utils.toastOnScreen("Established",SipService.this);
            SipService.this.sipCall = call;
            SipService.this.sipCall.startAudio();
            SipService.this.callState = CallState.IDLE.INCALL;
        }

        @Override
        public void onRinging(SipAudioCall call, SipProfile caller) {
            try {
                Utils.toastOnScreen("Ringing...", SipService.this);
                SipService.this.sipCall = call;
                SipService.this.sipCall.answerCall(30);
                SipService.this.callState = CallState.RINGING;
            } catch (SipException e) {
                e.printStackTrace();
            }
        }

    }

    class RegistrationListener implements SipRegistrationListener {

        @Override
        public void onRegistering(String localProfileUri) {
            SipService.this.registrationState = RegistrationState.REGISTERING;
        }

        @Override
        public void onRegistrationDone(String localProfileUri, final long expiryTime) {

            SipService.this.registrationState = RegistrationState.REGISTERED;
        }

        @Override
        public void onRegistrationFailed(final String localProfileUri, final int errorCode, final String errorMessage) {

            SipService.this.registrationState = RegistrationState.UNREGISTERED;
        }
    }

    class UnregistrationListener implements SipRegistrationListener {


        @Override
        public void onRegistering(String localProfileUri) {
            SipService.this.registrationState = RegistrationState.DEREGISTERING;
        }

        @Override
        public void onRegistrationDone(String localProfileUri, final long expiryTime) {
            SipService.this.registrationState = RegistrationState.UNREGISTERED;
        }

        @Override
        public void onRegistrationFailed(final String localProfileUri, final int errorCode, final String errorMessage) {
            Log.i(TAG, "onRegistrationFailed: " + errorMessage + errorCode);
            SipService.this.registrationState = RegistrationState.REGISTERED;
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
