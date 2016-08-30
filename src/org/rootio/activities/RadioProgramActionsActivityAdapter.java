package org.rootio.activities;

import org.rootio.radioClient.R;
import org.rootio.tools.media.Program;
import org.rootio.tools.utils.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RadioProgramActionsActivityAdapter extends BaseAdapter {

	private final Program program;

	RadioProgramActionsActivityAdapter(Program program) {
		this.program = program;
	}

	@Override
	public int getCount() {
		return this.program.getProgramActions().size();
	}

	@Override
	public Object getItem(int index) {
		return this.program.getProgramActions().get(index);

	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int index, View view, ViewGroup arg2) {
		if (view == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(arg2.getContext());
			view = layoutInflater.inflate(R.layout.station_activity_program_actions_row, arg2, false);
		}

		// render the type of program action
		TextView actionTypeTextView = (TextView) view.findViewById(R.id.station_activity_program_actions_type_tv);
		actionTypeTextView.setText(this.program.getProgramActions().get(index).toString());

		// render the start time of the program action
		TextView startTimeTextView = (TextView) view.findViewById(R.id.station_activity_program_actions_start_time_tv);
		startTimeTextView.setText(String.format("Start time: %s", Utils.getDateString(this.program.getProgramActions().get(index).getStartTime(), "HH:mm:ss")));
		return view;
	}
}
