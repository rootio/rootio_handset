package org.rootio.services.synchronization;
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
		while(((SynchronizationService)this.parent).isRunning())
		{
			Utils.toastOnScreen("synchronizing...");
			programSynchronizer.synchronize();
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
	//TO-DO: introduce ID of the particular program
	private String synchronizationURL;
	
	ProgramSynchronizer()
	{
		this.synchronizationURL = this.getSynchronizationURL();
	}
	
	/**
	 * Constructs the URL to check for Program updates
	 * @return
	 */
	private String getSynchronizationURL()
	{
		return String.format("http://%s:%s/%s/%s", SynchronizationDaemon.this.cloud.getIPAddress(),SynchronizationDaemon.this.cloud.getHTTPPort(), "api/program", SynchronizationDaemon.this.cloud.getServerKey());
	}
	
	/**
	 * Runs the synchronization for programs
	 */
	public void synchronize() {
		String response = Utils.doHTTP(this.synchronizationURL);
		ProgramHandler handler = new ProgramHandler(SynchronizationDaemon.this.parent, response);
		handler.processProgram();
	}
}



}
