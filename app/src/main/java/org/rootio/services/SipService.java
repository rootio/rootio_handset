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

import org.rootio.handset.R;
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
    private boolean isRunning;
    private CallListener callListener;
    private CallReceiver receiver;
    private RegistrationListener registrationListener;


    @Override
    public void onCreate() {
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
        this.startForeground(this.serviceId, Utils.getNotification(this, "RootIO", "SIP service is running", R.drawable.icon, false, null, null));
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        BindingAgent bindingAgent = new BindingAgent(this);
        return bindingAgent;
    }

    @Override
    public void onDestroy() {
        this.stopForeground(true);
        this.shutDownService();
        super.onDestroy();
    }

    /**
     * Load SIP configuration information from the stored credentials
     */
    private void loadConfig() {
        this.domain = "89.109.64.165"; //(String)Utils.getPreference("sip_domain", String.class, this);
            this.username = "1001"; //(String)Utils.getPreference("sip_username", String.class, this);
            this.password = "th1s_be_passw0rd"; // (String)Utils.getPreference("sip_password", String.class, this);
    }

    /**
     * Listens for incoming calls on the SIP profile once registered
     */
    private void listenForIncomingCalls() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("org.rootio.handset.SIP.INCOMING_CALL");
        receiver = new CallReceiver();
        this.registerReceiver(receiver, filter);
    }

    /**
     * Creates a SIP profile from the profile information supplied from the cloud platform
     */
    private void prepareSipProfile() {
        try {
            SipProfile.Builder builder = new SipProfile.Builder(this.username, this.domain);
            builder.setPassword(this.password);
            builder.setAutoRegistration(true);
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

            this.sipManager.open(this.sipProfile, pendingIntent, null);

            this.sipManager.register(this.sipProfile, 300, new RegistrationListener());
            //this.sipManager.setRegistrationListener(this.sipProfile.getUriString(), );
            this.listenForIncomingCalls();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    public void deregister() {
        try {
            this.unregisterReceiver(receiver);
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
        if (true) {
            this.answer();
        } else {
            this.hangup();
        }
    }

    /**
     * Sends out broadcasts informing listeners of changes in status of the
     * Telephone
     *
     * @param isInCall Boolean indicating whether the Telephone is in a call or not.
     *                 True: in call, False: Not in call
     */
    private void sendTelephonyEventBroadcast(boolean isInCall) {
        Intent intent = new Intent();
        intent.putExtra("Incall", isInCall);
        intent.setAction("org.rootio.services.telephony.TELEPHONY_EVENT");
        this.sendBroadcast(intent);
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
this.callState = CallState.INCALL;
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
                Utils.toastOnScreen("Incoming call...", SipService.this);
                //take calls only if not in other call
                if (SipService.this.callState == CallState.INCALL) {
                    //SipService.this.sipManager.takeAudioCall(intent, null).endCall();
                } else {
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
                SipService.this.sendTelephonyEventBroadcast(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCallEnded(SipAudioCall call) {
            try {
                SipService.this.sipCall = null;
                SipService.this.sendTelephonyEventBroadcast(false);
                SipService.this.callState = CallState.IDLE;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCallEstablished(SipAudioCall call) {
            SipService.this.sendTelephonyEventBroadcast(true);
            SipService.this.callState = CallState.INCALL;


        }

        @Override
        public void onRinging(SipAudioCall call, SipProfile caller) {
            try {
//                SipService.this.sipCall = call;
//                SipService.this.sipCall.answerCall(30);
//                SipService.this.sipCall.startAudio();
                SipService.this.callState = CallState.RINGING;
            } catch (Exception e) {
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
Utils.toastOnScreen("reged", SipService.this);
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
