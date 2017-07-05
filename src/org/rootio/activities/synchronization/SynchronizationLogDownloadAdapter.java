package org.rootio.activities.synchronization;

import java.util.ArrayList;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SynchronizationLogDownloadAdapter extends BaseAdapter{

	private ArrayList<SynchronizationLogDownloadRecord> synchronizationLogDownloadRecords;
	private Context context;

	SynchronizationLogDownloadAdapter(Context context, int offset, int limit)
	{
		this.context = context;
		this.synchronizationLogDownloadRecords = this.getSynchronizationRecords(offset, limit);
	}
	
	@Override
	public int getCount() {
		return this.synchronizationLogDownloadRecords.size();
	}

	@Override
	public Object getItem(int index) {
		return this.synchronizationLogDownloadRecords.get(index);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	
	@SuppressLint("NewApi")
	@Override
	public View getView(int index, View view, ViewGroup parent) {
		if(view == null)
		{
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			view = inflater.inflate(R.layout.synchronization_log_download_row,parent,false);
		}
		
		SynchronizationLogDownloadRecord currentRecord = this.synchronizationLogDownloadRecords.get(index);
		
		TextView changeIdTv = (TextView)view.findViewById(R.id.synchronization_log_download_changeid_tv);
		changeIdTv.setText(String.valueOf(currentRecord.getChangeId()));
		
		TextView changeDateTv = (TextView)view.findViewById(R.id.synchronization_log_download_changedate_tv);
		changeDateTv.setText(Utils.getDateString(currentRecord.getChangeDate(), "yyyy-MM-dd HH:mm:ss"));
		
		//determine the image to render
		ImageView icon = (ImageView)view.findViewById(R.id.synchronization_log_download_icon_imv);
		switch(currentRecord.getChangeTypeId())
		{
		case 1:
		     icon.setImageDrawable(view.getResources().getDrawable(R.drawable.calendar));
		     break;
		case 2:
			icon.setImageDrawable(view.getResources().getDrawable(R.drawable.music));
		     break;
		}
		
		//paint the background
		LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.synchronization_log_llt);
		int leftPadding = linearLayout.getPaddingLeft();
		int topPadding = linearLayout.getPaddingTop();
		int rightPadding = linearLayout.getPaddingRight();
		int bottomPadding = linearLayout.getPaddingBottom();
		
		switch(currentRecord.getDownloadStatusId())
		{
		case 1://pending
			linearLayout.setBackground(this.context.getResources().getDrawable(R.drawable.shadow_background));
			break;
		case 2://ongoing
			linearLayout.setBackground(this.context.getResources().getDrawable(R.drawable.yellow_background));
			break;
		case 3://done
			linearLayout.setBackground(this.context.getResources().getDrawable(R.drawable.green_background));
			break;
		case 4://failed
			linearLayout.setBackground(this.context.getResources().getDrawable(R.drawable.pink_background));
			break;
		}
		linearLayout.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
		return view;
	}
	
	
	/**
	 * Fetches Synchronization Download records from the database
	 * @param offset The number of records to skip
	 * @param limit The number of records to return
	 * @return ArrayList of SynchronizationLogDownloadRecord objects each representing a record in the database
	 */
	private ArrayList<SynchronizationLogDownloadRecord> getSynchronizationRecords(int offset, int limit)
	{
		String tableName = "downloadbacklog";
		String[] columnsToReturn  = new String[]{"changeid","changetypeid","changedate","downloadstatusid"};
		DBAgent agent = new DBAgent(this.context);
		String[][] results = agent.getData(true, tableName, columnsToReturn, null, null, null, null, null, null);
		ArrayList<SynchronizationLogDownloadRecord> synchronizationLogDownloadRecords = new ArrayList<SynchronizationLogDownloadRecord>();
		for(int i = 0; i < results.length; i++)
		{
			synchronizationLogDownloadRecords.add(new SynchronizationLogDownloadRecord(results[i][0],results[i][1],results[i][2],results[i][3]));
		}
		return synchronizationLogDownloadRecords;
    }

}
