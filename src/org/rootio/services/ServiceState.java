package org.rootio.services;

import java.util.Date;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

public class ServiceState {

	private int serviceId;
	private String serviceName;
    private int serviceState;
    private Context context;
    private Date lastUpdatedDate;
	
	public ServiceState(Context context, int serviceId)
	{
		this.context = context;
		this.serviceId = serviceId;
		this.fetchServiceState();
	}
	
	String getServiceName()
	{
		return this.serviceName;
	}
	
    int getServiceState()
	{
		return this.serviceState;
	}
    
    public void setServiceState(int serviceState)
    {
    	this.serviceState = serviceState;
    	this.saveServiceState();
    }
    
    public Date getLastUpdatedDate()
    {
    	return this.lastUpdatedDate;
    }
    
    private void saveServiceState()
    {
    	String tableName = "servicestate";
    	ContentValues data = new ContentValues();
    	data.put("servicestate", serviceState);
    	data.put("lastupdateddate", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
    	String whereClause = "id = ?";
    	String[] whereArgs = new String[]{String.valueOf(serviceId)};
    	DBAgent agent = new DBAgent(this.context);
    	agent.updateRecords(tableName, data, whereClause, whereArgs);	
    }
    
    private void fetchServiceState()
    {
    	String tableName = "servicestate";
    	String[] columns = new String[]{"service", "servicestate","lastupdateddate"};
    	String whereClause = "id = ?";
    	String[] whereArgs = new String[]{String.valueOf(serviceId)};
    	DBAgent agent = new DBAgent(this.context);
    	String[][] result = agent.getData(true, tableName, columns, whereClause,whereArgs, null, null, null, null);
    	this.serviceName = result.length > 0? result[0][0]: new String();
    	this.serviceState = result.length > 0? Utils.parseIntFromString(result[0][1]):0;
    }
}
