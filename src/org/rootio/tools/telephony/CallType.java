package org.rootio.tools.telephony;

public enum CallType {
	Incoming, Outgoing;

	public static CallType getCallType(String input) {
		if (input.equals("1")) {
			return CallType.Incoming;
		} else if (input.equals("2")) {
			return CallType.Outgoing;
		} else {
			return null;
		}
	}
}
