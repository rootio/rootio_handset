package org.rootio.tools.telephony;

import java.util.Date;

public class Call {
	private String telephoneNumber;
	private CallType callType;
	private Date callTime;
	private CallStatus callStatus;
	private boolean isNewCall;

	public Call(String telephoneNumber, CallType callType, CallStatus callStatus, Date callTime, boolean isNewCall) {
		this.telephoneNumber = telephoneNumber;
		this.callType = callType;
		this.callStatus = callStatus;
		this.callTime = callTime;
		this.isNewCall = isNewCall;
		if (this.isNewCall) {
			this.saveCallRecord();
		}
	}

	public Call(String telephoneNumber, CallType callType, CallStatus callStatus, Date callTime) {
		this(telephoneNumber, callType, callStatus, callTime, false);
	}

	/**
	 * Gets the Telephone number that originated the call
	 * 
	 * @return Telephone number of calling party
	 */
	public String getTelephoneNumber() {
		return this.telephoneNumber;
	}

	/**
	 * Gets the date when the call was made
	 * 
	 * @return Date of call
	 */
	public Date getCallTime() {
		return this.callTime;
	}

	/**
	 * Gets the type of call that this was
	 * 
	 * @return The type of this call
	 */
	public CallType getCallType() {
		return this.callType;
	}

	/**
	 * The status of this call, whether it was declined or picked
	 * 
	 * @return CallStatus object identifying the status of this call
	 */
	public CallStatus getCallStatus() {
		return this.callStatus;
	}

	/**
	 * Saves this call if it has just been created
	 */
	private void saveCallRecord() {
		// implement database persistence on new call
	}

}
