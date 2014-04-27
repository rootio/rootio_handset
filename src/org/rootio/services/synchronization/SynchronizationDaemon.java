package org.rootio.services.synchronization;


import java.util.Date;

import org.rootio.services.SynchronizationService;
import org.rootio.tools.cloud.Cloud;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.Context;

public class SynchronizationDaemon implements Runnable {
	private Context parent;
	private int frequency;
	private Cloud cloud;

	@Override
	public void run() {
		ProgramSynchronizer programSynchronizer = new ProgramSynchronizer();
		EventTimeSynchronizer eventTimeSynchronizer = new EventTimeSynchronizer();
		while(((SynchronizationService)this.parent).isRunning())
		{
			Utils.toastOnScreen("synchronizing...");
			programSynchronizer.synchronize();
			eventTimeSynchronizer.synchronize();
			try {
				Thread.sleep(this.frequency * 1000);//frequency is in seconds
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}

	public SynchronizationDaemon(Context parent)
	{
		this.parent = parent;
		this.frequency = this.getFrequency();
		this.cloud = new Cloud(this.parent);
	}
	
	/**
	 * Fetches the number of seconds representing the interval at which to issue synchronization requests
	 * @return Number of seconds representing synchronization interval
	 */
	private int getFrequency()
	{
		String tableName = "frequencyconfiguration";
		String[] columns = new String[]{"quantity","frequencyunitid"};
		String orderBy = "_id desc";
		String whereClause = "title = ?";
		String[] whereArgs = new String[] {"synchronization"};
		DBAgent dbAgent = new DBAgent(this.parent);
		String[][] results = dbAgent.getData(false, tableName, columns, whereClause, whereArgs, null, null, orderBy, null);
		if(results.length > 0)
		{
			int unit = Utils.parseIntFromString(results[0][1]);
			switch(unit)
			{
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
	
	/**
	 * This class handles synchronization particularly for programs
	 * @author Jude Mukundane
	 *
	 */
class ProgramSynchronizer
{
	private ProgramsHandler programsHandler;
	private SynchronizationUtils synchronizationUtils;
	
	ProgramSynchronizer()
	{
		this.synchronizationUtils = new SynchronizationUtils(SynchronizationDaemon.this.parent);
		this.programsHandler = new ProgramsHandler(SynchronizationDaemon.this.parent, cloud.getServerAddress(), cloud.getHTTPPort(), cloud.getStationId(), cloud.getServerKey(), this.synchronizationUtils.getLastUpdateDate(SynchronizationType.Program));
	}
	
	/**
	 * Constructs the URL to check for Program updates
	 * @return
	 */
	private String getSynchronizationURL(int programId)
	{
		return String.format("http://%s:%s/%s/%s?api_key=%s", SynchronizationDaemon.this.cloud.getServerAddress(),SynchronizationDaemon.this.cloud.getHTTPPort(), "api/program", programId, SynchronizationDaemon.this.cloud.getServerKey());
	}
	
	/**
	 * Runs the synchronization for programs
	 */
	public void synchronize() {
		for(Integer programId : this.programsHandler.getProgramIds())
		{
			String synchronizationUrl = this.getSynchronizationURL(programId);
			String response = Utils.doHTTP(synchronizationUrl);
			ProgramHandler handler = new ProgramHandler(SynchronizationDaemon.this.parent, response, new SynchronizationUtils(SynchronizationDaemon.this.parent));
			handler.processProgram();	
		}	
	}
}

class EventTimeSynchronizer
{
	
	EventTimeSynchronizer()
	{
		this.synchronizationUtils = new SynchronizationUtils(SynchronizationDaemon.this.parent);
	}
	private SynchronizationUtils synchronizationUtils;
	/**
	 * Constructs the URL to check for EventTime updates
	 * @return
	 */
	private String getSynchronizationURL()
	{
		String sincePart = this.getSincePart();
		//http://demo.rootio.org/api/station/2/schedule?all=1
		return String.format("http://%s:%s/%s/%s/schedule?api_key=%s&%s", SynchronizationDaemon.this.cloud.getServerAddress(),SynchronizationDaemon.this.cloud.getHTTPPort(), "api/station", SynchronizationDaemon.this.cloud.getStationId(), SynchronizationDaemon.this.cloud.getServerKey(),sincePart);
	}
	
	/**
	 * Runs the synchronization for programs
	 */
	public void synchronize() {
		
			String synchronizationUrl = this.getSynchronizationURL();
			String response = Utils.doHTTP(synchronizationUrl);
			EventTimeHandler handler = new EventTimeHandler(SynchronizationDaemon.this.parent, response,this.synchronizationUtils);
			handler.processEventTimes();	
	}
	
	private String getSincePart()
	{
		Date dt = this.synchronizationUtils.getLastUpdateDate(SynchronizationType.EventTime);
		return dt == null? "all=1":String.format("updated_since=%s&all=1", Utils.getDateString(dt, "yyyy-MM-dd'T'HH:mm:ss" ));
	}
}


}
