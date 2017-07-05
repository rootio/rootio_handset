package org.rootio.tools.sms;

import java.util.Date;

import org.rootio.activities.DiagnosticStatistics;
import org.rootio.services.synchronization.SynchronizationType;
import org.rootio.services.synchronization.SynchronizationUtils;
import org.rootio.tools.diagnostics.DiagnosticAgent;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.telephony.SmsManager;

public class DiagnosticsSMSHandler implements MessageProcessor {

	
	private String[] messageParts;
	private Context parent;
	private String from;
	private DiagnosticAgent diagnosticsAgent;
	private DiagnosticStatistics diagnosticStatistics;
	private SynchronizationUtils synchronizationUtils;
	
	DiagnosticsSMSHandler(Context parent, String from, String[] messageParts)
	{
		this.parent = parent;
		this.from = from;
		this.messageParts = messageParts;
		this.synchronizationUtils = new SynchronizationUtils(this.parent);
	}
	
	private Date getDefaultBaseDate()
	{
		Date lastUpdateDate = this.synchronizationUtils.getLastUpdateDate(SynchronizationType.Diagnostic);
		return lastUpdateDate;
	}

	@Override
	public boolean ProcessMessage() {
		
		//not enough parameters
		if(this.messageParts.length < 3 && this.messageParts.length > 4)
		{
			return false;
		}
		
		if(messageParts[0].equals("diagnostic"))
		{
			Date baseDate = null;
			if(messageParts.length == 4) //base date specified
			{
				baseDate = Utils.getDateFromString(messageParts[2], "yyyy-MM-dd'T'HH:mm:ss"); 
				if(baseDate == null){
					baseDate = this.getDefaultBaseDate();
				}
				
				this.diagnosticStatistics = new DiagnosticStatistics(this.parent, baseDate);
				
				String historicDiagnosticInformation = this.getHistoricDiagnosticInformation();
				this.respondAsyncStatusRequest(this.from, historicDiagnosticInformation);
			}
			else if(messageParts.length == 3)//no base date specified	
			{
				baseDate = this.getDefaultBaseDate();
			}
			
			this.diagnosticsAgent = new DiagnosticAgent(this.parent);
			String diagnosticInformation = this.getDiagnosticInformation();
			this.respondAsyncStatusRequest(from, diagnosticInformation);
			
		}
		
		//Gibberish
		return false;
	}
	
	
	
	private String getHistoricDiagnosticInformation()
	{
		StringBuilder diagnosticInformation = new StringBuilder();
		diagnosticInformation.append(String.format("%s|", this.messageParts[0]));
		diagnosticInformation.append(String.format("%s|%s|%s|", Math.round(this.diagnosticStatistics.getMinBatteryLevel()), Math.round(this.diagnosticStatistics.getAverageBatteryLevel()), Math.round(this.diagnosticStatistics.getMaxBatteryLevel())));
		diagnosticInformation.append(String.format("%s|%s|%s|", Math.round(this.diagnosticStatistics.getMinGSMStrength()), Math.round(this.diagnosticStatistics.getAverageGSMStrength()), Math.round(this.diagnosticStatistics.getMaxGSMStrength())));
		diagnosticInformation.append(String.format("%.1f|", this.diagnosticStatistics.getAverageWiFIAvailability()));
		diagnosticInformation.append(String.format("%s|%s|%s|", Math.round(this.diagnosticStatistics.getMinStorageUtilization()), Math.round(this.diagnosticStatistics.getAverageStorageUtilization()), Math.round(this.diagnosticStatistics.getMaxStorageUtilization())));
		diagnosticInformation.append(String.format("%s|%s|%s|", Math.round(this.diagnosticStatistics.getMinCPUUtilization()), Math.round(this.diagnosticStatistics.getAverageCPUUtilization()), Math.round(this.diagnosticStatistics.getMaxCPUUtilization())));
		diagnosticInformation.append(String.format("%s|%s|%s|", Math.round(this.diagnosticStatistics.getMinMemoryUtilization()), Math.round(this.diagnosticStatistics.getAverageMemoryUtilization()), Math.round(this.diagnosticStatistics.getMaxMemoryUtilization())));
		diagnosticInformation.append(String.format("%.3f|%.3f|", this.diagnosticStatistics.getMinLatitude(), this.diagnosticStatistics.getMaxLatitude()));
		diagnosticInformation.append(String.format("%.3f|%.3f|", this.diagnosticStatistics.getMinLongitude(), this.diagnosticStatistics.getMaxLongitude()));
		diagnosticInformation.append(String.format("%s", this.messageParts[this.messageParts.length - 1]));
		return diagnosticInformation.toString();
	}
	
	private String getDiagnosticInformation()
	{
		this.diagnosticsAgent.runDiagnostics();
		StringBuilder diagnosticInformation = new StringBuilder();
		diagnosticInformation.append(String.format("%s|", this.messageParts[0]));
		diagnosticInformation.append(String.format("%s|", this.diagnosticsAgent.getBatteryLevel()));
		diagnosticInformation.append(String.format("%s|", this.diagnosticsAgent.getGSMConnectionStrength()));
		diagnosticInformation.append(String.format("%s|", this.diagnosticsAgent.getCPUUtilization()));
		diagnosticInformation.append(String.format("%s|", this.diagnosticsAgent.getStorageStatus()));
		diagnosticInformation.append(String.format("%s|", this.diagnosticsAgent.getCPUUtilization()));
		diagnosticInformation.append(String.format("%s|", this.diagnosticsAgent.getMemoryStatus()));
		diagnosticInformation.append(String.format("%s|", this.diagnosticsAgent.getLatitude()));
		diagnosticInformation.append(String.format("%s", this.diagnosticsAgent.getLongitude()));
		return diagnosticInformation.toString();
	}

	String padString(String input, String pad, int length)
    {
        StringBuffer buff = new StringBuffer(input);
        while(buff.length() < length)
        {
            buff.insert(0, pad);
        }
        return buff.toString();
    }
	
	@Override
	public void respondAsyncStatusRequest(String from, String data) {
		SmsManager smsManager = SmsManager.getDefault();
		Utils.toastOnScreen(data  + "to" +from);
		smsManager.sendTextMessage(from, null, data, null, null);
		
	}
}
