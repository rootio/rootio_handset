package org.rootio.activities.telephoneLog;

import org.rootio.activities.telephoneLog.lists.BlackListActivity;
import org.rootio.activities.telephoneLog.lists.WhitelistActivity;
import org.rootio.radioClient.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class TelephoneLogActivity extends Activity implements OnRefreshListener {

	private SwipeRefreshLayout swipeContainer;
	private ListView telephoneLogView;
	private TelephoneLogAdapter telephoneLogAdapter;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		this.setContentView(R.layout.telephone_log);
		this.swipeContainer = (SwipeRefreshLayout)this.findViewById(R.id.swipe_container);
		telephoneLogView = (ListView)this.findViewById(R.id.call_log_lv);
		telephoneLogAdapter = new TelephoneLogAdapter(this);
		telephoneLogView.setAdapter(telephoneLogAdapter);
		this.setTitle("Call Records");
		if(!this.getIntent().getBooleanExtra("isHomeScreen", true))
		{
			this.getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		this.swipeContainer.setOnRefreshListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = this.getMenuInflater();
		menuInflater.inflate(R.menu.activity_telephony, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuItem)
	{
		switch(menuItem.getItemId())
		{
		
		case R.id.black_list_menu_item:
			Intent intent = new Intent(this, BlackListActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.white_list_menu_item:
			Intent intent2 = new Intent(this, WhitelistActivity.class);
			this.startActivity(intent2);
			return true;
		default: //handles icon click
			this.finish();
			return true;
	}
}

	@Override
	public void onRefresh() {
		this.telephoneLogAdapter.notifyDataSetChanged();
		this.swipeContainer.setRefreshing(false);
		
	}
}