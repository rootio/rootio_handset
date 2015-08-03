package org.rootio.services.synchronization;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

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
		ProgramSynchronizer programSynchronizer = new ProgramSynchronizer();
		EventTimeSynchronizer eventTimeSynchronizer = new EventTimeSynchronizer();
		StationInformationSynchronizer stationInformationSynchronizer = new StationInformationSynchronizer();
		DiagnosticsSynchronizer diagnosticsSynchronizer = new DiagnosticsSynchronizer();
		while (((SynchronizationService) this.parent).isRunning()) {
			if(syncConfig.getEnableDataToSync())
			{
				//turn on mobileData
				this.toggleData(true);
				this.getSomeSleep(10000);
			}
			
			
			//do the sync
			programSynchronizer.synchronize();
			eventTimeSynchronizer.synchronize();
			stationInformationSynchronizer.synchronize();
			diagnosticsSynchronizer.synchronize();
			
			if(syncConfig.getEnableDataToSync())
			{
			    //turn off mobile data
			    this.toggleData(false);
			    this.getSomeSleep(this.frequency * 1000);// frequency is in seconds
			}
		}
	}

	public SynchronizationDaemon(Context parent) {
		this.parent = parent;
		this.frequency = this.getFrequency();
		this.cloud = new Cloud(this.parent);
	}
	
	/**
	 * Causes the thread on which it is called to sleep for atleast the specified number of milliseconds
	 * @param milliseconds The number of milliseconds for which the thread is supposed to sleep.
	 */
	private void getSomeSleep(long milliseconds)
	{
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

	/**
	 * This class handles synchronization particularly for programs
	 * 
	 * @author Jude Mukundane
	 * 
	 */
	class ProgramSynchronizer {
		private  ProgramsHandler programsHandler;
		private final SynchronizationUtils synchronizationUtils;

		ProgramSynchronizer() {
			this.synchronizationUtils = new SynchronizationUtils(SynchronizationDaemon.this.parent);	
		}

		/**
		 * Constructs the URL to check for Program updates
		 * 
		 * @return
		 */
		private String getSynchronizationURL(int programId) {
			return String.format("http://%s:%s/%s/%s?api_key=%s", SynchronizationDaemon.this.cloud.getServerAddress(), SynchronizationDaemon.this.cloud.getHTTPPort(), "api/program", programId, SynchronizationDaemon.this.cloud.getServerKey());
		}

		/**
		 * Runs the synchronization for programs
		 */
		public void synchronize() {
			this.programsHandler = new ProgramsHandler(SynchronizationDaemon.this.parent, cloud.getServerAddress(), cloud.getHTTPPort(), cloud.getStationId(), cloud.getServerKey(), this.synchronizationUtils.getLastUpdateDate(SynchronizationType.Program));
			for (Integer programId : this.programsHandler.getProgramIds()) {
				String synchronizationUrl = this.getSynchronizationURL(programId);
				String response = Utils.doHTTP(synchronizationUrl);
				ProgramHandler handler = new ProgramHandler(SynchronizationDaemon.this.parent, response, new SynchronizationUtils(SynchronizationDaemon.this.parent));
				handler.processProgram();
			}
		}
	}

	class EventTimeSynchronizer {

		EventTimeSynchronizer() {
			this.synchronizationUtils = new SynchronizationUtils(SynchronizationDaemon.this.parent);
		}

		private final SynchronizationUtils synchronizationUtils;

		/**
		 * Constructs the URL to check for EventTime updates
		 * 
		 * @return
		 */
		private String getSynchronizationURL() {
			String sincePart = this.getSincePart();
			System.out.println(String.format("http://%s:%s/%s/%s/schedule?api_key=%s&%s", SynchronizationDaemon.this.cloud.getServerAddress(), SynchronizationDaemon.this.cloud.getHTTPPort(), "api/station", SynchronizationDaemon.this.cloud.getStationId(), SynchronizationDaemon.this.cloud.getServerKey(), sincePart));
			return String.format("http://%s:%s/%s/%s/schedule?api_key=%s&%s", SynchronizationDaemon.this.cloud.getServerAddress(), SynchronizationDaemon.this.cloud.getHTTPPort(), "api/station", SynchronizationDaemon.this.cloud.getStationId(), SynchronizationDaemon.this.cloud.getServerKey(), sincePart);
		}

		/**
		 * Runs the synchronization for programs
		 */
		public void synchronize() {

			String synchronizationUrl = this.getSynchronizationURL();
			String response = Utils.doHTTP(synchronizationUrl);
			EventTimeHandler handler = new EventTimeHandler(SynchronizationDaemon.this.parent, response, this.synchronizationUtils);
			handler.processEventTimes();
		}

		private String getSincePart() {
			Date dt = this.synchronizationUtils.getLastUpdateDate(SynchronizationType.EventTime);
			return dt == null ? "all=1" : String.format("updated_since=%s", Utils.getDateString(dt, "yyyy-MM-dd'T'HH:mm:ss"));
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
			EventTimeHandler handler = new EventTimeHandler(SynchronizationDaemon.this.parent, response, this.synchronizationUtils);
			handler.processEventTimes();
		}
	}

	class DiagnosticsSynchronizer {
		private final SynchronizationUtils synchronizationUtils;

		DiagnosticsSynchronizer() {
			this.synchronizationUtils = new SynchronizationUtils(SynchronizationDaemon.this.parent);
		}

		/**
		 * Constructs the URL to check for EventTime updates
		 * 
		 * @return
		 */
		private String getSynchronizationURL() {
			return String.format("http://%s:%s/%s/%s/diagnostics?api_key=%s", SynchronizationDaemon.this.cloud.getServerAddress(), SynchronizationDaemon.this.cloud.getHTTPPort(), "api/station", SynchronizationDaemon.this.cloud.getStationId(), SynchronizationDaemon.this.cloud.getServerKey());
		}

		/**
		 * Runs the synchronization for programs
		 */
		public void synchronize() {
			DiagnosticsHandler handler = new DiagnosticsHandler(SynchronizationDaemon.this.parent, this.synchronizationUtils);
			for (int i = 0; i < handler.getSize(); i++) {
				String synchronizationUrl = this.getSynchronizationURL();
				String response = Utils.doPostHTTP(synchronizationUrl, handler.getSynchronizationData(i));
				Utils.toastOnScreen(response, SynchronizationDaemon.this.parent);
				handler.processDiagnosticSynchronizationResponse(response, i);
			}
		}
	}
}
