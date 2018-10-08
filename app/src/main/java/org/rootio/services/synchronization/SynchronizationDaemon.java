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
import android.os.Handler;
import android.util.Log;

public class SynchronizationDaemon implements Runnable {
    private final Context parent;
    private final Cloud cloud;
    private Handler handler;

    @Override
    public void run() {

       /* while (((SynchronizationService) this.parent).isRunning()) {
            // turn on mobile data and wait
            //this.toggleData(true);
            //this.getSomeSleep(15000);

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

            this.getSomeSleep(this.getFrequency() * 1000); // seconds to milliseconds*/
        this.synchronize();

    }

    private void synchronize() {
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        SynchronizationDaemon.this.

                                synchronize(new DiagnosticsHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                        SynchronizationDaemon.this.

                                synchronize(new ProgramsHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                        SynchronizationDaemon.this.

                                synchronize(new CallLogHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                        SynchronizationDaemon.this.

                                synchronize(new SMSLogHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                        SynchronizationDaemon.this.

                                synchronize(new WhitelistHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                        SynchronizationDaemon.this.

                                synchronize(new FrequencyHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                        SynchronizationDaemon.this.

                                synchronize(new StationHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                        SynchronizationDaemon.this.

                                synchronize(new MusicListHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                        SynchronizationDaemon.this.

                                synchronize(new PlaylistHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                    }
                });
                thread.start();
                try {
                    //if this sync is taking long, wait for at most 3 sync cycles to finish before scheduling next iteration
                    thread.join(SynchronizationDaemon.this.getFrequency() * 1000 * 3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (((SynchronizationService) SynchronizationDaemon.this.parent).isRunning()) { //the service might be stopped in between the scheduling and actual run of this job
                    SynchronizationDaemon.this.handler.postDelayed(this, SynchronizationDaemon.this.getFrequency() * 1000);
                }
            }
        }, this.getFrequency() * 1000); //this is the first run. Maybe do not delay it..

    }


    public SynchronizationDaemon(Context parent) {
        this.parent = parent;
        this.cloud = new Cloud(this.parent);
        this.handler = new Handler();
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
        try {
            JSONObject frequencies = new JSONObject((String) Utils.getPreference("frequencies", String.class, this.parent));
            return frequencies.getJSONObject("synchronization").getInt("interval");
        } catch (Exception ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.toggleData)" : ex.getMessage());
            return 180; // default to 3 mins
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

    public void synchronize(final SynchronizationHandler handler) {
        try {
            String synchronizationUrl = handler.getSynchronizationURL();
            String response = Utils.doPostHTTP(synchronizationUrl, handler.getSynchronizationData().toString());
            JSONObject responseJSON;
            responseJSON = new JSONObject(response);
            handler.processJSONResponse(responseJSON);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(SynchronizationDaemon.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.synchronize)" : ex.getMessage());
        }
    }

}
