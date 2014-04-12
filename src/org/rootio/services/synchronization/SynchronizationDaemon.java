package org.rootio.services.synchronization;
import org.rootio.services.SynchronizationService;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.Context;

public class SynchronizationDaemon implements Runnable {
	private Context parent;
	private int frequency;
	private String serverKey;
	private String serverBaseURL;

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
		this.serverBaseURL = this.getServerBaseURL();
		this.serverKey = this.getStationKey();
	}
	
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
	
	private String getStationKey()
	{
		String tableName = "station";
		String[] columns = new String[]{"stationid", "serverkey"};
		DBAgent dbAgent = new DBAgent(this.parent);
		String[][] results = dbAgent.getData(true, tableName, columns, null, null, null, null, null, null);
		return results.length > 0? String.format("%s?api_key=%s", results[0][0], results[0][1]) : null;
	}
	
	private String getServerBaseURL()
	{
		String tableName = "cloud";
		String[] columns = new String[]{"ipaddress", "httpport"};
		DBAgent dbAgent = new DBAgent(this.parent);
		String[][] results = dbAgent.getData(true, tableName, columns, null, null, null, null, null, null);
		return results.length > 0? String.format("http://%s:%s", results[0][0], results[0][1]) : null;
	}
	
class ProgramSynchronizer
{
	private String synchronizationURL;
	
	ProgramSynchronizer()
	{
		this.synchronizationURL = this.getSynchronizationURL();
	}
	
	private String getSynchronizationURL()
	{
		return String.format("%s/%s/%s", SynchronizationDaemon.this.serverBaseURL, "api/program", SynchronizationDaemon.this.serverKey);
	}
	
	public void synchronize() {
		String response = Utils.doHTTP(this.synchronizationURL);
		ProgramHandler handler = new ProgramHandler(SynchronizationDaemon.this.parent, response);
		handler.processProgram();
	}
}



}
