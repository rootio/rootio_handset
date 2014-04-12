package org.rootio.activities.telephoneLog.lists;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddToBlacklist extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.add_to_blacklist);
		this.setTitle("Add Number to Blacklist");
	}
	
	/**
	 * Handles the click of the Add button
	 * @param v the view (Add Button) that was clicked
	 */
	public void onAddNumber(View v)
	{
		EditText editText = (EditText)this.findViewById(R.id.addBlacklistNumberEt);
		String msisdn = editText.getText().toString();
		long result = this.addBlacklistNumber(msisdn);
		this.showResult(msisdn, result);
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
	 * Persists a blacklisted number
	 * @param msisdn The MSISDN to be added to the blacklist
	 * @return ID of the record that was written to the database
	 */
	private long addBlacklistNumber(String msisdn)
	{
		String tableName = "blacklist";
		ContentValues values = new ContentValues();
		values.put("telephonenumber", msisdn);
		DBAgent agent = new DBAgent(this);
		return agent.saveData(tableName, null, values);
	}
	
	/**
	 * Shows the result of the operation of persisting the record to the database
	 * @param msisdn The MSISDN that was being added to the blacklist
	 * @param result The number of rows that was affected by the persistence
	 */
	private void showResult(String msisdn, long result)
	{
		Utils.toastOnScreen(result > 0? String.format("The number %s was successfully blacklisted",msisdn) : String.format("Blacklisting the number %s failed. Please check the number format and try again",msisdn));
	    this.finish();
	}
}
