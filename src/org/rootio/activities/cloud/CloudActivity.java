package org.rootio.activities.cloud;

import org.rootio.radioClient.R;
import org.rootio.tools.cloud.Cloud;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CloudActivity extends Activity {

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		this.setContentView(R.layout.cloud_details);
		this.setTitle("Cloud Details");
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		Cloud cloud = new Cloud(this);
		renderCloudInformation(cloud);
	}

	/**
	 * Renders on the screen information about the specified Cloud
	 * 
	 * @param cloud
	 *            The cloud whose information is to be printed out on the
	 *            screen.
	 */
	private void renderCloudInformation(Cloud cloud) {
		((TextView) findViewById(R.id.cloud_ip_address_tv)).setText(cloud.getServerAddress());
		((TextView) findViewById(R.id.cloud_httpport_tv)).setText(String.valueOf(cloud.getHTTPPort()));
		((TextView) findViewById(R.id.cloud_server_key_tv)).setText(cloud.getServerKey());
		((TextView) findViewById(R.id.cloud_station_id_tv)).setText(String.valueOf(cloud.getStationId()));
	}

	

}
