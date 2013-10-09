package org.rootio.radioClient;

import org.rootio.tools.radio.RadioRunner;
import org.rootio.tools.utils.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;

public class LauncherActivity extends Activity {

	Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        handler = new Handler();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.launcher, menu);
        return true;
    }
    
    @Override 
    public void onStart()
    {
    	super.onStart();
    	handler = new Handler();
    	Utils.setContext(this.getBaseContext());
    	Utils.setHandler(this.handler);
    	Utils.setActivity(this);
    	RadioRunner radioRunner = new RadioRunner(this);
    	Thread runnerThread = new Thread(radioRunner);
    	runnerThread.start();
    }
    
}
