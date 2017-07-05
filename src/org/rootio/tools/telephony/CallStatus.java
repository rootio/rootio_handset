package org.rootio.tools.telephony;

public enum CallStatus {
	Picked, Declined;
	
	public static CallStatus getCallStatus(String input) {
		if (input.equals("1")) {
			return CallStatus.Picked;
		} else if (input.equals("0")) {
			return CallStatus.Declined;
		} else {
			return CallStatus.Declined;
		}
	}
}
