package org.rootio.activities.telephoneLog.lists;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class BlackListActivity extends Activity {

	private ListView listView;
	private BlacklistAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstance)
	{
		
		super.onCreate(savedInstance);
		this.setContentView(R.layout.blacklist);
		listView = (ListView)this.findViewById(R.id.blacklist_lv);
		listView.setItemsCanFocus(false);
		adapter = new BlacklistAdapter(this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new ListViewItemClickListener());
		this.setTitle("Blacklisted Numbers");
	}
	
	@Override
	public void onResume()
    {
		super.onResume();
		this.refreshBlacklist();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = this.getMenuInflater();
		menuInflater.inflate(R.menu.blacklist_menu, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuItem)
	{
		if(menuItem.getItemId() == R.id.blacklistmenu_add_number_item)
		{
			Intent addBlacklistNumberIntent = new Intent(this, AddToBlacklist.class);
			this.startActivity(addBlacklistNumberIntent);
			return true;
		}
		return false;
	}
	
	/**
	 * Handles the click of the button to unselect all the selected numbers
	 * @param v The view (uncheck all button) that was selected
	 */
	public void onUncheckAll(View v)
	{
		for (int i = 0; i < listView.getChildCount(); i++) {
			LinearLayout parent = (LinearLayout) listView.getChildAt(i);
			CheckedTextView checkedTextView = (CheckedTextView) parent.findViewById(R.id.blacklisted_number_ctv);
			checkedTextView.setChecked(false);
			checkedTextView.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
		}
	}
	
	/**
	 * Handles the click for the button to remove a given number from the blacklist
	 * @param v The view (remove button) that was clicked
	 */
	public void onRemove(View v)
	{
		for(int i = 0; i < listView.getChildCount(); i++)
		{
			LinearLayout parent = (LinearLayout)listView.getChildAt(i);
		    CheckedTextView checkedTextView = (CheckedTextView)parent.findViewById(R.id.blacklisted_number_ctv);
		    if(checkedTextView.isChecked())
		    {
		    	String msisdn = checkedTextView.getText().toString();
		    	this.deleteRecord(msisdn);
		    }
		}
		this.refreshBlacklist();
	}
	
	/**
	 * Removes a specified number from the blacklist
	 * @param msisdn The MSISDN to be removed from the blacklist
	 * @return Integer number of rows affected by this transaction
	 */
	private int deleteRecord(String msisdn)
	{
		String tableName = "blacklist";
		String whereClause = "telephonenumber = ?";
		String[] args = new String[]{msisdn};
		DBAgent dbAgent = new DBAgent(this);
		return dbAgent.deleteRecords(tableName, whereClause, args);
    }
	
	/**
	 * Refreshes the adapter forcing a refresh of the data being displayed
	 */
	private void refreshBlacklist()
	{
		adapter.refresh();
		adapter.notifyDataSetChanged();
	}
}
