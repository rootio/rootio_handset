package org.rootio.activities.launch;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import org.rootio.activities.DiagnosticActivity;
import org.rootio.activities.RadioActivity;
import org.rootio.activities.cloud.CloudActivity;
import org.rootio.activities.diagnostics.FrequencyActivity;
import org.rootio.activities.services.ServicesActivity;
import org.rootio.activities.stationDetails.StationActivity;
import org.rootio.activities.telephoneLog.TelephoneLogActivity;
import org.rootio.handset.R;
import org.rootio.services.DiagnosticsService;
import org.rootio.services.LinSipService;
import org.rootio.services.ProgramService;
import org.rootio.services.SMSService;
import org.rootio.services.SynchronizationService;
import org.rootio.services.TelephonyService;
import org.rootio.tools.utils.Utils;

@SuppressWarnings("deprecation")
public class LauncherActivity extends TabActivity  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Resources resources = getResources();
        TabHost tabHost = getTabHost();

        // Radio tab
        Intent intentRadio = new Intent().setClass(this, RadioActivity.class);
        TabSpec tabSpecRadio = tabHost.newTabSpec("Radio").setIndicator("", resources.getDrawable(R.drawable.radio)).setContent(intentRadio);

        // Phone tab
        Intent intentPhone = new Intent().setClass(this, TelephoneLogActivity.class);
        TabSpec tabSpecCalls = tabHost.newTabSpec("Calls").setIndicator("", resources.getDrawable(R.drawable.telephone)).setContent(intentPhone);

        // Diagnostics tab
        Intent intentDiagnostics = new Intent().setClass(this, DiagnosticActivity.class);
        TabSpec tabSpecDiagnostics = tabHost.newTabSpec("Diagnostics").setIndicator("", resources.getDrawable(R.drawable.diagnostic)).setContent(intentDiagnostics);

        tabHost.addTab(tabSpecRadio);
        tabHost.addTab(tabSpecCalls);
        tabHost.addTab(tabSpecDiagnostics);

        // set Radio tab as default (zero based)
        tabHost.setCurrentTab(0);

        Utils.setContext(this.getBaseContext());

        //in the event that this is relaunched on app crash
        if(Utils.isConnectedToStation(this))
        {
            this.startServices();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!Utils.isConnectedToStation(this)) {
            Intent intent = new Intent(this, SplashScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            this.finish();
        } else if (this.getIntent().getBooleanExtra("isFirstTimeLaunch", false)) {
            for (Intent intent : new Intent[]{new Intent(this, TelephonyService.class), new Intent(this, DiagnosticsService.class), new Intent(this, ProgramService.class), new Intent(this, SynchronizationService.class)}) {
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
            case R.id.quit_menu_item:
                this.onStop();
                this.finish();
                return true;
            case R.id.services_menu_item:
                intent = new Intent(this, ServicesActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.station_change_menu_item:
                AlertDialog.Builder alert = new AlertDialog.Builder(LauncherActivity.this);
                alert.setView(R.layout.activity_main);
                alert.setTitle(R.string.Warning);
                alert.setMessage(Html.fromHtml(getString(R.string.AlertMessage) + "\n <i>" + getText(R.string.RestartWarning) + "</i>"));
                alert.setPositiveButton(R.string.OkButtonText,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int id){
                        SharedPreferences prefs = getSharedPreferences("org.rootio.handset", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.clear();
                        editor.apply();
                        finish();
                        }
                });
                alert.setNegativeButton(R.string.CancelButtonText,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        }
                });
                alert.create();
                alert.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void startServices() {
        for (int serviceId : new int[]{1, 2, 3, 4,5,6}) // only vitals
        {
            //ServiceState serviceState = new ServiceState(context, serviceId);
            // if(serviceState.getServiceState() > 0)//service was started
            // {
            Intent intent = this.getIntentToLaunch(this, serviceId);
            this.startService(intent);
            // }
        }

    }

    /**
     * Gets the intent to be used to launch the service with the specified
     * serviceId
     *
     * @param context   The context to be used in creating the intent
     * @param serviceId The ID of the service for which to create the intent
     * @return
     */
    private Intent getIntentToLaunch(Context context, int serviceId) {
        Intent intent = null;
        switch (serviceId) {
            case 1: // telephony service
                intent = new Intent(context, TelephonyService.class);
                break;
            case 2: // SMS service
                intent = new Intent(context, SMSService.class);
                break;
            case 3: // Diagnostic Service
                intent = new Intent(context, DiagnosticsService.class);
                break;
            case 4: // Program Service
                intent = new Intent(context, ProgramService.class);
                break;
            case 5: // Sync Service
                intent = new Intent(context, SynchronizationService.class);
                break;
            case 6: // SIP Service.
                //intent = new Intent(context, SipService.class); Use this to use the Android SIP stack
                intent = new Intent (context, LinSipService.class); //Use this to use the Liblinphone SIP stack. Much more versatile and reliable
                break;
        }
        return intent;
    }

}
