package org.rootio.activities.telephoneLog;

import java.util.ArrayList;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.telephony.Call;
import org.rootio.tools.telephony.CallStatus;
import org.rootio.tools.telephony.CallType;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TelephoneLogAdapter extends BaseAdapter {

	private ArrayList<Call> calls;
	private Context context;
	
	public TelephoneLogAdapter(Context context)
	{
		this.context = context;
		calls = getCallData();
	}
	
	/**
	 * Fetches call information from the database 
	 * @return ArrayList of Call objects each representing a record in the database
	 */
	private ArrayList<Call> getCallData()
	{
		String tableName = "calllog";
		DBAgent dbAgent = new DBAgent(this.context);
		String[] columnsToReturn  = new String[]{"telephonenumber","calltime","calltypeid","callstatusid"};
		String[][] data = dbAgent.getData(true, tableName, columnsToReturn, null, null, null, null, null, null);
		ArrayList<Call> calls = new ArrayList<Call>();
		for(int i = 0; i < data.length; i++)
		{
			
				calls.add(new Call(data[i][0],CallType.getCallType(data[i][2]), CallStatus.getCallStatus(data[i][3]), Utils.getDateFromString(data[i][1], "yyyy-MM-dd HH:mm:ss")));
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
		Call call = calls.get(index);
		if(view == null)
		{
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			view = inflater.inflate(R.layout.telephone_log_row, parent, false);
		}
		
		TextView phoneNumberTv = (TextView)view.findViewById(R.id.phone_number_tv);
		phoneNumberTv.setText(call.getTelephoneNumber());
		
		TextView callTypeTv = (TextView)view.findViewById(R.id.call_type_tv);
		callTypeTv.setText(call.getCallType().toString());
		
		TextView callTimeTv = (TextView)view.findViewById(R.id.call_time_tv);
		callTimeTv.setText(call.getCallTime() == null? "": Utils.getDateString(call.getCallTime(), "yyyy-MM-dd HH:mm:ss"));
		
		TextView callStatusTv = (TextView)view.findViewById(R.id.call_status_tv);
		callStatusTv.setText(call.getCallStatus().toString());
		
		return view;
	}
	
	
 
}
