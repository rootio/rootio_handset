package org.rootio.services;

import java.lang.reflect.Method;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.telephony.CallRecorder;
import org.rootio.tools.utils.Utils;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;

public class TelephonyService extends Service implements ServiceInformationPublisher {

	private boolean isRunning;
	private final int serviceId = 1;
	private TelephonyManager telephonyManager;
	private PhoneCallListener listener;
	private boolean wasStoppedOnPurpose = true;
	private CallRecorder callRecorder;

	@Override
	public IBinder onBind(Intent arg0) {
		BindingAgent bindingAgent = new BindingAgent(this);
		return bindingAgent;
	}

	@Override
	public void onCreate() {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!isRunning) {
			Utils.doNotification(this, "RootIO", "Telephony Service started");
			this.waitForCalls();
			this.isRunning = true;
			this.sendEventBroadcast();
		}
		return Service.START_STICKY;

	}
	
	@Override
	public void onTaskRemoved(Intent intent)
	{
		super.onTaskRemoved(intent);
		if(intent != null)	
		{
			wasStoppedOnPurpose  = intent.getBooleanExtra("wasStoppedOnPurpose", false);
			if(wasStoppedOnPurpose)
			{
				this.shutDownService();
			}
			else
			{
				this.onDestroy();
			}
		}
	}

	@Override
	public void onDestroy() {
		if(this.wasStoppedOnPurpose == false)
		{
			Intent intent = new Intent("org.rootio.services.restartServices");
			sendBroadcast(intent);
		}
		else
		{
			this.shutDownService();
		}
		super.onDestroy();
	}

	private void shutDownService() {
		if (this.isRunning) {
			Utils.doNotification(this, "RootIO", "Telephony Service stopped");
			this.isRunning = false;
			this.telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
			this.sendEventBroadcast();
		}
	}

	/**
	 * Listens for Telephony activity coming into the phone
	 */
	private void waitForCalls() {
		this.telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		listener = new PhoneCallListener();
		telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	/**
	 * Answers an incoming call
	 */
	private void pickCall() {

		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		try {
			this.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
		} catch (Exception e) {
		}

		Intent headSetUnPluggedintent = new Intent(Intent.ACTION_HEADSET_PLUG);
		headSetUnPluggedintent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
		headSetUnPluggedintent.putExtra("state", 1); // 0 = unplugged 1 =
														// Headset with
														// microphone 2 =
														// Headset without
														// microphone
		headSetUnPluggedintent.putExtra("name", "Headset");

		// adjust the volume
		AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FLAG_SHOW_UI);

		try {
			this.sendOrderedBroadcast(headSetUnPluggedintent, null);
		} catch (Exception e) {
		}
	}

	/**
	 * Declines an incoming call or ends an ongoing call.
	 */
	private void declineCall() {
		ITelephony telephonyService;
		TelephonyManager telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
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
	
	private void setupCallRecording()
	{
		this.callRecorder = new CallRecorder(this);
		this.callRecorder.startRecording();
	}

	/**
	 * Processes a call noticed by the listener. Determines whether or not to
	 * pick the phone call basing on the calling phone number * @param
	 * incomingNumber
	 */
	public void handleCall(String incomingNumber) {
		if (isWhiteListed(incomingNumber)) {
			this.sendTelephonyEventBroadcast(true);
			pickCall();
			//this.setupCallRecording();
			this.logCall(incomingNumber, 1, 1);
		} else {
			declineCall();
			this.logCall(incomingNumber, 1, 2);
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
		DBAgent agent = new DBAgent(this);
		String[][] result = agent.getData(distinct, tableName, columns, filter, selectionArgs, groupBy, having, orderBy, limit);
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
		DBAgent agent = new DBAgent(this);
		String[][] result = agent.getData(distinct, tableName, columns, filter, selectionArgs, groupBy, having, orderBy, limit);
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
	private void logCall(String telephoneNumber, int calltypeid, int callstatusid) {
		String tableName = "calllog";
		ContentValues data = new ContentValues();
		data.put("telephonenumber", telephoneNumber);
		data.put("calltypeid", calltypeid);
		data.put("callstatusid", callstatusid);
		data.put("calltime", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
		DBAgent dbagent = new DBAgent(this);
		dbagent.saveData(tableName, null, data);

	}

	/**
	 * Class to handle telephony events received by the phone
	 * 
	 * @author Jude Mukundane
	 */
	class PhoneCallListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
					handleCall(incomingNumber);
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					TelephonyService.this.sendTelephonyEventBroadcast(false);
					if(TelephonyService.this.callRecorder != null)
					{
						TelephonyService.this.callRecorder.stopRecording();
						TelephonyService.this.callRecorder = null;					
					}
					break;
			}
		}
	}

	@Override
	public boolean isRunning() {
		return this.isRunning;
	}

	/**
	 * Sends out broadcasts informing listeners of change in service state
	 */
	private void sendEventBroadcast() {
		Intent intent = new Intent();
		intent.putExtra("serviceId", this.serviceId);
		intent.putExtra("isRunning", this.isRunning);
		intent.setAction("org.rootio.services.telephony.EVENT");
		this.sendBroadcast(intent);
	}

	/**
	 * Sends out broadcasts informing listeners of changes in status of the
	 * Telephone
	 * 
	 * @param isInCall
	 *            Boolean indicating whether the Telephone is in a call or not.
	 *            True: in call, False: Not in call
	 */
	private void sendTelephonyEventBroadcast(boolean isInCall) {
		Intent intent = new Intent();
		intent.putExtra("Incall", isInCall);
		intent.setAction("org.rootio.services.telephony.TELEPHONY_EVENT");
		this.sendBroadcast(intent);
	}

	@Override
	public int getServiceId() {
		// TODO Auto-generated method stub
		return 0;
	}
}
