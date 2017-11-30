package org.rootio.activities.services;

public interface TelephonyEventNotifiable {

    /**
     * Notifies the implementing class that the phone is in a phone call for all
     * other audio to be paused. Also notifies when the phone is out of call so
     * that all the audio can be resumed
     *
     * @param isInCall Boolean indicating whether the phone is in call or not. True:
     *                 in call, False: not in call
     */
    void notifyTelephonyStatus(boolean isInCall);
}
