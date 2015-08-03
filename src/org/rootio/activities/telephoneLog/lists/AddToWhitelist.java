package org.rootio.activities.telephoneLog.lists;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class AddToWhitelist extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.add_to_whitelist);
		this.setTitle("Add Number to Whitelist");
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		default: //handles the click of the application icon
			this.finish();
			return false;
		}
	}
	
	/**
	 * Handles the click of the Add button
	 * @param v the view (Add Button) that was clicked
	 */
	public void onAddNumber(View v)
	{
		EditText editText = (EditText)this.findViewById(R.id.addWhitelistNumberEt);
		String msisdn = editText.getText().toString();
		long result = this.addWhitelistNumber(msisdn);
		this.announceResult(msisdn, result);
	}
	
	/**
	 * Handles the click of the cancel button effectively finishing the activity
	 * @param v The View (cancel button) that was clicked
	 */
	public void onCancel(View v)
	{
		this.finish();
	}
	
	/**
	 * Persists a whitelisted number
	 * @param msisdn The MSISDN to be added to the whitelist
	 * @return ID of the record that was written to the database
	 */
	private long addWhitelistNumber(String msisdn)
	{
		String tableName = "whitelist";
		ContentValues values = new ContentValues();
		values.put("telephonenumber", msisdn);
		DBAgent agent = new DBAgent(this);
		return agent.saveData(tableName, null, values);
	}
	
	/**
	 * Shows the result of the operation of persisting the record to the database
	 * @param msisdn The MSISDN that was being added to the whitelist
	 * @param result The number of rows that was affected by the persistence
	 */
	private void announceResult(String msisdn, long result)
	{
		Utils.toastOnScreen(result > 0? String.format("The number %s was successfully whitelisted",msisdn) : String.format("Whitelisting the number %s failed. Please check the number format and try again",msisdn), this);
	    this.finish();
	}
}
