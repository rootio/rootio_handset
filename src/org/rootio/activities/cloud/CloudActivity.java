package org.rootio.activities.cloud;

import org.rootio.radioClient.R;
import org.rootio.tools.cloud.Cloud;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class CloudActivity extends Activity{

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		this.setContentView(R.layout.cloud_details);
		this.setTitle("Cloud Details");
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		Cloud cloud = new Cloud(this);
		renderCloudInformation(cloud);
	}

/**
 * Renders on the screen information about the specified Cloud
 * @param cloud The cloud whose information is to be printed out on the screen.
 */
	private void renderCloudInformation(Cloud cloud) {
		((TextView)findViewById(R.id.cloud_ip_address_tv)).setText(cloud.getServerAddress());
		((TextView)findViewById(R.id.cloud_httpport_tv)).setText(String.valueOf(cloud.getHTTPPort()));
		((TextView)findViewById(R.id.cloud_server_key_tv)).setText(cloud.getServerKey());
		((TextView)findViewById(R.id.cloud_station_id_tv)).setText(String.valueOf(cloud.getStationId()));
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.cloud_configuration, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuItem)
	{
		switch(menuItem.getItemId())
		{
		case R.id.edit_cloud_details:
			Intent intent = new Intent(this, CloudConfigurationActivity.class);
			this.startActivity(intent);
			break;
			default: //handles icon click
				this.finish();
				break;
		}
		return true;
	}	
	
}
