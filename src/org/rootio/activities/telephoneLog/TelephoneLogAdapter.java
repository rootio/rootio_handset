package org.rootio.activities.telephoneLog;

import java.util.ArrayList;
import java.util.Date;

import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TelephoneLogAdapter extends BaseAdapter {

	private ArrayList<Object[]> calls;
	private Context parent;

	public TelephoneLogAdapter(Context parent) {
		this.parent = parent;
		calls = getCallData();
	}

	/**
	 * Fetches call information from the database
	 * 
	 * @return ArrayList of Call objects each representing a record in the
	 *         database
	 */
	private ArrayList<Object[]> getCallData() {
		ContentResolver cr = this.parent.getContentResolver();
		String[] columnsToReturn = new String[] { Calls.NUMBER, Calls.DATE, Calls.DURATION, Calls.TYPE };
		ArrayList<Object[]> calls = new ArrayList<Object[]>();
		Cursor cursor = cr.query(Calls.CONTENT_URI, columnsToReturn, null, null, Calls.DATE + " DESC");
		while(cursor.moveToNext())
		{
			calls.add(new Object[]{cursor.getString(0), cursor.getLong(1), cursor.getLong(2), cursor.getInt(3)});
		}
		return calls;
	}

	@Override
	public int getCount() {
		return this.calls.size();
	}

	@Override
	public Object getItem(int arg0) {
		return this.calls.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			view = inflater.inflate(R.layout.telephone_log_row, parent, false);
		}

		TextView phoneNumberTv = (TextView) view.findViewById(R.id.phone_number_tv);
		phoneNumberTv.setText((String)this.calls.get(index)[0]);

		TextView callTypeTv = (TextView) view.findViewById(R.id.call_type_tv);
		callTypeTv.setText(this.getCallType((Integer)this.calls.get(index)[3]));

		TextView callTimeTv = (TextView) view.findViewById(R.id.call_time_tv);
		callTimeTv.setText(Utils.getDateString(new Date((Long)this.calls.get(index)[1]), "yyyy-MM-dd HH:mm:ss"));

		TextView callStatusTv = (TextView) view.findViewById(R.id.call_duration_tv);
		callStatusTv.setText(this.getCallDuration((Long)this.calls.get(index)[2]));

		// Paint background basing on status
		LinearLayout background = (LinearLayout) view.findViewById(R.id.telephone_log_llt);
		int leftPadding = background.getPaddingLeft();
		int topPadding = background.getPaddingTop();
		int rightPadding = background.getPaddingRight();
		int bottomPadding = background.getPaddingBottom();
		//background.setBackgroundResource((Integer)this.calls.get(index)[2] ==  ? R.drawable.green_background : R.drawable.pink_background);
		background.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
		return view;
	}

	@SuppressLint("DefaultLocale") private CharSequence getCallDuration(Long seconds) {
	 return String.format("%02d:%02d:%02d",seconds / 3600, (seconds % 3600)/ 60, (seconds % 3600) % 60);
	}

	private CharSequence getCallType(Integer id) {
		switch(id)
		{
		case Calls.INCOMING_TYPE:
			return "Incoming";
		case Calls.MISSED_TYPE:
			return "Missed";
		case Calls.OUTGOING_TYPE:
			return "Outgoing";
			default: 
				return null;
		}
	}

}
