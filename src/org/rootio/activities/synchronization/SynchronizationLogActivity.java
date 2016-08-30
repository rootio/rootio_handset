package org.rootio.activities.synchronization;

import org.rootio.radioClient.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class SynchronizationLogActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.synchronization_log);
		ListView listView = (ListView) this.findViewById(R.id.synchronization_log_lv);
		SynchronizationLogAdapter adapter = new SynchronizationLogAdapter(this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new SynchronizationLogLongClickListener(((SynchronizationLogAdapter) listView.getAdapter()).getData()));
		this.setTitle("Synchronization Log");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = this.getMenuInflater();
		menuInflater.inflate(R.menu.synchronization_configuration_frequency, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.synchronization_configuration_frequency_menu_item:
			Intent intent = new Intent(this, SynchronizationConfigurationFrequencyActivity.class);
			this.startActivity(intent);
			return true;
		default:
			return false;
		}

	}

}
