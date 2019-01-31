package org.rootio.tools.sms;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

public class USSDSMSHandler implements MessageProcessor, UssdResultNotifiable {

    private String[] messageParts;
    private Context parent;
    private String from;

    USSDSMSHandler(Context parent, String from, String[] messageParts) {
        this.parent = parent;
        this.from = from;
        this.messageParts = messageParts;
    }

    @Override
    public boolean ProcessMessage() {
        //uncomment below if you are doing one-hit USSD requests and do not care for multiple requests in single session
        //if ((this.messageParts[1].startsWith("*") || this.messageParts[1].startsWith("#")) && this.messageParts[1].endsWith("#")) {
            //Utils.toastOnScreen("received " + messageParts[1]);
            this.doUSSDRequest(messageParts[1]);
            return true;
        //}
       // return false;
    }

    @Override
    public void respondAsyncStatusRequest(String from, String data) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(from, null, data, null, null);
    }

    private void doUSSDRequest(String USSDString) {
        Utils.toastOnScreen("calling USSD" + Calendar.getInstance().getTimeInMillis(), this.parent);
        if (ActivityCompat.checkSelfPermission(this.parent, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            this.respondAsyncStatusRequest(this.from, this.parent.getString(R.string.NecessaryPermissionsMissing));
        } else {
            new USSDSessionHandler(this.parent, this).doSession(USSDString);
        }
    }

    @Override
    public void notifyUssdResult(String request, String response, int resultCode) {
        this.respondAsyncStatusRequest(this.from, "%s|%s".format(response, resultCode));
    }

    class USSDSessionHandler {

        TelephonyManager tm;
        private UssdResultNotifiable client;
        private Method handleUssdRequest;
        private Object iTelephony;

        USSDSessionHandler(Context parent, UssdResultNotifiable client) {
            this.client = client;
            this.tm = (TelephonyManager) parent.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                this.getUssdRequestMethod();
            } catch (Exception ex) {
                //log
            }

        }

        private void getUssdRequestMethod() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            if (tm != null) {
                Class telephonyManagerClass = Class.forName(tm.getClass().getName());
                if (telephonyManagerClass != null) {
                    Method getITelephony = telephonyManagerClass.getDeclaredMethod("getITelephony");
                    getITelephony.setAccessible(true);
                    this.iTelephony = getITelephony.invoke(tm); // Get the internal ITelephony object
                    Method[] methodList = iTelephony.getClass().getMethods();
                    this.handleUssdRequest = null;
                    /*
                     *  Somehow, the method wouldn't come up if I simply used:
                     *  iTelephony.getClass().getMethod('handleUssdRequest')
                     */

                    for (Method _m : methodList) {
                         if (_m.getName().equals("handleUssdRequest")) {
                            handleUssdRequest = _m;
                            break;
                        }
                    }
                }
            }
        }

        public void doSession(String ussdRequest) {
            try {

                Utils.toastOnScreen("invoking ussd for "+ussdRequest, USSDSMSHandler.this.parent);
                if (handleUssdRequest != null) {
                    handleUssdRequest.setAccessible(true);
                    handleUssdRequest.invoke(iTelephony, ThreadLocalRandom.current().nextInt(30000), ussdRequest, new ResultReceiver(new Handler()) {

                        @Override
                        protected void onReceiveResult(int resultCode, Bundle ussdResponse) {
                            /*
                             * Usually you should the getParcelable() response to some Parcel
                             * child class but that's not possible here, since the "UssdResponse"
                             * class isn't in the SDK so we need to(
                             * reflect again to get the result of getReturnMessage() and
                             * finally return that!
                             */

                            Object p = ussdResponse.getParcelable("USSD_RESPONSE");

                            if (p != null) {
                                try {
                                    CharSequence returnMessage = (CharSequence) p.getClass().getMethod("getReturnMessage").invoke(p);
                                    CharSequence request = (CharSequence) p.getClass().getMethod("getUssdRequest").invoke(p);
                                    USSDSessionHandler.this.client.notifyUssdResult("" + request, "" + returnMessage, resultCode); //they could be null
                                 } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    });
                }
            } catch (IllegalAccessException | InvocationTargetException e1) {
                e1.printStackTrace();
            }
        }
    }
}
