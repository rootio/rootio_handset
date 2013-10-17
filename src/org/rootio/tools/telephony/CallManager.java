package org.rootio.tools.telephony;

import java.lang.reflect.Method;

import org.rootio.tools.persistence.DBAgent;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.ITelephony;

public class CallManager implements Runnable {
	private Activity parentActivity;
	private TelephonyManager telephonyManager;

	@Override
	public void run() {
		waitForCalls();
	}

	private void waitForCalls() {
		PhoneCallListener listener = new PhoneCallListener();
		telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	/**
	 * Answers an incoming call
	 */
	private void pickCall() {
		ITelephony telephonyService;
		TelephonyManager telephony = (TelephonyManager) parentActivity
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Class c = Class.forName(telephony.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			telephonyService = (ITelephony) m.invoke(telephony);
			telephonyService.answerRingingCall();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Declines an incoming call or ends an ongoing call.
	 */
	private void declineCall() {
		ITelephony telephonyService;
		TelephonyManager telephony = (TelephonyManager) parentActivity
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Class c = Class.forName(telephony.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			telephonyService = (ITelephony) m.invoke(telephony);
			telephonyService.endCall();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Processes a call noticed by the listener. Determines whether or not to
	 * pick the phone call basing on the calling phone number * @param
	 * incomingNumber
	 */
	public void handleCall(String incomingNumber) {
		if (isWhiteListed(incomingNumber)) {
			pickCall();
			this.logCall(incomingNumber, CallType.Incoming.ordinal(),
					CallStatus.Picked.ordinal());
		} else {
			declineCall();
			this.logCall(incomingNumber, CallType.Incoming.ordinal(),
					CallStatus.Declined.ordinal());
		}
	}

	/**
	 * Given a number, determines if the number is permitted to call the
	 * micro-station
	 * 
	 * @param phoneNumber
	 *            the phone number to be checked for existence in the white list
	 * @return true if the number is in the station white list, false if number
	 *         is not in station white list
	 */
	private boolean isWhiteListed(String phoneNumber) {
		boolean distinct = true;
		String tableName = "whitelist";
		String[] columns = new String[] { "msisdn" };
		String filter = "where msisdn = ?";
		String[] selectionArgs = new String[] { phoneNumber };
		String having = null;
		String orderBy = null;
		String limit = null;
		String groupBy = null;
		DBAgent agent = new DBAgent();
		String[][] result = agent.getData(distinct, tableName, columns, filter,
				selectionArgs, groupBy, having, orderBy, limit);
		return result.length > 0;
	}

	/**
	 * Given a number, determines if the number is not permitted to call the
	 * micro-station. Not yet implemented.
	 * 
	 * @param phoneNumber
	 *            the phone number to be checked for existence in the white list
	 * @return true if number is in the station black list, false if the number
	 *         is not in the station black list
	 */
	private boolean isBlackListed(String phoneNumber) {
		boolean distinct = true;
		String tableName = "blacklist";
		String[] columns = new String[] { "msisdn" };
		String filter = "where msisdn = ?";
		String[] selectionArgs = new String[] { phoneNumber };
		String having = null;
		String orderBy = null;
		String limit = null;
		String groupBy = null;
		DBAgent agent = new DBAgent();
		String[][] result = agent.getData(distinct, tableName, columns, filter,
				selectionArgs, groupBy, having, orderBy, limit);
		return result.length > 0;
	}

	/**
	 * Log the call event
	 * 
	 * @param telephoneNumber
	 *            The telephone number that made or received the call
	 * @param calltypeid
	 *            The type of call (incoming or outgoing)
	 * @param callstatusid
	 *            The status of the call whether picked or declined.
	 */
	private void logCall(String telephoneNumber, int calltypeid,
			int callstatusid) {
		String tableName = "calllog";
		ContentValues data = new ContentValues();
		data.put("telephonenumber", telephoneNumber);
		data.put("calltypeid", calltypeid);
		data.put("callstatusid", callstatusid);
		DBAgent dbagent = new DBAgent();
		dbagent.saveData(tableName, null, data);

	}

	/**
	 * Class to handle telephony events received by the phone
	 * 
	 * @author UTL051109
	 */
	class PhoneCallListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				handleCall(incomingNumber);
				break;
			default:
				break;
			}
		}
	}
}
