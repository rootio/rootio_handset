package org.rootio.tools.telephony;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.radioClient.R;

import android.content.Context;
import android.util.Log;

public class CallAuthenticator {

	private Context parent;
	private JSONObject whiteList;

	public CallAuthenticator(Context parent) {
		this.parent = parent;
		this.whiteList = this.loadWhiteList();
	}

	private JSONObject loadWhiteList() {
		InputStream instr = null; 
		try {
			instr = this.parent.getAssets().open("whitelist.json");
			byte[] buffer = new byte[1024];
			instr.read(buffer);
			return new JSONObject(new String(buffer));
		} catch (IOException ex) {
			Log.e(this.parent.getString(R.string.app_name),
					ex.getMessage() == null ? "NullPointerException(CallAuthenticator.loadWhitelist)"
							: ex.getMessage());
		} catch (JSONException ex) {
			Log.e(this.parent.getString(R.string.app_name),
					ex.getMessage() == null ? "NullPointerException(CallAuthenticator.loadWhitelist)"
							: ex.getMessage());
		} finally {
			try {
				instr.close();
			} catch (Exception ex) {
				Log.e(this.parent.getString(R.string.app_name),
						ex.getMessage() == null ? "NullPointerException(CallAuthenticator.loadWhitelist)"
								: ex.getMessage());
			}
		}
		return null;
	}

	public boolean isWhiteListed(String phoneNumber) {
		try {
			String sanitizedPhoneNumber = this.sanitizePhoneNumber(phoneNumber);
			return this.whiteList.getJSONArray("whitelist").toString()
					.contains(sanitizedPhoneNumber); // potentially problematic
		} catch (JSONException ex) {
			Log.e(this.parent.getString(R.string.app_name),
					ex.getMessage() == null ? "NullPointerException(CallAuthenticator.isWhiteListed)"
							: ex.getMessage());
		} catch (NullPointerException ex) {
			Log.e(this.parent.getString(R.string.app_name),
					ex.getMessage() == null ? "NullPointerException(CallAuthenticator.isWhiteListed)"
							: ex.getMessage());
		}
		return false;
	}

	private String sanitizePhoneNumber(String phoneNumber) {
		return phoneNumber.trim();
	}
}
