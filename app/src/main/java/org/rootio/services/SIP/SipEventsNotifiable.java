package org.rootio.services.SIP;

import android.content.ContentValues;

import org.linphone.core.Call;

public interface SipEventsNotifiable {
    void updateCallState(Call.State callState, Call call, ContentValues values);

    void updateRegistrationState(org.linphone.core.RegistrationState registrationstate, ContentValues values);
}
