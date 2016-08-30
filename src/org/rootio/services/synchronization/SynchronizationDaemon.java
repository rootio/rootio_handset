package org.rootio.services.synchronization;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.activities.synchronization.SynchronizationConfiguration;
import org.rootio.radioClient.R;
import org.rootio.services.SynchronizationService;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

public class SynchronizationDaemon implements Runnable {
	private final Context parent;
	private final int frequency;
	private final Cloud cloud;

	@Override
	public void run() {
		SynchronizationConfiguration syncConfig = new SynchronizationConfiguration(this.parent);
		//StationInformationSynchronizer stationInformationSynchronizer = new StationInformationSynchronizer();
		
		while (((SynchronizationService) this.parent).isRunning()) {
			if (syncConfig.getEnableDataToSync()) {
				// turn on mobileData
				this.toggleData(true);
				this.getSomeSleep(15000);
			}

			// do the sync
			// programSynchronizer.synchronize();
			// eventTimeSynchronizer.synchronize();
			// stationInformationSynchronizer.synchronize();
			this.synchronize(new DiagnosticsHandler(this.parent, this.cloud));
			this.synchronize(new CallLogHandler(this.parent, this.cloud));
			this.synchronize(new SMSLogHandler(this.parent, this.cloud));
			//this.synchronize(new ProgramsHandler(this.parent, this.cloud));
			this.synchronize(new WhitelistHandler(this.parent, this.cloud));
			this.synchronize(new FrequencyHandler(this.parent, this.cloud));

			if (syncConfig.getEnableDataToSync()) {
				// turn off mobile data
				this.toggleData(false);														// seconds
			}
			
			this.getSomeSleep(15000); //(this.frequency * 15000);// frequency is in
			
		}
	}

	public SynchronizationDaemon(Context parent) {
		this.parent = parent;
		this.frequency = 10000; // this.getFrequency();
		this.cloud = new Cloud(this.parent);
	}

	/**
	 * Causes the thread on which it is called to sleep for atleast the
	 * specified number of milliseconds
	 * 
	 * @param milliseconds
	 *            The number of milliseconds for which the thread is supposed to
	 *            sleep.
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
		String tableName = "frequencyconfiguration";
		String[] columns = new String[] { "quantity", "frequencyunitid" };
		String orderBy = "_id desc";
		String whereClause = "title = ?";
		String[] whereArgs = new String[] { "synchronization" };
		DBAgent dbAgent = new DBAgent(this.parent);
		String[][] results = dbAgent.getData(false, tableName, columns, whereClause, whereArgs, null, null, orderBy, null);
		if (results.length > 0) {
			int unit = Utils.parseIntFromString(results[0][1]);
			switch (unit) {
			case 1:
				return Utils.parseIntFromString(results[0][0]) * 3600;
			case 2:
				return Utils.parseIntFromString(results[0][0]) * 60;
			case 3:
				return Utils.parseIntFromString(results[0][0]);
			}
		}
		return 0;
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
		Utils.toastOnScreen(response, this.parent);
		try {
			JSONObject responseJSON;
			responseJSON = new JSONObject(response);			
			handler.processJSONResponse(responseJSON);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	

	class StationInformationSynchronizer {
		private final SynchronizationUtils synchronizationUtils;

		StationInformationSynchronizer() {
			this.synchronizationUtils = new SynchronizationUtils(SynchronizationDaemon.this.parent);
		}

		/**
		 * Constructs the URL to check for EventTime updates
		 * 
		 * @return
		 */
		private String getSynchronizationURL() {
			System.out.println(String.format("http://%s:%s/%s/%s/scheduled_programs?api_key=%s", SynchronizationDaemon.this.cloud.getServerAddress(), SynchronizationDaemon.this.cloud.getHTTPPort(), "api/station", SynchronizationDaemon.this.cloud.getStationId(), SynchronizationDaemon.this.cloud.getServerKey()));
			return String.format("http://%s:%s/%s/%s/scheduled_programs?api_key=%s", SynchronizationDaemon.this.cloud.getServerAddress(), SynchronizationDaemon.this.cloud.getHTTPPort(), "api/station", SynchronizationDaemon.this.cloud.getStationId(), SynchronizationDaemon.this.cloud.getServerKey());
		}

		/**
		 * Runs the synchronization for programs
		 */
		public void synchronize() {
			String synchronizationUrl = this.getSynchronizationURL();
			String response = Utils.doHTTP(synchronizationUrl);
			//EventTimeHandler handler = new EventTimeHandler(SynchronizationDaemon.this.parent, response, this.synchronizationUtils);
			//handler.processEventTimes();
		}
	}

}
