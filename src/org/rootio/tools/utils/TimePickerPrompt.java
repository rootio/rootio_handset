package org.rootio.tools.utils;

import java.util.Calendar;

import org.rootio.activities.synchronization.SynchronizationConfigurationFrequencyActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;


@SuppressLint("NewApi")
public class TimePickerPrompt extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
	
	private SynchronizationConfigurationFrequencyActivity parent;
	private int flag;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, true);

    }

	@Override
	public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
		parent.setSelectedTime(hourOfDay, minute, this.flag);
		
	}
	
	public void show(FragmentManager manager, String tag, SynchronizationConfigurationFrequencyActivity parent, int flag)
	{
		this.parent = parent;
		this.flag = flag;
		this.show(manager, tag);
	}
}
