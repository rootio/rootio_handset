package org.rootio.services;

import java.lang.reflect.Method;

import org.rootio.handset.BuildConfig;
import org.rootio.handset.R;
import org.rootio.tools.telephony.CallAuthenticator;
import org.rootio.tools.telephony.CallRecorder;
import org.rootio.tools.utils.Utils;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;

public class TelephonyService extends Service implements ServiceInformationPublisher {

    private boolean isRunning;
    private final int serviceId = 1;
    private TelephonyManager telephonyManager;
    private TelecomManager telecomManager;
    private PhoneCallListener listener;
    private CallRecorder callRecorder;

    @Override
    public IBinder onBind(Intent arg0) {
        BindingAgent bindingAgent = new BindingAgent(this);
        return bindingAgent;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            Utils.doNotification(this, "RootIO", "Telephony Service started");
            this.waitForCalls();
            this.isRunning = true;
            this.sendEventBroadcast();
        }
        this.startForeground(this.serviceId, Utils.getNotification(this, "RootIO", "Telephony service is running", R.drawable.icon, false, null, null));
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
            Utils.doNotification(this, "RootIO", "Telephony Service stopped");
            this.isRunning = false;
            this.telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
            this.sendEventBroadcast();
        }
    }

    /**
     * Listens for Telephony activity coming into the phone
     */
    private void waitForCalls() {
        this.telecomManager = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
        this.telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        listener = new PhoneCallListener();
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * Answers an incoming call
     */
    private void pickCall(String frommNumber) {
        if (BuildConfig.DEBUG) {
            Utils.toastOnScreen("call ringing...", this);
        }
        if (Build.VERSION.SDK_INT >= 26) //Oreo onwards
        {
            try {
                Thread.sleep(500); //Otherwise the call proceeds to the system ringer. Tested on Samsung A6+
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            this.telecomManager.acceptRingingCall();
            Utils.logEvent(this, Utils.EventCategory.CALL, Utils.EventAction.START, frommNumber);
        } else { //hail mary. This works only for Kitkat and below

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Runtime.getRuntime().exec("input event " + KeyEvent.KEYCODE_HEADSETHOOK);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();

            Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
            Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
            buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
            buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
            try {
                this.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
                this.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent headSetUnPluggedintent = new Intent(Intent.ACTION_HEADSET_PLUG);
            headSetUnPluggedintent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
            headSetUnPluggedintent.putExtra("state", 1); // 0 = unplugged 1 =
            // Headset with
            // microphone 2 =
            // Headset without
            // microphone
            headSetUnPluggedintent.putExtra("name", "Headset");
            try {
                this.sendOrderedBroadcast(headSetUnPluggedintent, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // adjust the volume
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FLAG_SHOW_UI);


    }

    /**
     * Declines an incoming call or ends an ongoing call.
     */
    private void declineCall(String fromNumber) {
        ITelephony telephonyService;
        TelephonyManager telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class c = Class.forName(telephony.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            telephonyService = (ITelephony) m.invoke(telephony);
            telephonyService.endCall();
            Utils.logEvent(this, Utils.EventCategory.CALL, Utils.EventAction.STOP, fromNumber);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Processes a call noticed by the listener. Determines whether or not to
     * pick the phone call basing on the calling phone number * @param
     * incomingNumber
     */
    public void handleCall(String fromNumber) {
        if (true || new CallAuthenticator(this).isWhiteListed(fromNumber)) {
            this.sendTelephonyEventBroadcast(true);
            pickCall(fromNumber);
            // this.setupCallRecording(); //not possible on pockets
        } else {
            declineCall(fromNumber);
        }
    }


    /**
     * Class to handle telephony events received by the phone
     *
     * @author Jude Mukundane
     */
    class PhoneCallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Utils.logEvent(TelephonyService.this, Utils.EventCategory.CALL, Utils.EventAction.RINGING, incomingNumber);
                    handleCall(incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    TelephonyService.this.sendTelephonyEventBroadcast(false);
                    if (TelephonyService.this.callRecorder != null) {
                        TelephonyService.this.callRecorder.stopRecording();
                        TelephonyService.this.callRecorder = null;
                    }
                    Utils.logEvent(TelephonyService.this, Utils.EventCategory.CALL, Utils.EventAction.STOP, incomingNumber);
                    break;
            }
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Sends out broadcasts informing listeners of change in service state
     */
    @Override
    public void sendEventBroadcast() {
        Intent intent = new Intent();
        intent.putExtra("serviceId", this.serviceId);
        intent.putExtra("isRunning", this.isRunning);
        intent.setAction("org.rootio.services.telephony.EVENT");
        this.sendBroadcast(intent);
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

    @Override
    public int getServiceId() {
        return this.serviceId;
    }
}
