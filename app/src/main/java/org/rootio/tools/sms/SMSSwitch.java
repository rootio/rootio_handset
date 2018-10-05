package org.rootio.tools.sms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.SmsMessage;

public class SMSSwitch {

    private String[] messageParts;
    private String from;
    private Context parent;

    @SuppressLint("DefaultLocale")
    public SMSSwitch(Context parent, SmsMessage message) {
        this.parent = parent;
        this.from = message.getOriginatingAddress();
        this.messageParts = this.getMessageParts(message.getMessageBody().toLowerCase());
    }

    /**
     * Gets a Message processor to be used to process the received message
     *
     * @return A MessageProcessor object to process the message
     */
    public MessageProcessor getMessageProcessor() {
        return this.switchSMS(this.messageParts);
    }

    /**
     * Tokenizes the message into parts that can be analyzed for actions
     *
     * @param message The message to be broken down
     * @return Array of strings representing tokens in the message
     */
    private String[] getMessageParts(String message) {
        return message.split("[|]");
     }

    /**
     * Examines the message parts and returns a suitable message processor to
     * process the message
     *
     * @param messageParts Tokens from the message to be analyzed
     * @return a MessageProcessor to process the message
     */
    private MessageProcessor switchSMS(String[] messageParts) {
        String keyword = messageParts.length > 0 ? messageParts[0] : "";
        if (keyword.equals("network")) {
            return new NetworkSMSHandler(this.parent, from, messageParts);
        }
        if (keyword.equals("station")) {

            return new StationSMSHandler(this.parent, from, messageParts);
        }
        if (keyword.equals("services")) {

            return new ServicesSMSHandler(this.parent, from, messageParts);
        }
        if (keyword.equals("resources")) {

            return new ResourcesSMSHandler(this.parent, from, messageParts);
        }
        if (keyword.equals("sound")) {

            return new SoundSMSHandler(this.parent, from, messageParts);
        }
        if (keyword.equals("whitelist")) {
            return new WhiteListSMSHandler(this.parent, from, messageParts);
        }
        return null;
    }
}
