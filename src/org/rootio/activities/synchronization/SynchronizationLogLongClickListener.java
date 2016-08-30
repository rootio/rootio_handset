package org.rootio.activities.synchronization;

import java.util.ArrayList;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class SynchronizationLogLongClickListener implements OnItemClickListener {

	private ArrayList<SynchronizationRecord> synchronizationRecords;

	SynchronizationLogLongClickListener(ArrayList<SynchronizationRecord> synchronizationRecords) {
		this.synchronizationRecords = synchronizationRecords;
	}

	@Override
	public void onItemClick(AdapterView<?> viewgroup, View view, int index, long id) {
		Intent intent = new Intent(view.getContext(), SynchronizationLogDownloadActivity.class);
		intent.putExtra("index", this.synchronizationRecords.get(index).getId());
		view.getContext().startActivity(intent);
	}

}
