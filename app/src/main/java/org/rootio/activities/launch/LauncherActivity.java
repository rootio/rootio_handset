package org.rootio.activities.launch;

import java.io.File;

import org.rootio.activities.DiagnosticActivity;
import org.rootio.activities.RadioActivity;
import org.rootio.activities.cloud.CloudActivity;
import org.rootio.activities.diagnostics.FrequencyActivity;
import org.rootio.activities.services.ServicesActivity;
import org.rootio.activities.stationDetails.StationActivity;
import org.rootio.activities.telephoneLog.TelephoneLogActivity;
import org.rootio.handset.R;
import org.rootio.services.DiagnosticsService;
import org.rootio.services.ProgramService;
import org.rootio.services.SynchronizationService;
import org.rootio.services.TelephonyService;
import org.rootio.tools.utils.Utils;

import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class LauncherActivity extends TabActivity {

	PendingIntent crashIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Resources ressources = getResources();
		TabHost tabHost = getTabHost();

		// Radio tab
		Intent intentRadio = new Intent().setClass(this, RadioActivity.class);
		TabSpec tabSpecRadio = tabHost.newTabSpec("Radio").setIndicator("", ressources.getDrawable(R.drawable.radio)).setContent(intentRadio);

		// Phone tab
		Intent intentPhone = new Intent().setClass(this, TelephoneLogActivity.class);
		TabSpec tabSpecCalls = tabHost.newTabSpec("Calls").setIndicator("", ressources.getDrawable(R.drawable.telephone)).setContent(intentPhone);

		// Diagnostics tab
		Intent intentDiagnostics = new Intent().setClass(this, DiagnosticActivity.class);
		TabSpec tabSpecDiagnostics = tabHost.newTabSpec("Diagnostics").setIndicator("", ressources.getDrawable(R.drawable.diagnostic)).setContent(intentDiagnostics);

		tabHost.addTab(tabSpecRadio);
		tabHost.addTab(tabSpecCalls);
		tabHost.addTab(tabSpecDiagnostics);

		// set Radio tab as default (zero based)
		tabHost.setCurrentTab(0);

		Utils.setContext(this.getBaseContext());
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(!new File(this.getFilesDir().getAbsolutePath() + "/station.json").exists())
		{
			Intent intent = new Intent(this, SplashScreen.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			this.finish();
		}
		else if(this.getIntent().getBooleanExtra("isFirstTimeLaunch", false))
		{
			for (Intent intent : new Intent[] {new Intent(this, TelephonyService.class), new Intent(this, DiagnosticsService.class), new Intent(this, ProgramService.class), new Intent(this, SynchronizationService.class)})
			{
				this.startService(intent);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
		case R.id.station_menu_item:
			intent = new Intent(this, StationActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.cloud_menu_item:
			intent = new Intent(this, CloudActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.telephony_menu_item:
			intent = new Intent(this, TelephoneLogActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.frequency_menu_item:
			intent = new Intent(this, FrequencyActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.quity_menu_item:
			this.onStop();
			this.finish();
			return true;
		case R.id.services_menu_item:
			intent = new Intent(this, ServicesActivity.class);
			this.startActivity(intent);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

}
