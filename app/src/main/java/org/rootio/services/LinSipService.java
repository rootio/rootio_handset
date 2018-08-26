package org.rootio.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
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
import org.rootio.services.SIP.CallState;
import org.rootio.services.SIP.SipEventsNotifiable;
import org.rootio.services.SIP.SipListener;
import org.rootio.tools.utils.Utils;

public class LinSipService extends Service implements ServiceInformationPublisher, SipEventsNotifiable {

    private final int serviceId = 6;
    private Core linphoneCore;
    private ProxyConfig proxyConfig;
    private CallState callState = CallState.IDLE;
    private String username, password, domain, stunServer;
    private SharedPreferences prefs;
    private boolean isRunning;
    private boolean wasStoppedOnPurpose;
    private SipListener callListener;
    private boolean isSipRunning;


    @Override
    public void onCreate() {
        this.prefs = this.getSharedPreferences("org.rootio.handset", Context.MODE_PRIVATE);
        this.callListener = new SipListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            this.register();
            Utils.doNotification(this, "RootIO", "LinSip Service Started");
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
            this.domain = "89.109.64.165"; // prefs.getString("org.rootio.handset.sip_domain", "");
            this.username = "1001";// prefs.getString("org.rootio.handset.sip_username", "");
            this.password = "this_was_password"; //prefs.getString("org.rootio.handset.sip_password", "");
            this.stunServer = "stun.zoiper.com:3478"; // //prefs.getString("org.rootio.handset.sip_password", "");
        }
    }

    private NatPolicy createNatPolicy() {
        NatPolicy natPolicy = this.linphoneCore.createNatPolicy();
        natPolicy.enableStun(true);
        natPolicy.setStunServer(this.stunServer);//server address in the form <address:port>
        return natPolicy;
    }

    /**
     * Creates a SIP profile from the profile information supplied from the cloud platform
     */
    private void prepareSipProfile() {
        try {

            Config profile = Factory.instance().createConfigFromString(""); //no configuration, defaults assumed
            this.linphoneCore = Factory.instance().createCoreWithConfig(profile, this);
            Address peer = Factory.instance().createAddress(String.format("sip:%s@%s", this.username, this.domain));
            AuthInfo authInfo = Factory.instance().createAuthInfo(peer.getUsername(), null, this.password, null, null, null);
            this.linphoneCore.addAuthInfo(authInfo);

            //create the proxy element
            this.proxyConfig = this.linphoneCore.createProxyConfig();
            this.proxyConfig.setIdentityAddress(peer);
            this.proxyConfig.setServerAddr(peer.getDomain());

            //set registration deets
            this.proxyConfig.setExpires(3600); //1 hour
            this.proxyConfig.enableRegister(true);
            this.proxyConfig.setNatPolicy(this.createNatPolicy()); //STUN attempt 1

            //add the proxy config to the core
            this.linphoneCore.addProxyConfig(this.proxyConfig);
            this.linphoneCore.setDefaultProxyConfig(this.proxyConfig);
            this.linphoneCore.setStunServer(this.stunServer); //STUN attempt again. Not sure if this or above is used

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean register() {
        try {
            this.loadConfig();
            if (this.username == "" || this.password == "" || this.domain == "") //Some servers may take blank username or passwords. modify accordingly..
            {
                //ideally this only happen in an unreged state
                Utils.toastOnScreen("Some configuration information is missing. SIP registration not possible", this);
                return false;
            }
            this.prepareSipProfile();
            this.linphoneCore.addListener(this.callListener);
            this.isSipRunning = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (LinSipService.this.isSipRunning) {
                        LinSipService.this.linphoneCore.iterate();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deregister() {
        try {
            if (this.linphoneCore.inCall()) {
                try {
                    this.linphoneCore.getCurrentCall().terminate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            this.linphoneCore.getDefaultProxyConfig().edit();
            this.linphoneCore.getDefaultProxyConfig().enableRegister(false);
            this.linphoneCore.getDefaultProxyConfig().done();
            this.linphoneCore.clearProxyConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process an incoming SIP call: Typically check the whitelist to make sure that the number is allowed to call this station
     */
    private void handleCall(Call call) {
        //check the whitelist
        if (true) {
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


    @Override
    public void updateCallState(Call.State callState, Call call, ContentValues values) {
        if (call != null && callState != null) //this notif could come in before the listener connects to SIP server
        {
            switch (callState) {
                case End:
                case Error:
                    if (values != null && values.containsKey("otherParty")) //not being sent au moment
                    {
                        Utils.toastOnScreen("Call with " + values.getAsString("otherParty") + " terminated", this);
                    }
                    break;
                case Connected:
                case StreamsRunning: //in case you reconnect to the main activity during call.
                    if (values != null && values.containsKey("otherParty")) //ideally check for direction and report if outgoing or incoming
                    {
                        Utils.toastOnScreen("In call with " + values.getAsString("otherParty"), this);
                    }
                    break;
                case IncomingReceived:
                    if (values != null && values.containsKey("otherParty")) {
                        Utils.toastOnScreen("Incoming call from " + values.getAsString("otherParty"), this);
                        this.handleCall(call); //check WhiteList first!!
                    }
                    break;
                case OutgoingInit:
                    if (values != null && values.containsKey("otherParty")) {
                        Utils.toastOnScreen("Dialling out... ", this);
                    }
                    break;
                default: //handles 13 other states!
                    break;
            }
        }
    }

    @Override
    public void updateRegistrationState(RegistrationState registrationState, ContentValues values) {
        if (registrationState != null) { //could be sent before listener has any notifs, e.g when this service connects to service before registration
            switch (registrationState) {
                case Progress:
                    Utils.toastOnScreen("Registering...", this);
                    break;
                case Ok:
                    if (values != null && values.containsKey("username")) //Requested notifications will not come with values
                    {
                        Utils.toastOnScreen("Registered " + values.getAsString("username") + "@" + values.getAsString("domain"), this);
                    }
                    break;
                case None:
                case Cleared:
                case Failed:
                    if (values != null && values.containsKey("localProfileUri")) {
                        Utils.toastOnScreen("Unregistered " + values.getAsString("localProfileUri"), this);
                    }
            }
        }
    }
}
