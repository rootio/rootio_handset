package org.rootio.activities.telephoneLog.lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.handset.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WhitelistAdapter extends BaseAdapter {

	private ArrayList<String> numbers;
	private Context parent;

	public WhitelistAdapter(Context parent) {
		this.parent = parent;
		this.refresh();
	}

	/**
	 * Refreshes information in the adapter from the database
	 */
	public void refresh() {
		this.numbers = getWhitelistedNumbers();
	}

	/**
	 * Fetches whitelisted numbers form the database
	 * 
	 * @return An ArrayList of Strings each representing a whitelisted number
	 */
	private ArrayList<String> getWhitelistedNumbers() {
		ArrayList<String> numbers = new ArrayList<String>();
		try {
			JSONObject whitelistJson = this.loadWhiteList();
			JSONArray whitelist = whitelistJson.getJSONArray("whitelist");
			for (int i = 0; i < whitelist.length(); i++) {
				numbers.add(whitelist.get(i).toString());
			}
		} catch (Exception ex) {
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "NullPointerException(WhitelistAdaptor.getWhitelistedNumber)" : ex.getMessage());
		}
		return numbers;
	}

	private JSONObject loadWhiteList() {
		FileInputStream instr = null;
		try {
			File whitelistFile = new File(this.parent.getFilesDir().getAbsolutePath() + "/whitelist.json");

			instr = new FileInputStream(whitelistFile);
			byte[] buffer = new byte[1024];
			instr.read(buffer);
			return new JSONObject(new String(buffer));
		} catch (IOException ex) {
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "NullPointerException(CallAuthenticator.loadWhitelist)" : ex.getMessage());
		} catch (JSONException ex) {
			Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "NullPointerException(CallAuthenticator.loadWhitelist)" : ex.getMessage());
		} finally {
			try {
				instr.close();
			} catch (Exception ex) {
				Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "NullPointerException(CallAuthenticator.loadWhitelist)" : ex.getMessage());
			}
		}
		return null;
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
		if (view == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
			view = layoutInflater.inflate(R.layout.whitelist_row, parent, false);
		}

		TextView checkedTv = (TextView) view.findViewById(R.id.whitelisted_number_ctv);
		checkedTv.setText(numbers.get(index));

		return view;
	}
}
