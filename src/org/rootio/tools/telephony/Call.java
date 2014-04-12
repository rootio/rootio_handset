package org.rootio.tools.telephony;

import java.util.Date;

public class Call {
private String telephoneNumber;
private CallType callType;
private Date callTime;
private CallStatus callStatus;
private boolean isNewCall;

public Call(String telephoneNumber, CallType callType, CallStatus callStatus, Date callTime, boolean isNewCall)
{
	this.telephoneNumber = telephoneNumber;
	this.callType = callType;
	this.callStatus = callStatus;
	this.callTime = callTime;
	this.isNewCall = isNewCall;
	if(this.isNewCall)
	{
		this.saveCallRecord();
	}
}

public Call(String telephoneNumber, CallType callType, CallStatus callStatus, Date callTime)
{
	this(telephoneNumber, callType,callStatus, callTime,false);
}

public String getTelephoneNumber()
{
	return this.telephoneNumber;
}

public Date getCallTime()
{
	return this.callTime;
}

public CallType getCallType()
{
	return this.callType;
}

public CallStatus getCallStatus()
{
	return this.callStatus;
}

private void saveCallRecord()
{
	//implement database persistence on new call
}

}
