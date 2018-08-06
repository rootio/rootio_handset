package org.rootio.tools.telephony;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.rootio.handset.R;
import org.rootio.tools.utils.Utils;

import android.content.Context;
import android.util.Log;

public class CallAuthenticator {

    private Context parent;
    private JSONObject whiteList;

    public CallAuthenticator(Context parent) {
        this.parent = parent;
        try {
            this.whiteList = new JSONObject((String) Utils.getPreference("whitelist", String.class, this.parent));
        } catch (Exception e) {
            Log.e(this.parent.getString(R.string.app_name), e.getMessage() == null ? "NullPointerException(CallAuthenticator())" : e.getMessage());
        }
    }


    public boolean isWhiteListed(String phoneNumber) {
        try {
            String sanitizedPhoneNumber = this.sanitizePhoneNumber(phoneNumber);
            return this.whiteList.getJSONArray("whitelist").toString().contains(sanitizedPhoneNumber); // potentially
            // problematic
        } catch (JSONException ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "NullPointerException(CallAuthenticator.isWhiteListed)" : ex.getMessage());
        } catch (NullPointerException ex) {
            Log.e(this.parent.getString(R.string.app_name), ex.getMessage() == null ? "NullPointerException(CallAuthenticator.isWhiteListed)" : ex.getMessage());
        }
        return false;
    }

    private String sanitizePhoneNumber(String phoneNumber) {
        return phoneNumber.trim();
    }
}
