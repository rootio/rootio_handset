package org.rootio.services.SIP;

import org.linphone.core.Call;
import org.linphone.core.ProxyConfig;

public interface SipEventsNotifiable {
    void updateCallState(Call.State callState, Call call);

    void updateRegistrationState(org.linphone.core.RegistrationState registrationstate, ProxyConfig proxyConfig);
}
