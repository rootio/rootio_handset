package org.rootio.activities.telephoneLog.lists;

import org.rootio.radioClient.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class WhitelistActivity extends Activity {

	private ListView listView;
	private WhitelistAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstance) {

		super.onCreate(savedInstance);
		this.setContentView(R.layout.whitelist);
		listView = (ListView) this.findViewById(R.id.whitelist_lv);
		listView.setItemsCanFocus(false);
		adapter = new WhitelistAdapter(this);
		listView.setAdapter(adapter);
		this.setTitle("Whitelisted Numbers");
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		this.refreshWhitelist();
	}

	private void refreshWhitelist() {
		adapter.refresh();
		adapter.notifyDataSetChanged();
	}
}
