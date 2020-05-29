package org.rootio.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Call;
import org.linphone.core.Config;
import org.linphone.core.Core;
import org.linphone.core.Factory;
import org.linphone.core.NatPolicy;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;
import org.linphone.core.Transports;
import org.rootio.RootioApp;
import org.rootio.handset.BuildConfig;
import org.rootio.handset.R;
import org.rootio.services.SIP.SipEventsNotifiable;
import org.rootio.services.SIP.SipListener;
import org.rootio.tools.media.Program;
import org.rootio.tools.radio.RadioRunner;
import org.rootio.tools.telephony.CallAuthenticator;
import org.rootio.tools.telephony.CallRecorder;
import org.rootio.tools.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RadioService extends Service implements ServiceInformationPublisher, SipEventsNotifiable {

    private final int serviceId = 4;
    private boolean isRunning;
    private Thread runnerThread;
    private RadioRunner radioRunner;
    private NewDayScheduleHandler newDayScheduleHandler;
    private PendingIntent pi;
    private AlarmManager am;
    private TelephonyManager telephonyManager;
    private TelecomManager telecomManager;
    private RadioService.PhoneCallListener listener;
    private CallRecorder callRecorder;
    //private boolean inCall;
    private String currentCallingNumber;
    private int port, reRegisterPeriod;
    private Core linphoneCore;
    private AuthInfo authInfo;
    private ProxyConfig proxyConfig;
    private String username, password, domain, stun, protocol;
    private boolean isPendingRestart;
    private boolean wasStoppedOnPurpose, isSipRunning;
    private SipListener coreListener;
    private Config profile;
    private BroadcastReceiver br;
    private int callVolume;
    //private boolean inSIPCall;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.logEvent(this, Utils.EventCategory.SERVICES, Utils.EventAction.START, "Radio Service");
        if (!this.isRunning) {
            Utils.doNotification(this, "RootIO", "Radio Service Started");
            this.am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            this.runTodaySchedule();
            this.setupNewDayScheduleListener();
            this.silenceRinger();

            //Telephony service
            this.waitForCalls();
           
            //SIP service
            this.register();
            this.listenForConfigChange();
            this.isRunning = true;
            this.sendEventBroadcast();
        }

        this.startForeground(this.serviceId, Utils.getNotification(this, "RootIO", "Radio Service is running", R.drawable.icon, false, null, null));
        new ServiceState(this, 4, "Radio", 1).save();
        return Service.START_STICKY;
    }

    private void setupNewDayScheduleListener() {
        this.newDayScheduleHandler = new NewDayScheduleHandler();
        IntentFilter intentFilter = new IntentFilter("org.rootio.services.radio.NEW_DAY_SCHEDULE");
        this.registerReceiver(newDayScheduleHandler, intentFilter);
    }

    private void runTodaySchedule() {
        radioRunner = RadioRunner.getInstance(this);
        runnerThread = new Thread(radioRunner);
        runnerThread.start();
        this.scheduleNextDayAlarm();
    }

    private void silenceRinger() {
        try {
            AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_SHOW_UI);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_SHOW_UI);
        } catch (Exception ex) {
            Log.e(this.getString(R.string.app_name), String.format("[RadioService.silenceRinger] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
        }
    }

    @Override
    public void onDestroy() {
        Utils.logEvent(this, Utils.EventCategory.SERVICES, Utils.EventAction.STOP, "Radio Service");
        this.stopForeground(true);
        try {
            this.shutDownService();
        } catch (Exception ex) {
            Log.e(this.getString(R.string.app_name), String.format("[RadioService.onDestroy] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
        }
        new ServiceState(this, 4, "Radio", 0).save();
        super.onDestroy();
    }

    private void shutDownService() {
        if (radioRunner != null && this.isRunning) {
            radioRunner.stop();
            this.isRunning = false;
            try {
                this.unregisterReceiver(newDayScheduleHandler);
            } catch (Exception ex) {
                Log.e(this.getString(R.string.app_name), String.format("[RadioService.shutDownService] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
            }
            this.stopSelf();

            //Telephony
            this.telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);

            //SIP
            this.deregister();
            this.stopListeningForConfigChange();

            Utils.doNotification(this, "RootIO", "Radio Service Stopped");
            this.sendEventBroadcast();
        }
    }

    /**
     * Sends out broadcasts informing listeners of change in the status of the
     * service
     */
    @Override
    public void sendEventBroadcast() {
        Intent intent = new Intent();
        intent.putExtra("serviceId", this.serviceId);
        intent.putExtra("isRunning", this.isRunning);
        intent.setAction("org.rootio.services.radio.EVENT");
        this.sendBroadcast(intent);
    }

    /**
     * Gets the program slots that are defined for the current schedule
     *
     * @return An ArrayList of ProgramSlot objects each representing a slot on
     * the schedule of the radio
     */
    public ArrayList<Program> getPrograms() {
        return radioRunner == null ? new ArrayList<Program>() : radioRunner.getPrograms();
    }

    private void scheduleNextDayAlarm() {
        Date dt = this.getTomorrowBaseDate();
        Intent intent = new Intent("org.rootio.services.radio.NEW_DAY_SCHEDULE");

        //send an intent to restart services
        Intent restartIntent = new Intent("org.rootio.services.RESTART_ALL");
        restartIntent.putExtra("isRestart", true);
        PendingIntent restartPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        this.am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dt.getTime() + 10000, restartPendingIntent);


        //this.pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        //this.am.set(AlarmManager.RTC_WAKEUP, dt.getTime(), this.pi);
    }

    private Date getTomorrowBaseDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    /**
     * Listens for Telephony activity coming into the phone
     */
    private void waitForCalls() {
        this.telecomManager = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
        setInCall(false);
        this.telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        listener = new RadioService.PhoneCallListener();
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void setInCall(boolean inCall)
    {
        //this.inCall = inCall; //local
        RootioApp.setInCall(inCall); //stored in the RootioApp instance
        //persist in app preferences. This will become the preferred approach
        ContentValues values = new ContentValues();
        values.put("inCall", inCall);
        Utils.savePreferences(values, this);
    }

    private void setInSIPCall(boolean inSIPCall)
    {
        //this.inSIPCall = inSIPCall; //local
        RootioApp.setInCall(inSIPCall); //stored in the RootioApp instance
        //persist in app preferences. This will become the preferred approach
        ContentValues values = new ContentValues();
        values.put("inSIPCall", inSIPCall);
        Utils.savePreferences(values, this);
    }

    private boolean getInCall()
    {
        return (boolean)Utils.getPreference("inCall", boolean.class, this);
    }

    private boolean getInSIPCall()
    {
        return (boolean)Utils.getPreference("inSIPCall", boolean.class, this);
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
    private void declineCall(String fromNumber) { //just let the phone ring silenced
//        this.telecomManager.silenceRinger();
//        ITelephony RadioService;
//        TelephonyManager telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//        try {
//            Class c = Class.forName(telephony.getClass().getName());
//            Method m = c.getDeclaredMethod("getITelephony");
//            m.setAccessible(true);
//            RadioService = (ITelephony) m.invoke(telephony);
//            RadioService.endCall();
//            Utils.logEvent(this, Utils.EventCategory.CALL, Utils.EventAction.STOP, fromNumber);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    /**
     * Processes a call noticed by the listener. Determines whether or not to
     * pick the phone call basing on the calling phone number * @param
     * incomingNumber
     */
    public void handleCall(final String fromNumber) {
        if (!this.getInCall() && !this.getInSIPCall() && new CallAuthenticator(this).isWhiteListed(fromNumber)) {
            this.setInCall(true);
            try{
            if(this.radioRunner != null && this.radioRunner.getRunningProgram() != null) {
                this.radioRunner.getRunningProgram().pause();
                //RadioService.this.sendTelephonyEventBroadcast(true);
            }}
            catch(Exception ex)
            {
                Log.e(this.getString(R.string.app_name), String.format("[RadioService.handleCall] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000); // Music thread is fading out
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pickCall(fromNumber);

                    //mute any music that might be playing
                    AudioManager audioManager = (AudioManager) RadioService.this.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
                    // this.setupCallRecording(); //not possible on pockets
                }
            }).start();
        } else {
            declineCall(fromNumber);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.coreListener = new SipListener(this);
        this.initializeStack();
    }

    private void listenForConfigChange() {
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (RadioService.this.isRunning) {
                    if (RadioService.this.getInCall()) //re-registration will cause call to be dropped
                    {
                        RadioService.this.isPendingRestart = true;
                    } else {
                        RadioService.this.deregister();
                        RadioService.this.initializeStack();
                        RadioService.this.register();
                    }
                }
            }
        };
        IntentFilter fltr = new IntentFilter();
        fltr.addAction("org.rootio.handset.SIP.CONFIGURATION_CHANGE");
        this.registerReceiver(br, fltr);
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
        return new BindingAgent(this);
    }


    private void loadConfig() {
        String stationInformation = (String) Utils.getPreference("station_information", String.class, this);
        JSONObject stationJson;
        try {
            stationJson = new JSONObject(stationInformation).optJSONObject("station");
            JSONObject sipConfiguration = new JSONObject(stationJson.optString("sip_settings")); //optJSONObject("sip_settings"); the sip config is JSON as a string in a JSON file :-(
            this.domain = sipConfiguration.optString("sip_domain");
            this.username = sipConfiguration.optString("sip_username");
            this.password = sipConfiguration.optString("sip_password");
            this.stun = sipConfiguration.optString("sip_stun");
            this.protocol = sipConfiguration.optString("protocol", "udp");
            this.port = sipConfiguration.optInt("sip_port", 5060);
            this.reRegisterPeriod = sipConfiguration.optInt("sip_reregister_period", 30);
            this.callVolume = sipConfiguration.optInt("call_volume", 5);
            if (this.callVolume <= 0 || this.callVolume > 5) //unacceptable values
            {
                this.callVolume = 5;
            }
        } catch (JSONException ex) {
            Log.e(this.getString(R.string.app_name), String.format("[RadioService.loadConfig] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
        }
    }

    private NatPolicy createNatPolicy() {
        NatPolicy natPolicy = linphoneCore.createNatPolicy();
        natPolicy.setStunServer(this.stun);

        //natPolicy.enableTurn(true);
        natPolicy.enableIce(true);
        natPolicy.enableStun(true);

        natPolicy.resolveStunServer();
        return natPolicy;
    }

    private void prepareProxy() {
        this.proxyConfig = linphoneCore.createProxyConfig();
        Transports trns = Factory.instance().createTransports();
        trns.setUdpPort(12312);
        trns.setTcpPort(12312);
        this.linphoneCore.setTransports(trns);

        //The address of the peer
        Address addr = Factory.instance().createAddress(String.format("sip:%s@%s:%s", this.username, this.domain, this.port));
        addr.setPort(this.port);
        addr.setTransport(this.protocol.toLowerCase().equals("udp") ? TransportType.Udp : TransportType.Tcp);

        //the address of the SIP server
        Address proxy = Factory.instance().createAddress(String.format("sip:%s:%s", this.domain, 12312)); // this.port)); avoid default port, easily spammed
        proxy.setTransport(this.protocol.toLowerCase().equals("udp") ? TransportType.Udp : TransportType.Tcp);
        proxy.setPort(12312);


        this.authInfo = Factory.instance().createAuthInfo(addr.getUsername(), null, this.password, null, null, null);
        this.linphoneCore.addAuthInfo(authInfo);


        this.proxyConfig.setIdentityAddress(addr);
        this.proxyConfig.setServerAddr(String.format("sip:%s:%s", this.domain, this.port));
        //this.proxyConfig.setServerAddr(proxy.getDomain());


        this.proxyConfig.setNatPolicy(this.createNatPolicy()); //use STUN. There is every chance you are on a NATted network

        //Registration deets
        this.proxyConfig.setExpires(this.reRegisterPeriod);
        this.proxyConfig.enableRegister(true);


        this.linphoneCore.addProxyConfig(this.proxyConfig);
        this.linphoneCore.setDefaultProxyConfig(this.proxyConfig);
        this.linphoneCore.setRingerDevice(null);
        this.linphoneCore.setRing(null);
    }

    private void prepareSipProfile() {
        this.profile = Factory.instance().createConfigFromString(""); //Config string above was tripping the client, would not connect..

    }

    void register() {
        if (this.linphoneCore != null) { //LinPhone core can be null if initialized with wrong parameters!
            this.linphoneCore.removeListener(coreListener);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //This is a strange flow, but for STUN/TURN to kick in, you need to register first, then unregister and register again!
                    //The registration triggers a stun update but it can only be used on next registration. That's what it looks like for now
                    //so, register for 5 sec, then re-register

                    sipRegister();
                    try {
                        linphoneCore.addListener(coreListener);
                        Thread.sleep(5000);//too little and linphoneCore may not process our events due to backlog/network delay, too high and we sleep too long..
                        deregister();
                        Thread.sleep(5000);//too little and linphoneCore may not process our events due to backlog/network delay, too high and we sleep too long..
                        sipRegister();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void sipRegister() {

        linphoneCore.getDefaultProxyConfig().edit();
        linphoneCore.getDefaultProxyConfig().enableRegister(true);
        linphoneCore.getDefaultProxyConfig().done();
    }

    void initializeStack() {
        try {
            this.loadConfig();
            if (this.username.isEmpty() || this.password.isEmpty() || this.domain.isEmpty()) {
                Utils.toastOnScreen("Can't register! Username, password or domain is missing!", this);
                this.updateRegistrationState(RegistrationState.None, null);
                return;
            }

            this.prepareSipProfile();
            this.linphoneCore = Factory.instance().createCoreWithConfig(profile, this);
            this.prepareProxy();

            linphoneCore.start();
            isSipRunning = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isSipRunning) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        RadioService.this.linphoneCore.iterate();
                    }
                }
            }).start();
        } catch (Exception ex) {
            Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(RadioService.initializeStack)" : ex.getMessage());
        }
    }

    public void deregister() {
        try {
            if (linphoneCore.inCall()) {
                try { //state might change between check and termination call..
                    this.linphoneCore.getCurrentCall().terminate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            this.linphoneCore.getDefaultProxyConfig().edit();
            this.linphoneCore.getDefaultProxyConfig().enableRegister(false);
            this.linphoneCore.getDefaultProxyConfig().done();
        } catch (Exception ex) {
            Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(RadioService.deregister)" : ex.getMessage());
        }
    }

    /**
     * Process an incoming SIP call: Typically check the whitelist to make sure that the number is allowed to call this station
     */
    private void handleCall(Call call) {
        //check the whitelist
        Log.e("RootIO", "handleCall: " + call.getRemoteAddress().getDomain() + " " + this.domain);
        if (!this.getInCall() && !this.getInSIPCall() && call.getRemoteAddress().getDomain().equals(this.domain)) //Guard against spoofing..
        {
                Log.e("RootIO", "handleCall: " +getInCall() + " " + getInSIPCall());
            //RootioApp.setInSIPCall(true);
            setInSIPCall(true);
            //this.inSIPCall = true;
            try{
                if(this.radioRunner != null && this.radioRunner.getRunningProgram() != null) {
                    this.radioRunner.getRunningProgram().pause();
                    //RadioService.this.sendTelephonyEventBroadcast(true);
                }}
            catch(Exception ex)
            {
                Log.e(this.getString(R.string.app_name), String.format("[RadioService.handleCall] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
            }
            //this.sendTelephonyEventBroadcast(true);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try{
                if(this.radioRunner != null && this.radioRunner.getRunningProgram() != null) {
                    this.radioRunner.getRunningProgram().pause();
                    //RadioService.this.sendTelephonyEventBroadcast(true);
                }}
            catch(Exception ex)
            {
                Log.e(this.getString(R.string.app_name), String.format("[RadioService.handleCall] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
            }
            this.answer(call);


        } else {
            this.hangup(call);
        }
    }

    /**
     * Terminate a SIP call that has been taken over by this service
     */
    public void hangup(Call call) {
        try {
            call.terminate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Answer a SIP call that has been taken over by this service
     */
    public void answer(final Call call) {
        try {
            call.accept();
            call.setSpeakerVolumeGain(1.0f);
            // adjust the volume of the telephony stream
            AudioManager audioManager = (AudioManager) RadioService.this.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, this.callVolume, AudioManager.FLAG_SHOW_UI);

            //mute any music that might be playing
            //AudioManager audioManager = (AudioManager) RadioService.this.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
            Utils.logEvent(this, Utils.EventCategory.SIP_CALL, Utils.EventAction.START, call.getRemoteContact());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getServiceId() {
        return this.serviceId;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void updateCallState(Call.State callState, Call call) {
        switch (callState) {
            case End:
                if (call.getRemoteAddress().getDomain().equals(this.domain) && this.getInSIPCall()) {
                    //this.inSIPCall = false;
                    if (isPendingRestart) {
                        this.deregister();
                        this.initializeStack();
                        this.register();
                        isPendingRestart = false;
                    }
                    //RootioApp.setInSIPCall(false);
                    setInSIPCall(false);
                    try{
                        if(this.radioRunner != null && this.radioRunner. getRunningProgram() != null) {
                            this.radioRunner.getRunningProgram().resume();
                            //RadioService.this.sendTelephonyEventBroadcast(true);
                        }}
                    catch(Exception ex)
                    {
                        Log.e(this.getString(R.string.app_name), String.format("[RadioService.handleCall] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
                    }
                    //this.sendTelephonyEventBroadcast(false);
                    Utils.logEvent(this, Utils.EventCategory.SIP_CALL, Utils.EventAction.STOP, call != null ? call.getRemoteContact() : "");
                    Log.e("RootIO", "updateCallState: End");
                    if (call != null) //not being sent au moment
                    {
                        Utils.toastOnScreen("Call with " + call != null ? call.getRemoteContact() : "" + " ended", this);
                    }
                    //up any music that might be playing
                    AudioManager audioManager = (AudioManager) RadioService.this.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, getMaxVolume() > 9? 9: getMaxVolume(), AudioManager.FLAG_SHOW_UI);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, getMaxVolume(), AudioManager.FLAG_SHOW_UI);
                }
                break;
            case Error:
                //this.inCall = false;
                if (isPendingRestart) {
                    this.deregister();
                    this.initializeStack();
                    this.register();
                    isPendingRestart = false;
                }
                Utils.logEvent(this, Utils.EventCategory.SIP_CALL, Utils.EventAction.STOP, call != null ? call.getRemoteContact() : "");
                //this.sendTelephonyEventBroadcast(false);
                Log.e("RootIO", "updateCallState: Error");
                if (call != null) //not being sent au moment
                {
                    Utils.toastOnScreen("Call with " + call != null ? call.getRemoteContact() : "" + " erred", this);
                }
                break;
            case Connected:
                Log.e("RootIO", "updateCallState: Connected");
            case StreamsRunning: //in case you reconnect to the main activity during call.
                //this.inSIPCall = true;
                setInSIPCall(true);
                try{
                    if(this.radioRunner != null && this.radioRunner.getRunningProgram() != null) {
                        this.radioRunner.getRunningProgram().pause();
                        //RadioService.this.sendTelephonyEventBroadcast(true);
                    }}
                catch(Exception ex)
                {
                    Log.e(this.getString(R.string.app_name), String.format("[RadioService.handleCall] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
                }
                //this.sendTelephonyEventBroadcast(true);
                if (call != null) //ideally check for direction and report if outgoing or incoming
                {
                    Utils.toastOnScreen("In call with " + call != null ? call.getRemoteContact() : "", this);
                }
                Utils.logEvent(this, Utils.EventCategory.SIP_CALL, Utils.EventAction.START, call != null ? call.getRemoteContact() : "");
                Log.e("RootIO", "updateCallState: StreamsRunning");
                break;
            case IncomingReceived:
                this.linphoneCore.stopRinging();
                if (call != null) {
                    Utils.toastOnScreen("Incoming call from " + call != null ? call.getRemoteContact() : "", this);
                    this.handleCall(call); //check WhiteList first!!
                }
                Utils.logEvent(this, Utils.EventCategory.SIP_CALL, Utils.EventAction.RINGING, call != null ? call.getRemoteContact() : "");
                break;
            case OutgoingInit:
                if (call != null) {
                    Utils.toastOnScreen("Dialling out to" + call != null ? call.getRemoteContact() : "", this);
                }
                break;
            default: //handles 11 other states!
                break;
        }
    }

    private int getMaxVolume() {
        String stationInfo = (String) Utils.getPreference("station_information", String.class, this);
        try {
            JSONObject stationInfoJson = new JSONObject(stationInfo);
            if (stationInfoJson.has("station") && stationInfoJson.getJSONObject("station").has("media_volume")) {
                int volume = stationInfoJson.getJSONObject("station").getInt("media_volume");
                return volume >= 0 && volume <= 15 ? volume : 8;
            } else
                return 8;
        } catch (Exception ex) {
            Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(RadioService.getMaxVolume)" : ex.getMessage());
        }
        return 8;
    }

    private void stopListeningForConfigChange() {
        this.unregisterReceiver(br);
    }

    @Override
    public void updateRegistrationState(RegistrationState registrationState, ProxyConfig proxyConfig) {
        if (registrationState != null) { //could be sent before listener has any notifs, e.g when this service connects to service before registration
            Utils.logEvent(this, Utils.EventCategory.SIP_CALL, Utils.EventAction.REGISTRATION, registrationState.name());
            switch (registrationState) {
                case Progress:
                    if (BuildConfig.DEBUG) Utils.toastOnScreen("Registering...", this);
                    break;
                case Ok:
                    if (proxyConfig != null) {
                        if (BuildConfig.DEBUG)
                            Utils.toastOnScreen("Registered " + proxyConfig.getIdentityAddress().getUsername() + "@" + proxyConfig.getServerAddr(), this);
                    }
                    break;
                case None:
                case Cleared:
                case Failed:
                    if (proxyConfig != null) {
                        if (BuildConfig.DEBUG)
                            Utils.toastOnScreen("Unregistered " + proxyConfig.getIdentityAddress().asString(), this);
                    }
            }
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

    class NewDayScheduleHandler extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            RadioService.this.radioRunner.stop();
            try {
                //RadioService.this.finalize();
                //intent.putExtra("isRestart", true);
                //new BootMonitor().onReceive(context, intent);

            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            RadioService.this.runTodaySchedule();

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
                    if (!getInCall() && !getInSIPCall()) {
                        currentCallingNumber = incomingNumber;
                        Utils.logEvent(RadioService.this, Utils.EventCategory.CALL, Utils.EventAction.RINGING, incomingNumber);
                        handleCall(incomingNumber);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (incomingNumber.equals(currentCallingNumber)) {
                        //inCall = false;
                       // RootioApp.setInCall(false);
                        setInCall(false);
                        try{
                            if(RadioService.this.radioRunner != null && RadioService.this.radioRunner.getRunningProgram() != null) {
                                RadioService.this.radioRunner.getRunningProgram().resume();
                                //RadioService.this.sendTelephonyEventBroadcast(true);
                            }}
                        catch(Exception ex)
                        {
                            Log.e(RadioService.this.getString(R.string.app_name), String.format("[RadioService.handleCall] %s", ex.getMessage() == null ? "Null pointer exception" : ex.getMessage()));
                        }
                        //RadioService.this.sendTelephonyEventBroadcast(false);
                        if (RadioService.this.callRecorder != null) {
                            RadioService.this.callRecorder.stopRecording();
                            RadioService.this.callRecorder = null;
                        }
                    }

                    if(!getInSIPCall()) {
                        AudioManager audioManager = (AudioManager) RadioService.this.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, getMaxVolume() > 9? 9: getMaxVolume(), AudioManager.FLAG_SHOW_UI);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, getMaxVolume(), AudioManager.FLAG_SHOW_UI);
                    }

                    Utils.logEvent(RadioService.this, Utils.EventCategory.CALL, Utils.EventAction.STOP, incomingNumber);
                    break;
            }
        }
    }

}
