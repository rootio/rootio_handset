package org.rootio.activities.synchronization;

import java.util.ArrayList;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SynchronizationLogAdapter extends BaseAdapter {

	private ArrayList<SynchronizationRecord> synchronizationRecords;
	private Context context;

	SynchronizationLogAdapter(Context context) {
		this.context = context;
		this.synchronizationRecords = this.getSynchronizationRecords();
	}

	@Override
	public int getCount() {
		return this.synchronizationRecords.size();
	}

	@Override
	public Object getItem(int index) {
		return this.synchronizationRecords.get(index);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			view = inflater.inflate(R.layout.synchronization_log_row, parent, false);
		}

		SynchronizationRecord currentRecord = this.synchronizationRecords.get(index);

		TextView dateTv = (TextView) view.findViewById(R.id.synchronization_log_date_tv);
		dateTv.setText(Utils.getDateString(currentRecord.getSynchronizationDate(), "yyyy-MM-dd HH:mm:ss"));

		TextView HTTPResponseCodeTv = (TextView) view.findViewById(R.id.synchronization_log_http_tv);
		HTTPResponseCodeTv.setText(String.valueOf(currentRecord.getHTTPResponseCode()));

		TextView changesCountTv = (TextView) view.findViewById(R.id.synchronization_log_changes_tv);
		changesCountTv.setText(String.valueOf(currentRecord.getChangeCount()));

		return view;
	}

	/**
	 * Gets stored synchronization records from the database
	 * 
	 * @return ArrayList of SynchronizationRecord objects
	 */
	ArrayList<SynchronizationRecord> getData() {
		return this.synchronizationRecords;
	}

	/**
	 * Fetches stored synchronization records from the database
	 * 
	 * @return ArrayList of SynchronizationRecord objects each representing a
	 *         record in the database
	 */
	private ArrayList<SynchronizationRecord> getSynchronizationRecords() {
		String tableName = "synchronizationlog";
		String[] columnsToReturn = new String[] { "_id", "synchronizationdate", "httpresponsecode", "changesetcount" };
		DBAgent agent = new DBAgent(this.context);
		String[][] results = agent.getData(true, tableName, columnsToReturn, null, null, null, null, null, null);
		ArrayList<SynchronizationRecord> synchronizationRecords = new ArrayList<SynchronizationRecord>();
		for (String[] result : results) {
			synchronizationRecords.add(new SynchronizationRecord(result[0], result[1], result[2], result[3]));
		}
		return synchronizationRecords;
	}

}
