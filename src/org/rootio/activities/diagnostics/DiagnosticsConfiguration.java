package org.rootio.activities.diagnostics;

import java.util.Date;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

public class DiagnosticsConfiguration {

	private Context parent;
	private int unitId;
	private int quantity;
	private Date changeDate;

	public DiagnosticsConfiguration(Context parent) {
		this.parent = parent;
		this.loadConfigurationInformation();
	}

	private void loadConfigurationInformation() {
		String tableName = "frequencyconfiguration";
		String[] columnsToReturn = new String[] { "title", "frequencyunitid", "changedate", "quantity" };
		DBAgent agent = new DBAgent(this.parent);
		String[][] results = agent.getData(true, tableName, columnsToReturn, null, null, null, null, null, null);
		if (results.length > 0) {
			this.unitId = Utils.parseIntFromString(results[0][1]);
			this.changeDate = Utils.getDateFromString(results[0][2], "yyyy-MM-dd HH:mm:ss");
			this.quantity = Utils.parseIntFromString(results[0][3]);
		}
	}

	public int getUnitId() {
		return this.unitId;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public Date getChangeDate() {
		return this.changeDate;
	}

	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int persist() {
		String tableName = "frequencyconfiguration";
		String whereClause = "title = ?";
		String[] whereArgs = new String[] { "diagnostics" };
		ContentValues data = new ContentValues();
		data.put("frequencyunitid", this.unitId);
		data.put("quantity", quantity);
		DBAgent agent = new DBAgent(this.parent);
		int result = agent.updateRecords(tableName, data, whereClause, whereArgs);
		this.loadConfigurationInformation();
		return result;
	}
}
