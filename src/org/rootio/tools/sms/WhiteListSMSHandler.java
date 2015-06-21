package org.rootio.tools.sms;

import org.rootio.tools.persistence.DBAgent;

import android.content.ContentValues;
import android.content.Context;

public class WhiteListSMSHandler implements MessageProcessor {

	private String[] messageParts;
	private final String from;
	private final Context parent;

	WhiteListSMSHandler(Context parent, String from, String[] messageParts) {
		this.parent = parent;
		this.from = from;
		this.messageParts = messageParts;
	}

	@Override
	public boolean ProcessMessage() {
		if (messageParts.length != 3) {
			return false;
		}

		// adding a number
		if (messageParts[1].equals("add")) {
			try {
				boolean isNumberAdded = this.addNumberToWhitelist(messageParts[2]);
				this.respondAsyncStatusRequest(from, isNumberAdded?String.format("The number %s was successfully added", messageParts[2]): String.format("The number %s was not added", messageParts[2]));
                return isNumberAdded;
			} catch (Exception ex) {
				return false;
			}
		} 

		// removing a number nfrom wite;list
		if (messageParts[1].equals("remove")) {
			try {
				boolean isNumberRemoved = this.removeNumberFromWhitelist(messageParts[2]);
				this.respondAsyncStatusRequest(from, isNumberRemoved?String.format("The number %s was successfully removed", messageParts[2]): String.format("The number %s was not added", messageParts[2]));
                return  isNumberRemoved;
			} catch (Exception ex) {
				return false;
			}
		}
		return false;
	}

	private boolean addNumberToWhitelist(String phoneNumber) {
		try {
			DBAgent dbAgent = new DBAgent(this.parent);
			String tableName = "whitelist";
			ContentValues data = new ContentValues();
			data.put("telephonenumber", phoneNumber);
			dbAgent.saveData(tableName, "", data);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean removeNumberFromWhitelist(String phoneNumber) {
		try {
			DBAgent dbAgent = new DBAgent(this.parent);
			String tableName = "whitelist";
			String whereClause = "telephonenumber = ?";
			String[] whereArgs = new String[] { phoneNumber };
			dbAgent.deleteRecords(tableName, whereClause, whereArgs);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	@Override
	public void respondAsyncStatusRequest(String from, String data) {
		// TODO Auto-generated method stub

	}

}
