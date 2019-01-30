package org.rootio.services.synchronization;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.services.SynchronizationService;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import java.util.HashMap;

public class SynchronizationDaemon implements Runnable {
    private final Context parent;
    private final Cloud cloud;
    private Handler handler;
    private MusicListHandler musicListHandler;

    @Override
    public void run() {

        this.musicListHandler = new MusicListHandler(this.parent, this.cloud);
        this.synchronize();

    }

    private void synchronize() {
        this.handler.post(new Runnable() {
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

                                synchronize(SynchronizationDaemon.this.musicListHandler);
                        SynchronizationDaemon.this.

                                synchronize(new PlaylistHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                        SynchronizationDaemon.this.synchronize(new LogHandler(SynchronizationDaemon.this.parent, SynchronizationDaemon.this.cloud));
                    }
                });
                thread.start(); //TODO: fix synchronicity. if the interval is too short, next jobs will start before this finishes!
                if (((SynchronizationService) SynchronizationDaemon.this.parent).isRunning()) { //the service might be stopped in between the scheduling and actual run of this job
                        SynchronizationDaemon.this.handler.postDelayed(this, SynchronizationDaemon.this.getFrequency() * 1000);
                }
            }
        }); //this is the first run. Maybe do not delay it..

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
            return 60; // default to 1 minute
        }
    }

    public void synchronize(final SynchronizationHandler handler) {
        try {
            String synchronizationUrl = handler.getSynchronizationURL();
            HashMap<String, Object> response = Utils.doDetailedPostHTTP(synchronizationUrl, handler.getSynchronizationData().toString());
            JSONObject responseJSON;
            responseJSON = new JSONObject((String)response.get("response"));
            handler.processJSONResponse(responseJSON);
            Utils.logEvent(this.parent, Utils.EventCategory.SYNC, Utils.EventAction.START, String.format("length: %s, response code: %s, duration: %s, url: %s", response.get("length"), response.get("responseCode"), response.get("duration"), response.get("url")));
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(SynchronizationDaemon.this.parent.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.synchronize)" : ex.getMessage());
        }
    }

}
