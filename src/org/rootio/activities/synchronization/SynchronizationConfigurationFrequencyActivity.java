package org.rootio.activities.synchronization;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.FrequencyUnit;
import org.rootio.tools.utils.TimePickerPrompt;
import org.rootio.tools.utils.Utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

public class SynchronizationConfigurationFrequencyActivity extends
		FragmentActivity {

	private FrequencyUnit[] frequencyUnits;
	private SynchronizationConfiguration synchronizationConfiguration;
	private Spinner spinner;
	private TextView startTimeLabel, endTimeLabel, startTimeTextView, endTimeTextView;
	private CheckedTextView syncDuringParticularTimesCheckedTextView;
	private EditText quantityEditText;
	private TableRow startTimeTableRow, endTimeTableRow;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.synchronization_configuration_frequency);
		this.setTitle("Configure Frequency");
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.lookUpControls();
		this.prepareFrequencyDropdown();
		this.synchronizationConfiguration = new SynchronizationConfiguration(this);
		this.renderCurrentConfiguration(synchronizationConfiguration);
		this.setNotificationText();
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		default: //handles the click of the application icon
			this.finish();
			return false;
		}
	}
	
	/**
	 * Sets the notification text about general guidelines for setting the multicast address
	 */
	private void setNotificationText()
	{
		TextView tv = (TextView)this.findViewById(R.id.synchronization_configuration_note_tv);
		tv.setText(Html.fromHtml(this.getString(R.string.SynchronizationConfigurationNoteMessage)));
	}

	/**
	 * Look up views from the screen and assign them to reference variables 
	 */
	private void lookUpControls() {
		this.spinner = (Spinner) findViewById(R.id.synchronization_frequency_configuration_spn);
		this.quantityEditText = (EditText) findViewById(R.id.synchronization_frequency_configuration_etv);
		this.syncDuringParticularTimesCheckedTextView = (CheckedTextView) findViewById(R.id.synchronization_configure_frequency_syncduringparticulartimes_ctv);
		this.startTimeTextView = (TextView) findViewById(R.id.synchronization_configure_frequency_starttime_tv);
		this.endTimeTextView = (TextView) findViewById(R.id.synchronization_configure_frequency_endtime_tv);
		this.startTimeLabel = (TextView) this
				.findViewById(R.id.synchronization_configure_frequency_starttimelabel_tv);
		this.endTimeLabel = (TextView) this
				.findViewById(R.id.synchronization_configure_frequency_endtimelabel_tv);
		this.startTimeTableRow = (TableRow) this
				.findViewById(R.id.synchronization_configure_frequency_starttime_tbr);
		this.endTimeTableRow = (TableRow) this
				.findViewById(R.id.synchronization_configure_frequency_endtime_tbr);
	}

	/**
	 * Sets the selected time to be displayed on the relevant textview
	 * @param hour Integer representation of the selected hour
	 * @param minute Integer representation of the selected minute
	 * @param flag flag indicating whether this is the start time or end time
	 */
	public void setSelectedTime(int hour, int minute, int flag) {
		TextView textView; 
		if (flag == 1) {
			textView = this.startTimeTextView;
			textView.setText(String.format("%2d:%2d", hour, minute));
		} else if (flag == 2) {
			textView = this.endTimeTextView;
			textView.setText(String.format("%2d:%2d", hour, minute));
		}
		
	}

	/**
	 * Handler for click of the view that has start time definition
	 * @param v  The view (Table Row) containing start time information definition
	 */
	public void onSetStartTime(View v) {
		TimePickerPrompt timePicker = new TimePickerPrompt();
		timePicker
				.show(this.getSupportFragmentManager(), "Start Time", this, 1);
	}

	/**
	 * Handler for click of the view that has the end time definition 
	 * @param v The view (Table Row) containing end time information definition
	 */
	public void onSetEndTime(View v) {
		TimePickerPrompt timePicker = new TimePickerPrompt();
		timePicker.show(this.getSupportFragmentManager(), "End Time", this, 2);
	}

	/**
	 * Fetch available units for configuring synchronization frequency
	 * @return Array of frequency units that can be used to define the synchronization frequency
	 */
	private FrequencyUnit[] getFrequencyUnits() {
		String tableName = "frequencyunits";
		String[] columnsToReturn = new String[] { "_id", "title" };
		DBAgent agent = new DBAgent(this);
		String[][] result = agent.getData(true, tableName, columnsToReturn,
				null, null, null, null, null, null);
		FrequencyUnit[] frequencyUnitsTmp = new FrequencyUnit[result.length];
		for (int i = 0; i < result.length; i++) {
			frequencyUnitsTmp[i] = new FrequencyUnit(
					Integer.parseInt(result[i][0]), result[i][1]);
		}
		return frequencyUnitsTmp;
	}

	/**
	 * Renders the spinner that is used to choose a configuration frequency time unit.
	 */
	private void prepareFrequencyDropdown() {
		this.frequencyUnits = this.getFrequencyUnits();
		ArrayAdapter<FrequencyUnit> frequencyUnitsAdapter = new ArrayAdapter<FrequencyUnit>(
				this, android.R.layout.simple_spinner_item, frequencyUnits);
		frequencyUnitsAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.spinner.setAdapter(frequencyUnitsAdapter);
	}

	/**
	 * Handles the click of the save button to persist the defined configuration
	 * @param v The view (Save button) that was clicked
	 */
	public void onSave(View v) {
		EditText editText = (EditText) findViewById(R.id.synchronization_frequency_configuration_etv);
		if (Utils.validateNumber(editText.getText().toString())) {
			this.promptConfigurationChange();
		} else {
			Utils.warnOnScreen(this, "Please enter a valid number");
		}
	}

	/**
	 * Handles the click of the cancel button effectively finishing this activity
	 * @param v The view (Cancel button) that was clicked
	 */
	public void onCancel(View v) {
		this.finish();
	}

	/**
	 * Prompts the user for confirmation to alter the configuration, upon which the configuration is persisted
	 */
	private void promptConfigurationChange() {
		new AlertDialog.Builder(this).setIcon(R.drawable.questionmark)
				.setMessage("Change current configuration?")
				.setTitle("Confirmation")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						// get the quantity of units
						synchronizationConfiguration
								.setUnitId(frequencyUnits[spinner
										.getSelectedItemPosition()].getId());

						// get the id of the unit to be used
						synchronizationConfiguration.setQuantity(Integer
								.parseInt(quantityEditText.getText().toString()));

						// whether to sync during particular times
						synchronizationConfiguration
								.setSyncDuringParticularTimes(syncDuringParticularTimesCheckedTextView
										.isChecked());

						// starttime for the sync
						synchronizationConfiguration
								.setSyncStartTime(startTimeTextView.getText()
										.toString());

						// end time for the sync
						synchronizationConfiguration
								.setSyncEndTime(endTimeTextView.getText()
										.toString());

						synchronizationConfiguration.save();

					}
				}).setNegativeButton("Cancel", null).show();
	}

	/**
	 * Updates the screen to reflect the status of the configuration as persisted in the database.
	 * @param synchronizationConfiguration A SynchronizationConfiguration object representing the current frequency configuration
	 */
	private void renderCurrentConfiguration(SynchronizationConfiguration synchronizationConfiguration) {
		this.quantityEditText.setText(String.valueOf(synchronizationConfiguration.getQuantity()));
		//this.syncDuringParticularTimesCheckedTextView.setChecked(synchronizationConfiguration.syncDuringParticularTimes());
		this.toggleCheckState(this.syncDuringParticularTimesCheckedTextView, true, this.synchronizationConfiguration.syncDuringParticularTimes());
		this.startTimeTextView.setText(synchronizationConfiguration.getSyncStartTime());
		this.endTimeTextView.setText(synchronizationConfiguration.getSyncEndTime());
	}

	/**
	 * Handles check actions for the option to synchronize during particular times
	 * @param v The view (CheckedTextView) That was clicked
	 */
	public void onCheck(View v) {
		
		toggleCheckState(v, false,false);
	}

	/**
	 * Toggles the checked state of the specified CheckedTextView
	 * @param v The view that was checked
	 * @param isInit Whether the change of check status is due to user click or to initialization to persisted state. True: persisted state, False: user click
	 * @param initialValue
	 */
	private void toggleCheckState(View v, boolean isInit, boolean initialValue) {
		CheckedTextView checkedTextView = (CheckedTextView) v;
		checkedTextView.setChecked(isInit? initialValue: !checkedTextView.isChecked());
		checkedTextView.setCheckMarkDrawable(checkedTextView.isChecked() ? android.R.drawable.checkbox_on_background
						: android.R.drawable.checkbox_off_background);
		this.toggleControls(checkedTextView.isChecked());
	}

	/**
	 * Toggles the state of the views pertinent to synchronization during a particular time window, dependent on whether the option is enabled or not
	 * @param enabled Boolean representing whether the controls are enabled or not. True: enabled, False: disabled
	 */
	public void toggleControls(boolean enabled) {
		startTimeLabel.setEnabled(enabled);
		startTimeTextView.setEnabled(enabled);
		endTimeLabel.setEnabled(enabled);
		endTimeTextView.setEnabled(enabled);
		startTimeTableRow.setClickable(enabled);
		endTimeTableRow.setClickable(enabled);

	}
}
