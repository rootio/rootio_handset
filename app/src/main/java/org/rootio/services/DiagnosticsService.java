package org.rootio.services;

import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.diagnostics.DiagnosticAgent;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class DiagnosticsService extends Service implements ServiceInformationPublisher {

    private boolean isRunning;
    private int serviceId = 3;
    private Thread runnerThread;

    @Override
    public IBinder onBind(Intent arg0) {
        return new BindingAgent(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.logEvent(this, Utils.EventCategory.SERVICES, Utils.EventAction.START, "Diagnostics Service");
        if (!this.isRunning) {
            Utils.doNotification(this, "RootIO", "Diagnostics service started");
            long delay = this.getDelay();
            delay = delay > 0 ? this.getMillisToSleep("seconds", delay) : 10000; // 10000 default
            DiagnosticsRunner diagnosticsRunner = new DiagnosticsRunner(this, delay);
            runnerThread = new Thread(diagnosticsRunner);
            runnerThread.start();
            this.isRunning = true;
        }
        this.startForeground(this.serviceId, Utils.getNotification(this, "RootIO", "Diagnostics service is running", R.drawable.icon, false, null, null));

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Utils.logEvent(this, Utils.EventCategory.SERVICES, Utils.EventAction.STOP, "Diagnostics Service");
        this.stopForeground(true);
        this.shutDownService();
        super.onDestroy();
    }

    private void shutDownService() {
                 this.isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    private long getDelay() {
        try {
            JSONObject frequencies = new JSONObject((String)Utils.getPreference("frequencies",String.class, this));
            return frequencies.getJSONObject("diagnostics").getInt("interval");
        } catch (Exception ex) {
            return 180; // default to 3 mins
        }
    }

    /**
     * Get the time in milliseconds for which to sleep given the unit and
     * quantity
     *
     * @param units   The measure of the interval supplied by the cloud. This will always be seconds hence this is redundant
     * @param quantity The quantity of units to be used in measuring time
     * @return The amount of time in milliseconds
     */
    private long getMillisToSleep(String units, long quantity) {
        switch (units) {
            case "hours":
                return quantity * 3600 * 1000;
            case "minutes":
                return quantity * 60 * 1000;
            case "seconds":
                return quantity * 1000;
            default:
                return this.getMillisToSleep("minutes", quantity);
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
        intent.setAction("org.rootio.services.diagnostics.EVENT");
        this.sendBroadcast(intent);
    }

    class DiagnosticsRunner implements Runnable {
        private DiagnosticAgent diagnosticAgent;
        private Context parentActivity;
        private long delay;

        DiagnosticsRunner(Context parentActivity, long delay) {
            this.parentActivity = parentActivity;
            this.diagnosticAgent = new DiagnosticAgent(this.parentActivity);
            this.delay = delay;
        }

        @Override
        public void run() {
            while (isRunning) {
                diagnosticAgent.runDiagnostics();
                this.logToDB();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    Log.e(TAG, "run: " +ex.getMessage(), ex);
                }
            }
        }

        /**
         * Saves the diagnostics gathered to the database
         */
        private void logToDB() {
            String tableName = "diagnostic";
            ContentValues values = new ContentValues();
            values.put("batterylevel", diagnosticAgent.getBatteryLevel());
            values.put("memoryutilization", diagnosticAgent.getMemoryStatus());
            values.put("storageutilization", diagnosticAgent.getStorageStatus());
            values.put("CPUutilization", diagnosticAgent.getCPUUtilization());
            values.put("wificonnected", diagnosticAgent.isConnectedToWifi());
            values.put("firstmobilenetworkname", diagnosticAgent.getTelecomOperatorName());
            values.put("firstmobilenetworkconnected", diagnosticAgent.isConnectedToMobileNetwork());
            values.put("firstmobilenetworkstrength", diagnosticAgent.getMobileSignalStrength());
            values.put("firstmobilenetworktype", diagnosticAgent.getMobileNetworkType());
            values.put("latitude", diagnosticAgent.getLatitude());
            values.put("longitude", diagnosticAgent.getLongitude());
            DBAgent dbAgent = new DBAgent(this.parentActivity);
            dbAgent.saveData(tableName, null, values);
        }
    }

}
