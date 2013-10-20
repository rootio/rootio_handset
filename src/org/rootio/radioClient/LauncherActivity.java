package org.rootio.radioClient;

import java.lang.reflect.Method;

import org.rootio.tools.diagnostics.DiagnosticsRunner;
import org.rootio.tools.radio.PhoneCallPickListener;
import org.rootio.tools.radio.RadioRunner;
import org.rootio.tools.telephony.CallRunner;
import org.rootio.tools.utils.Utils;

import com.android.internal.telephony.ITelephony;

import android.os.Bundle;
import android.os.Handler;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class LauncherActivity extends TabActivity {

	private Handler handler;
	private Thread radioThread;
	private Thread diagnosticThread;
	private Thread callThread;
	private boolean isRunning = false;
	private TelephonyManager telephonyManager;
	private BroadcastReceiver broadcastReceiver;
	private RadioRunner radioRunner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Resources ressources = getResources();
		TabHost tabHost = getTabHost();

		// Radio tab
		Intent intentRadio = new Intent().setClass(this, RadioActivity.class);
		TabSpec tabSpecRadio = tabHost.newTabSpec("Radio")
				.setIndicator("", ressources.getDrawable(R.drawable.radio))
				.setContent(intentRadio);

		// Phone tab
		Intent intentPhone = new Intent().setClass(this, PhoneActivity.class);
		TabSpec tabSpecCalls = tabHost.newTabSpec("Calls")
				.setIndicator("", ressources.getDrawable(R.drawable.phone))
				.setContent(intentPhone);

		// Diagnostics tab
		Intent intentDiagnostics = new Intent().setClass(this,
				DiagnosticActivity.class);
		TabSpec tabSpecDiagnostics = tabHost
				.newTabSpec("Diagnostics")
				.setIndicator("", ressources.getDrawable(R.drawable.diagnostic))
				.setContent(intentDiagnostics);

		tabHost.addTab(tabSpecRadio);
		tabHost.addTab(tabSpecCalls);
		tabHost.addTab(tabSpecDiagnostics);

		// set Radio tab as default (zero based)
		tabHost.setCurrentTab(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent = new Intent(this, StationActivity.class);
		startActivity(intent);
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!isRunning) {
			
			isRunning = true;
			try
			{
			handler = new Handler();
			Utils.setContext(this.getBaseContext());
			Utils.setHandler(this.handler);

			radioRunner = new RadioRunner(this);
			radioThread = new Thread(radioRunner);
			radioThread.start();

			this.broadcastReceiver = new PhoneCallPickListener(radioRunner);
			this.registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
			
			
			DiagnosticsRunner diagnosticsRunner = new DiagnosticsRunner(this);
			diagnosticThread = new Thread(diagnosticsRunner);
			diagnosticThread.start();
			
			/*
			CallRunner callRunner = new CallRunner(this);
			callThread = new Thread(callRunner);
			callThread.start();
			*/
			
			this.telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			PhoneCallListener listener = new PhoneCallListener();
			telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
			}
			catch(Exception ex)
			{
				Utils.toastOnScreen(ex.getMessage());
			}
		}

	}
	
	private void proceedAfterCall()
	{
		try
		{
		radioRunner.resumeProgram();
		}
		catch(Exception ex)
		{
			Utils.toastOnScreen(ex.getMessage());
		}
	}
	
	/**
	 * Answers an incoming call
	 */
	private void pickCall() {
		 
		radioRunner.pauseProgram();
	         Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);             
	         buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
	         try {
	             this.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
	         }
	         catch (Exception e) {
	             this.logCall("Catch block of ACTION_MEDIA_BUTTON broadcast !");
	         }

	         this.logCall("Answered incoming call from: " );  
	    }
	   

	/**
	 * Declines an incoming call or ends an ongoing call.
	 */
	private void declineCall() {
		ITelephony telephonyService;
		TelephonyManager telephony = (TelephonyManager) this
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
			this.logCall(incomingNumber);
		} else {
			declineCall();
			this.logCall(incomingNumber);
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
		return true;
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
		return true;
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
	private void logCall(String telephoneNumber) {
		Toast.makeText(this, telephoneNumber, Toast.LENGTH_LONG).show();

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
			case TelephonyManager.CALL_STATE_IDLE:
				proceedAfterCall();
			default:
				break;
			}
		}
	}

}
