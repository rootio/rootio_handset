package org.rootio.activities.diagnostics;

import org.rootio.radioClient.R;
import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.FrequencyUnit;
import org.rootio.tools.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class DiagnosticsConfigurationFrequencyActivity extends Activity {

	private FrequencyUnit[] frequencyUnits;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.diagnostics_configuration_frequency);
		this.prepareFrequencyDropdown();
		this.setTitle("Set Diagnostics Frequency");
	}

	/**
	 * Fetches the units that can be used to configure the interval for diagnostic checks
	 * @return An array of the frequency units that are available for use in setting diagnostic check interval
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
	 * Renders the spinner that is used to select the units used for configuration of frequency
	 */
	private void prepareFrequencyDropdown() {
		this.frequencyUnits = this.getFrequencyUnits();
		ArrayAdapter<FrequencyUnit> frequencyUnitsAdapter = new ArrayAdapter<FrequencyUnit>(
				this, android.R.layout.simple_spinner_item, frequencyUnits);
		frequencyUnitsAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinner = (Spinner) this
				.findViewById(R.id.diagnostic_frequency_configuration_spn);
		spinner.setAdapter(frequencyUnitsAdapter);
	}

	/**
	 * Handles the click of the save button
	 * @param v The view (save button) that was clicked
	 */
	public void onSave(View v) {
		EditText editText = (EditText) findViewById(R.id.diagnostic_frequency_configuration_etv);
		if (Utils.validateNumber(editText.getText().toString())) {
			this.promptConfigurationChange();
		} else {
			Utils.warnOnScreen(this, "Please enter a valid number");
		}
	}

	/**
	 * Handles the click of the cancel button, effectively offloading the activity.
	 * @param v
	 */
	public void onCancel(View v) {
		this.finish();
	}

	/**
	 * Prompts for confirmation of alteration of the current configuration upon which the configuration changes are persisted to the database
	 */
	private void promptConfigurationChange() {
		new AlertDialog.Builder(this).setIcon(R.drawable.questionmark)
				.setMessage("Change current configuration?")
				.setTitle("Confirmation")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Spinner spinner = (Spinner) findViewById(R.id.diagnostic_frequency_configuration_spn);
						EditText editText = (EditText) findViewById(R.id.diagnostic_frequency_configuration_etv);
						runParameterUpdate("diagnostics",
								frequencyUnits[spinner
										.getSelectedItemPosition()].getId(),
								Integer.parseInt(editText.getText().toString()));

					}
				}).setNegativeButton("Cancel", null).show();
	}

	/**
	 * Updates the configuration of frequency at which to take diagnostic checks
	 * @param title The parameter whose frequency is being updated
	 * @param frequencyUnitId The unit that will be used to measure the frequency
	 * @param quantity The amount of units of frequency at whose interval to take diagnostic checks
	 * @return
	 */
	private int runParameterUpdate(String title, int frequencyUnitId,
			int quantity) {
		String tableName = "frequencyconfiguration";
		String whereClause = "title = ?";
		String[] whereArgs = new String[] { title };
		ContentValues data = new ContentValues();
		data.put("frequencyunitid", frequencyUnitId);
		data.put("quantity", quantity);
		DBAgent agent = new DBAgent(this);
		return agent.updateRecords(tableName, data, whereClause, whereArgs);
	}
}
