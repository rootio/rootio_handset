package org.rootio.activities.telephoneLog.lists;

import java.util.ArrayList;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

public class WhitelistAdapter extends BaseAdapter {

	private ArrayList<String> numbers;
	private Context context;
	
	public WhitelistAdapter(Context context)
	{
		this.context = context;
		this.refresh();
	}
	
	/**
	 * Refreshes information in the adapter from the database
	 */
	public void refresh()
	{
		this.numbers = getWhitelistedNumbers();
	}
	
	/**
	 * Fetches whitelisted numbers form the database
	 * @return An ArrayList of Strings each representing a whitelisted number
	 */
	private ArrayList<String> getWhitelistedNumbers()
	{
		ArrayList<String> numbers = new ArrayList<String>();
		String tableName = "whitelist";
		DBAgent dbAgent = new DBAgent(this.context);
		String[] columnsToReturn = new String[]{"telephonenumber"};
		String[][] results = dbAgent.getData(true, tableName, columnsToReturn, null, null, null, null, null, null);
		for(String[] result : results)
		{
			numbers.add(result[0]);
		}
		return numbers;
	}
	
	@Override
	public int getCount() {
		return numbers.size();
	}

	@Override
	public Object getItem(int index) {
		return numbers.get(index);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
		if(view == null)
		{
			LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
			view = layoutInflater.inflate(R.layout.whitelist_row, parent,false);
		}
		
		CheckedTextView checkedTv = (CheckedTextView)view.findViewById(R.id.whitelisted_number_ctv);
		checkedTv.setText(numbers.get(index));
		
		return view;
	}
}
