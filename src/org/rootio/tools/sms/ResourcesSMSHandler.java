package org.rootio.tools.sms;

import java.io.RandomAccessFile;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.SmsManager;

public class ResourcesSMSHandler implements MessageProcessor {

	private String[] messageParts;
	private String from;
	private Context parent;
	
	public ResourcesSMSHandler(Context parent, String from, String[] messageParts) {
		this.parent = parent;
		this.from = from;
		this.messageParts = messageParts;
			}

	@Override
	public boolean ProcessMessage() {
		
		return false;
		
	}
	
	private boolean getBatteryLevel()
	{
		float batteryLevel;
		try
		{
			 Intent batteryIntent = this.parent.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			    batteryLevel= ((float)level / (float)scale) * 100.0f; 
			    String response = String.format("Battery Level: %f", batteryLevel);
			    this.respondAsyncStatusRequest(response, from);
				return true;
	     }
		catch(Exception ex)
		{
			return false;
		}
	}
	
	private boolean getStorageLevel()
	{
		try
		{
		StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
		double freeBytes = statFs.getAvailableBlocks() * statFs.getBlockSize();
		double totalBytes = statFs.getBlockCount() * statFs.getBlockSize();
		String response = String.format("Available Bytes: %d, Free Bytes: %d", freeBytes, totalBytes);
		this.respondAsyncStatusRequest(response, from);
		return true;
		}
		catch(Exception ex)
		{
			return false;
		}
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private boolean getMemoryUsage()
	{
		try
		{
		MemoryInfo outInfo = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager)this.parent.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(outInfo);
		double availableMemory = outInfo.availMem;
		double totalMemory = outInfo.totalMem;
		String response = String.format("Available Memory: %d, Total Memory: %d", availableMemory, totalMemory);
		this.respondAsyncStatusRequest(response, from);
		return true;
		}
		catch(Exception ex)
		{
			return false;
		}
	}
	
	private boolean getCPUusage()
	{
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
			String load = reader.readLine();

			String[] toks = load.split(" ");

			long idle1 = Long.parseLong(toks[5]);
			long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
					+ Long.parseLong(toks[4]) + Long.parseLong(toks[6])
					+ Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

			try {
				Thread.sleep(360);
			} catch (Exception e) {
			}

			reader.seek(0);
			load = reader.readLine();
			reader.close();

			toks = load.split(" ");

			long idle2 = Long.parseLong(toks[5]);
			long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
			float CPUUtilization = (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
			String response = String.format("CPU Usage: %f", CPUUtilization);
			this.respondAsyncStatusRequest(response, from);
			return false;

		} catch (Exception ex) {
			return false;
		}

}

	@Override
	public void respondAsyncStatusRequest(String from, String data) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(from, null, data, null, null);	
	}
	
	

}
