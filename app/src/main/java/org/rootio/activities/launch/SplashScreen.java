/**
 *
 */
package org.rootio.activities.launch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.handset.BuildConfig;
import org.rootio.handset.R;
import org.rootio.services.synchronization.StationHandler;
import org.rootio.services.synchronization.SynchronizationHandler;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 */
public class SplashScreen extends Activity {

    private ProgressDialog progressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.checkCloudFileExists();
        setContentView(R.layout.splash_screen);
    }

    private void checkCloudFileExists() {
        if (!new File(this.getFilesDir().getAbsolutePath() + "/cloud.json").exists()) {
            for (String fileName : new String[]{"rootio.sqlite", "sync_ids.json"}) {
                this.copyDataFile(fileName);
            }
        }
    }


    public void onConnectClick(View view) {

        try {
            int stationId = Integer.parseInt(((EditText) this.findViewById(R.id.stationIdEt)).getText().toString());
            String stationKey = ((EditText) this.findViewById(R.id.stationKeyEt)).getText().toString();
            String serverAddress = ((EditText) this.findViewById(R.id.serverAddressEt)).getText().toString();
            int serverPort = Integer.parseInt(((EditText) this.findViewById(R.id.serverPortEt)).getText().toString());
           this.synchronize(new StationHandler(this, new Cloud(this, serverAddress, serverPort, stationId, stationKey)));
        }  catch (NumberFormatException e) {
            Utils.warnOnScreen(this, "Station ID and Port number should be Integers");
        } catch (Exception e) {
            e.printStackTrace();
            Utils.warnOnScreen(this, "Error encountered connecting to station. Please verify credentials and Internet connectivity");
        }
    }

    private void notifySynced() {
        //only save station deets if they are valid and allow a sync
        int stationId = Integer.parseInt(((EditText) this.findViewById(R.id.stationIdEt)).getText().toString());
        String stationKey = ((EditText) this.findViewById(R.id.stationKeyEt)).getText().toString();
        String serverAddress = ((EditText) this.findViewById(R.id.serverAddressEt)).getText().toString();
        int serverPort = Integer.parseInt(((EditText) this.findViewById(R.id.serverPortEt)).getText().toString());
        try {
            this.saveCloudInformation(stationId, stationKey, serverAddress, serverPort);
        }
        catch (Exception e) {
            Utils.warnOnScreen(this, "Station information was not saved due to an error, please try again");
            return;
        }

        //move on to the next screen
        Intent intent = new Intent(this, LauncherActivity.class);
        intent.putExtra("isFirstTimeLaunch", true);
        this.startActivity(intent);
        this.finish();
    }

    private void saveCloudInformation(int stationId, String stationKey, String serverAddress, int serverPort) throws Exception {
        try {
            ContentValues cloudInformation = new ContentValues();
            cloudInformation.put("station_id", stationId);
            cloudInformation.put("station_key", stationKey);
            cloudInformation.put("server_IP", serverAddress);
            cloudInformation.put("server_port", serverPort);
            Utils.savePreferences(cloudInformation, this);
        } catch (Exception e) {
            throw e;
        }
    }

    private void copyDataFile(String fileName) {
        InputStream instr = null;
        FileOutputStream foutstr = null;
        File destinationFile = null;
        try {
            instr = this.getAssets().open(fileName);

            byte[] buffer = new byte[1024000]; // 1 MB
            instr.read(buffer);
            destinationFile = new File(this.getFilesDir().getAbsolutePath() + "/" + fileName);
            if (destinationFile.exists()) {
                destinationFile.delete();
            }
            if (destinationFile.createNewFile()) {
                foutstr = new FileOutputStream(destinationFile);
                foutstr.write(buffer);
            } else {
                Utils.toastOnScreen(BuildConfig.DEBUG? "Failed to create file" + fileName : "Failed to create necessary file, app may not work well", this);
            }
        } catch (IOException ex) {
            Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SplashScreen.copyDataFile)" : ex.getMessage());

        } finally {
            try {
                instr.close();
            } catch (Exception ex) {
                Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SplashScreen.copyDataFile)" : ex.getMessage());
            }

            try {
                foutstr.close();
            } catch (Exception ex) {
                Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SplashScreen.copyDataFile)" : ex.getMessage());
            }
        }
    }

    public void synchronize(final SynchronizationHandler handler) {
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setTitle("Connecting to station");
        this.progressDialog.show();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                String synchronizationUrl = handler.getSynchronizationURL();
                String response = Utils.doPostHTTP(synchronizationUrl, handler.getSynchronizationData().toString());
                try {
                    SplashScreen.this.progressDialog.dismiss();
                    JSONObject responseJSON = new JSONObject(response);
                    handler.processJSONResponse(responseJSON);
                    Utils.toastOnScreen("Successfully connected to cloud server!", SplashScreen.this);
                    SplashScreen.this.notifySynced();
                } catch (Exception ex) {
                    Utils.toastOnScreen("Error! Please check your internet connection and try Again", SplashScreen.this);
                    Log.e(SplashScreen.this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.synchronize)" : ex.getMessage());
                }
            }
        });
        thread.start();
    }
}
