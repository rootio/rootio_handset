package org.rootio.services.synchronization;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.services.SynchronizationService;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

public class SynchronizationDaemon implements Runnable {
    private final Context parent;
    private final Cloud cloud;

    @Override
    public void run() {

        while (((SynchronizationService) this.parent).isRunning()) {
            // turn on mobile data and wait
            //this.toggleData(true);
            this.getSomeSleep(15000);

            // do the sync
            this.synchronize(new DiagnosticsHandler(this.parent, this.cloud));
            this.synchronize(new ProgramsHandler(this.parent, this.cloud));
            this.synchronize(new CallLogHandler(this.parent, this.cloud));
            this.synchronize(new SMSLogHandler(this.parent, this.cloud));
            this.synchronize(new WhitelistHandler(this.parent, this.cloud));
            this.synchronize(new FrequencyHandler(this.parent, this.cloud));
            this.synchronize(new StationHandler(this.parent, this.cloud));
            this.synchronize(new MusicListHandler(this.parent, this.cloud));
            this.synchronize(new PlaylistHandler(this.parent, this.cloud));

            // turn off mobile data
            //this.toggleData(false);

            this.getSomeSleep(this.getFrequency() * 1000); // seconds to milliseconds

        }
    }

    public SynchronizationDaemon(Context parent) {
        this.parent = parent;
        this.cloud = new Cloud(this.parent);
    }

    /**
     * Causes the thread on which it is called to sleep for atleast the specified number of milliseconds
     *
     * @param milliseconds The number of milliseconds for which the thread is supposed to sleep.
     */
    private void getSomeSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);// frequency is in seconds
        } catch (InterruptedException ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.getSomeSleep)" : ex.getMessage());
        }

    }

    /**
     * Fetches the number of seconds representing the interval at which to issue
     * synchronization requests
     *
     * @return Number of seconds representing synchronization interval
     */
    private int getFrequency() {
        JSONObject frequencies = Utils.getJSONFromFile(this.parent, this.parent.getFilesDir().getAbsolutePath() + "/frequency.json");
        try {
            return frequencies.getJSONObject("synchronization").getInt("interval");
        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.toggleData)" : ex.getMessage());
            return 3600; // default to one hour
        }
    }

    private boolean toggleData(boolean status) {
        try {
            final ConnectivityManager conman = (ConnectivityManager) this.parent.getSystemService(Context.CONNECTIVITY_SERVICE);
            Class conmanClass;
            conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, status);
            return true;
        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.toggleData)" : ex.getMessage());
            return false;
        }
    }

    public void synchronize(SynchronizationHandler handler) {
        String synchronizationUrl = handler.getSynchronizationURL();
        String response = Utils.doPostHTTP(synchronizationUrl, handler.getSynchronizationData().toString());
        try {
            JSONObject responseJSON;
            responseJSON = new JSONObject(response);
            handler.processJSONResponse(responseJSON);
        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.synchronize)" : ex.getMessage());

        }
    }

}
