package org.rootio.services.synchronization;

import java.util.Date;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

public class SynchronizationUtils {

	private Context parent;

	public SynchronizationUtils(Context parent) {
		this.parent = parent;
	}

	public Date getLastUpdateDate(SynchronizationType synchronizationType) {
		String tableName = "downloadbacklog";
		String[] columns = new String[] { "updatedate" };
		String whereClause = "changetypeid = ?";
		String[] whereArgs = new String[] { String.valueOf(synchronizationType.ordinal() + 1) };
		String orderBy = "updatedate desc";
		// String limit = " 1";
		DBAgent agent = new DBAgent(this.parent);

		String[][] result = agent.getData(true, tableName, columns, whereClause, whereArgs, null, null, orderBy, null);
		return result.length > 0 ? Utils.getDateFromString(result[0][0], "yyyy-MM-dd HH:mm:ss") : null;
	}

	/**
	 * Logs the result of this synchronization session
	 * 
	 * @param synchronizationType
	 *            The type of content that was being synchronizes
	 * @param id
	 *            The ID of the object created by this synchronization
	 * @param status
	 *            Integer representing the status of the synchronization.
	 */
	void logSynchronization(SynchronizationType synchronizationType, long id, int status, Date lastUpdateDate) {
		String tableName = "downloadBacklog";
		ContentValues data = new ContentValues();
		data.put("changetypeid", synchronizationType.ordinal() + 1);
		data.put("changeid", id);
		data.put("downloadstatusid", status);
		data.put("updatedate", Utils.getDateString(lastUpdateDate, "yyyy-MM-dd HH:mm:ss"));
		DBAgent dbAgent = new DBAgent(this.parent);
		dbAgent.saveData(tableName, null, data);
	}
}
