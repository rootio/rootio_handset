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

public class WhitelistActivity extends Activity {

	private ListView listView;
	private WhitelistAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstance)
	{
		
		super.onCreate(savedInstance);
		this.setContentView(R.layout.whitelist);
		listView = (ListView)this.findViewById(R.id.whitelist_lv);
		listView.setItemsCanFocus(false);
		adapter = new WhitelistAdapter(this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new ListViewItemClickListener());
		this.setTitle("Whitelisted Numbers");
	}
	
	@Override
	public void onResume()
    {
		super.onResume();
		this.refreshWhitelist();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = this.getMenuInflater();
		menuInflater.inflate(R.menu.whitelist_menu, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuItem)
	{
		if(menuItem.getItemId() == R.id.whitelistmenu_add_number_item)
		{
			Intent addWhitelistNumberIntent = new Intent(this, AddToWhitelist.class);
			this.startActivity(addWhitelistNumberIntent);
			return true;
		}
		return false;
	}
	
	private void uncheckAll()
	{
		
		for(int i = 0; i < listView.getChildCount(); i++)
		{
			    LinearLayout parent = (LinearLayout)listView.getChildAt(i);
			    CheckedTextView checkedTextView = (CheckedTextView)parent.findViewById(R.id.whitelisted_number_ctv);
			    checkedTextView.setChecked(false);
			    checkedTextView.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
			
		}
	}
	
	public void onUncheckAll(View v)
	{
		this.uncheckAll();
	}
	
	public void onRemove(View v)
	{
		for(int i = 0; i < listView.getChildCount(); i++)
		{
			LinearLayout parent = (LinearLayout)listView.getChildAt(i);
		    CheckedTextView checkedTextView = (CheckedTextView)parent.findViewById(R.id.whitelisted_number_ctv);
		    if(checkedTextView.isChecked())
		    {
		    	String msisdn = checkedTextView.getText().toString();
		    	this.deleteRecord(msisdn);
		    }
		}
		this.refreshWhitelist();
	}
	
	private int deleteRecord(String msisdn)
	{
		String tableName = "whitelist";
		String whereClause = "telephonenumber = ?";
		String[] args = new String[]{msisdn};
		DBAgent dbAgent = new DBAgent(this);
		return dbAgent.deleteRecords(tableName, whereClause, args);
    }
	
	private void refreshWhitelist()
	{
		adapter.refresh();
		adapter.notifyDataSetChanged();
	}
}
