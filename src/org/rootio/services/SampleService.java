package org.rootio.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class SampleService extends Service {

	private Handler handler;
	private boolean isRunning;
	
	@Override
	public IBinder onBind(Intent arg0) {
		BindingAgent bindingAgent = new BindingAgent(this);
		return bindingAgent;
	}
	
	@Override
	public void onCreate()
	{
		handler = new Handler();
	}
	
	public boolean isRunning()
	{
		return this.isRunning;
	}
	@Override
	public void onDestroy()
	{
		this.makeToast("received stop command");
		this.isRunning = false;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		this.isRunning = true;
	Thread thread  = new Thread(new Runnable()
		{

			@Override
			public void run() {
				while(isRunning)
				{
					handler.post(new Runnable(){

						@Override
						public void run() {
							makeToast("sleepy hollow");
							
						}
						
					});
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
				
				
			}
			
		});
		thread.start();
		return Service.START_STICKY;
	}
	
	
	private void makeToast(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	

}
