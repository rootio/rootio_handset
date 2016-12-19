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
import org.rootio.radioClient.R;
import org.rootio.services.synchronization.StationHandler;
import org.rootio.services.synchronization.SynchronizationHandler;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * @author Jude Mukundane, M-ITI/IST-UL
 * 
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
			this.createCloudFile();
		}

	}

	private void createCloudFile() {
		InputStream instr = null;
		FileOutputStream foutstr = null;
		File destinationFile = null;
		try {
			instr = this.getAssets().open("cloud.json");

			byte[] buffer = new byte[1024000]; // 1 MB
			instr.read(buffer);
			destinationFile = new File(this.getFilesDir().getAbsolutePath() + "/cloud.json");
			if (destinationFile.createNewFile()) {
				foutstr = new FileOutputStream(destinationFile);
				foutstr.write(buffer);
			} else {
				Utils.toastOnScreen("We cant create file", this);
			}
		} catch (IOException ex) {
			Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.createDatabaseFile)" : ex.getMessage());

		} finally {
			try {
				instr.close();
			} catch (Exception ex) {
				Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.createDatabaseFile)" : ex.getMessage());
			}

			try {
				foutstr.close();
			} catch (Exception ex) {
				Log.e(this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(DBAgent.createDatabaseFile)" : ex.getMessage());
			}
		}

	}

	public void onConnectClick(View view) {
		String stationId = ((EditText) this.findViewById(R.id.stationIdEt)).getText().toString();
		String stationKey = ((EditText) this.findViewById(R.id.stationKeyEt)).getText().toString();
		try {
			this.saveCloudInformation(stationId, stationKey);
			this.synchronize(new StationHandler(this, new Cloud(this)));
			Intent intent = new Intent(this, LauncherActivity.class);
			this.startActivity(intent);
			this.finish();
		} catch (JSONException e) {
			Utils.warnOnScreen(this, "Station information not saved, please try again");
		} catch (Exception e) {
			e.printStackTrace();
			Utils.warnOnScreen(this, "Error encountered connecting to station. Please verify credentials and Internet connectivity");
		}
	}

	private void saveCloudInformation(String stationId, String stationKey) throws Exception {
		JSONObject cloudInformation = Utils.getJSONFromFile(this, this.getFilesDir().getAbsolutePath() + "/cloud.json");
		try {
			cloudInformation.put("station_id", stationId);
			cloudInformation.put("station_key", stationKey);
			Utils.saveJSONToFile(this, cloudInformation, this.getFilesDir().getAbsolutePath() + "/cloud.json");
		} catch (JSONException e) {
			throw e;
		} catch (Exception e) {
			throw e;
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
				Utils.toastOnScreen(response, SplashScreen.this);
				try {
					JSONObject responseJSON = new JSONObject(response);
					handler.processJSONResponse(responseJSON);
				} catch (Exception ex) {
					Log.e(SplashScreen.this.getString(R.string.app_name), ex.getMessage() == null ? "Null pointer exception(SynchronizationDaemon.synchronize)" : ex.getMessage());
				}
			}
		});
		thread.start();
	}
}
