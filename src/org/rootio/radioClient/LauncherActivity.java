package org.rootio.radioClient;

import org.rootio.tools.diagnostics.DiagnosticsRunner;
import org.rootio.tools.radio.RadioRunner;
import org.rootio.tools.telephony.CallRunner;
import org.rootio.tools.utils.Utils;
import android.os.Bundle;
import android.os.Handler;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class LauncherActivity extends TabActivity {

	private Handler handler;
	private Thread radioThread;
	private Thread diagnosticThread;
	private Thread callThread;
	private boolean isRunning;

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
			handler = new Handler();
			Utils.setContext(this.getBaseContext());
			Utils.setHandler(this.handler);

			RadioRunner radioRunner = new RadioRunner(this);
			radioThread = new Thread(radioRunner);
			radioThread.start();

			DiagnosticsRunner diagnosticsRunner = new DiagnosticsRunner(this);
			diagnosticThread = new Thread(diagnosticsRunner);
			diagnosticThread.start();
			
			CallRunner callRunner = new CallRunner(this);
			callThread = new Thread(callRunner);
			callThread.start();
		}

	}

}
