package org.rootio.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Call;
import org.linphone.core.Config;
import org.linphone.core.Core;
import org.linphone.core.Factory;
import org.linphone.core.NatPolicy;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.Transports;
import org.rootio.handset.R;
import org.rootio.services.SIP.SipEventsNotifiable;
import org.rootio.services.SIP.SipListener;
import org.rootio.tools.utils.Utils;

public class LinSipService extends Service implements ServiceInformationPublisher, SipEventsNotifiable {

    private final int serviceId = 6;
    private Core linphoneCore;
    private Config sipConfig;
    private AuthInfo authInfo;
    private ProxyConfig proxyConfig;
    private String username, password, domain, stunServer;
    private SharedPreferences prefs;
    private boolean isRunning;
    private boolean wasStoppedOnPurpose;
    private SipListener coreListener;
    private boolean isSipRunning;
    private String stun;
    private Config profile;


    @Override
    public void onCreate() {
        super.onCreate();
        this.coreListener = new SipListener(this);
        this.initializeStack();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            this.register();
            Utils.doNotification(this, "RootIO", "LinSip Service Started");
            this.sendEventBroadcast();
        }
        this.startForeground(this.serviceId, Utils.getNotification(this, "RootIO", "LinSIP service is running", R.drawable.icon, false, null, null));
        return Service.START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        Utils.toastOnScreen("being stopped (ontskr", this);
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

    @Override
    public void onDestroy() {
      this.stopForeground(true);
      this.shutDownService();
      super.onDestroy();
    }


    private void loadConfig() {



        this.domain = (String)Utils.getPreference("org.rootio.handset.sip_domain", String.class, this);
        this.username = (String)Utils.getPreference("org.rootio.handset.sip_username", String.class, this);
        this.password = (String)Utils.getPreference("org.rootio.handset.sip_password", String.class, this);
        this.stun = (String)Utils.getPreference("org.rootio.handset.sip_stun", String.class, this);
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
        trns.setUdpPort(-1);
        trns.setTcpPort(-1);
        this.linphoneCore.setTransports(trns);

        //The address of the peer
        Address addr = Factory.instance().createAddress(String.format("sip:%s@%s", this.username, this.domain));
        Address proxy = Factory.instance().createAddress("sip:" + this.domain);
        this.authInfo = Factory.instance().createAuthInfo(addr.getUsername(), null, this.password, null, null, null);
        this.linphoneCore.addAuthInfo(authInfo);


        this.proxyConfig.setIdentityAddress(addr);
        this.proxyConfig.setServerAddr(proxy.getDomain());

        this.proxyConfig.setNatPolicy(this.createNatPolicy()); //use STUN. There is every chance you are on a NATted network

        //Registration deets
        this.proxyConfig.setExpires(2000);
        this.proxyConfig.enableRegister(false);

        this.linphoneCore.addProxyConfig(this.proxyConfig);
        this.linphoneCore.setDefaultProxyConfig(this.proxyConfig);
    }

    private void prepareSipProfile() {
        this.profile = Factory.instance().createConfigFromString(""); //Config string above was tripping the client, would not connect..

    }

    void register() {
        this.linphoneCore.removeListener(coreListener);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //This is a strange flow, but for STUN/TURN to kick in, you need to register first, then unregister and register again!
                //The registration triggers a stun update but it can only be used on next registration. That's what it looks like for now
                //so, register for 1 sec, then re-register

                sipRegister();
                try {
                    linphoneCore.addListener(coreListener);
                    Thread.sleep(1000);//too little and linphoneCore may not process our events due to backlog/network delay, too high and we sleep too long..
                    deregister();
                    Thread.sleep(1000);//too little and linphoneCore may not process our events due to backlog/network delay, too high and we sleep too long..
                    sipRegister();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                        LinSipService.this.linphoneCore.iterate();
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();

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
            //this.isRunning = false;
            //this.linphoneCore.clearProxyConfig(); //only thing similar to deregistration

        } catch (Exception e) {
            e.printStackTrace();
            //this.notifyRegistrationEvent(this.registrationState, null); //potential conflict of handling to the receiver
        }
    }
    /**
     * Process an incoming SIP call: Typically check the whitelist to make sure that the number is allowed to call this station
     */
    private void handleCall(Call call) {
        //check the whitelist
        if (true /*whitelisted()*/) {
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
    public void answer(Call call) {
        try {
            call.accept();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shutDownService() {
        if (this.isRunning) {
            this.isRunning = this.isSipRunning = false;
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


    @Override
    public void updateCallState(Call.State callState, Call call) {
        switch (callState) {
            case End:
            case Error:
                this.sendTelephonyEventBroadcast(false);
                if (call != null) //not being sent au moment
                {
                    Utils.toastOnScreen("Call with " + call.getRemoteContact() + " terminated", this);
                }
                break;
            case Connected:
            case StreamsRunning: //in case you reconnect to the main activity during call.
                this.sendTelephonyEventBroadcast(true);
                if (call != null) //ideally check for direction and report if outgoing or incoming
                {
                    Utils.toastOnScreen("In call with " + call.getRemoteContact(), this);
                }
                break;
            case IncomingReceived:
                if (call != null) {
                    Utils.toastOnScreen("Incoming call from " + call.getRemoteContact(), this);
                    this.handleCall(call); //check WhiteList first!!
                }
                break;
            case OutgoingInit:
                if (call != null) {
                    Utils.toastOnScreen("Dialling out to" + call.getRemoteContact(), this);
                }
                break;
            default: //handles 11 other states!
                break;
        }
    }

    @Override
    public void updateRegistrationState(RegistrationState registrationState, ProxyConfig proxyConfig) {
        if (registrationState != null) { //could be sent before listener has any notifs, e.g when this service connects to service before registration
            switch (registrationState) {
                case Progress:
                    Utils.toastOnScreen("Registering...", this);
                    break;
                case Ok:
                    if (proxyConfig != null) {
                        Utils.toastOnScreen("Registered " + proxyConfig.getIdentityAddress().getUsername() + "@" + proxyConfig.getServerAddr(), this);
                    }
                    break;
                case None:
                case Cleared:
                case Failed:
                    if (proxyConfig != null) {
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
}
