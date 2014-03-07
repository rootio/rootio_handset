package org.rootio.tools.telephony;

import java.lang.reflect.Method;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;

public class CallManager implements Runnable {
	private Service parentService;
	private TelephonyManager telephonyManager;

	public CallManager(Service parentService)
	{
		this.parentService = parentService;
		this.telephonyManager = (TelephonyManager) this.parentService
				.getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	@Override
	public void run() {
		Looper.prepare();
		waitForCalls();
		
	}
	
	public void stop()
	{
		//this.isListening = false;
	}

	private void waitForCalls() {
		PhoneCallListener listener = new PhoneCallListener();
		telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	/**
	 * Answers an incoming call
	 */
	private void pickCall() {
		 Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);             
         buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
         try {
             this.parentService.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
         }
         catch (Exception e) {
             //Log.e(R.string.app_name,e.getMessage());
         }

         Intent headSetUnPluggedintent = new Intent(Intent.ACTION_HEADSET_PLUG);
         headSetUnPluggedintent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
         headSetUnPluggedintent.putExtra("state", 1); // 0 = unplugged  1 = Headset with microphone 2 = Headset without microphone
         headSetUnPluggedintent.putExtra("name", "Headset");
         // TODO: Should we require a permission?
         try {
        	 this.parentService.sendOrderedBroadcast(headSetUnPluggedintent, null);
            }
         catch (Exception e) {
              System.err.println(e.getMessage());
         }
        }

	/**
	 * Declines an incoming call or ends an ongoing call.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void declineCall() {
		ITelephony telephonyService;
		TelephonyManager telephony = (TelephonyManager) parentService
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
		try
		{
		if (isWhiteListed(incomingNumber)) {
			Utils.doNotification(this.parentService, parentService.getResources().getString(R.string.app_name), "Picking phone call from "+incomingNumber); //, LogType.Call);
			pickCall();
			this.logCall(incomingNumber, CallType.Incoming.ordinal(), CallStatus.Picked.ordinal());
		} else {
			Utils.doNotification(this.parentService, parentService.getResources().getString(R.string.app_name),"Declining phone call from "+incomingNumber);//, LogType.Call);
			declineCall();
			this.logCall(incomingNumber, CallType.Incoming.ordinal(),	CallStatus.Declined.ordinal());
		}
		}
		catch(Exception ex)
		{
			Utils.toastOnScreen(ex.getMessage());
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
		String[] columns = new String[] { "telephonenumber" };
		String filter = "telephonenumber = ?";
		String[] selectionArgs = new String[] { phoneNumber };
		String having = null;
		String orderBy = null;
		String limit = null;
		String groupBy = null;
		DBAgent agent = new DBAgent((Context)this.parentService);
		String[][] result = agent.getData(distinct, tableName, columns, filter,selectionArgs, groupBy, having, orderBy, limit);
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
		String[] columns = new String[] { "telephonenumber" };
		String filter = "where telephonenumber = ?";
		String[] selectionArgs = new String[] { phoneNumber };
		String having = null;
		String orderBy = null;
		String limit = null;
		String groupBy = null;
		DBAgent agent = new DBAgent((Context)this.parentService);
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
		DBAgent dbagent = new DBAgent((Context)this.parentService);
		dbagent.saveData(tableName, null, data);

	}

	/**
	 * Class to handle telephony events received by the phone
	 * 
	 * @author UTL051109
	 */
	public class PhoneCallListener extends PhoneStateListener {
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
