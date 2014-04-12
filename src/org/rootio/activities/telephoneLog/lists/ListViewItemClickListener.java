package org.rootio.activities.telephoneLog.lists;

import org.rootio.radioClient.R;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ListViewItemClickListener implements OnItemClickListener {

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		parent.setSelection(index);
		ListView parentView = ((ListView)parent);
		LinearLayout linearLayout = (LinearLayout)parentView.getChildAt(index);
		CheckedTextView checkedTextView = (CheckedTextView)linearLayout.findViewById(R.id.blacklisted_number_ctv);
		toggleCheckStatus(checkedTextView);
    }
	
	/**
	 * Handles the checking of the various checked text views
	 * @param checkedTextView The Checked text view that whose checked status has changed
	 */
	private void toggleCheckStatus(CheckedTextView checkedTextView)
	{
		checkedTextView.setChecked(!checkedTextView.isChecked());
		checkedTextView.setCheckMarkDrawable(checkedTextView.isChecked()? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);
	}
	

}
