package org.rootio.tools.sms;

interface UssdResultNotifiable {
    void notifyUssdResult(String s, String s1, int resultCode);
}
