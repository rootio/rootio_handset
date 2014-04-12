package org.rootio.activities.stationDetails;

import java.util.ArrayList;

import org.rootio.radioClient.R;
import org.rootio.tools.media.Program;
import org.rootio.tools.radio.ProgramSlot;
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

@SuppressLint("NewApi")
public class StationActivityAdapter extends BaseAdapter {

	private ArrayList<ProgramSlot> programSlots;
	private Context Parent;

	public StationActivityAdapter(Context parent, ArrayList<ProgramSlot> programSlots) {
		this.programSlots = programSlots;
		this.Parent = parent;
	}

	@Override
	public int getCount() {
		return programSlots.size();
	}

	@Override
	public Object getItem(int index) {
		return programSlots.get(index);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
		if (view == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(parent
					.getContext());
			view = layoutInflater.inflate(R.layout.station_activity_row, null);
		}

		Program program = this.programSlots.get(index).getProgram();

		// determine the image to render
		ImageView icon = (ImageView) view.findViewById(R.id.progam_icon_iv);
		switch (program.getProgramType()) {
		case Media:
			icon.setImageResource(R.drawable.music);
			break;
		case Call:
			icon.setImageResource(R.drawable.phone);
			break;
		case Stream:
			icon.setImageResource(R.drawable.multicast);
			break;
		default:
		    icon.setImageResource(R.drawable.music);
			break;
		}
		
		//render the title
		TextView titleTextView = (TextView)view.findViewById(R.id.program_title_tv);
		titleTextView.setText(program.getTitle());
		
		//render the start time
		TextView startTimeTextView = (TextView)view.findViewById(R.id.program_starttime_tv);
		startTimeTextView.setText(Utils.getDateString(program.getEventTimes()[this.programSlots.get(index).getScheduledIndex()].getScheduledDate(), "HH:mm:ss")); //fix this
	
		//render the duration
		TextView durationTextView = (TextView)view.findViewById(R.id.program_duration_tv);
		durationTextView.setText(String.valueOf(program.getEventTimes()[this.programSlots.get(index).getScheduledIndex()].getDuration())); //fix this
		
		//determine the running state
		LinearLayout programLinearLayout = (LinearLayout)view.findViewById(R.id.program_lt);
		int leftPadding = programLinearLayout.getPaddingLeft();
		int topPadding = programLinearLayout.getPaddingTop();
		int rightPadding = programLinearLayout.getPaddingRight();
		int bottomPadding = programLinearLayout.getPaddingBottom();
		
		switch(this.programSlots.get(index).getRunState())
		{
		case 0: //not started
			programLinearLayout.setBackground(this.Parent.getResources().getDrawable(R.drawable.yellow_background));//yellow
			break;
		case 1: //running
			programLinearLayout.setBackground(this.Parent.getResources().getDrawable(R.drawable.green_background));//green
			break;
		case 2: //terminated
			programLinearLayout.setBackground(this.Parent.getResources().getDrawable(R.drawable.pink_background));//red
			break;
		}
		programLinearLayout.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
		return view;
	}
}
