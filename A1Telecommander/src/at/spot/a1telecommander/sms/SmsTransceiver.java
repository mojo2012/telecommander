package at.spot.a1telecommander.sms;

import java.util.ArrayList;

import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsTransceiver {
	final static String				TAG			= "A1Telecommander/SmsTransceiver";
	static private SmsTransceiver	instance	= null;
	SmsManager						sm			= SmsManager.getDefault();

	private SmsTransceiver() {
	}

	public static synchronized SmsTransceiver getInstance() {
		if (instance == null)
			instance = new SmsTransceiver();

		return instance;
	}

	public void sendShortMessage(String number, String message) {
		sm.sendTextMessage(number, null, message, null, null);
		Log.i(TAG, "Sending message to " + number + ", text=" + message);
	}

	class SmsMessageListenerEntry {
		ISmsMessageListener	listener		= null;
		String				number			= "";
		String				messageContains	= "";

		public SmsMessageListenerEntry(ISmsMessageListener listener, String number, String messageContains) {
			this.listener = listener;
			this.number = number;
			this.messageContains = messageContains;
		}
	}

	ArrayList<SmsMessageListenerEntry>	messageListeners	= new ArrayList<SmsMessageListenerEntry>();

	public void listenForMessage(ISmsMessageListener listener, String number) {
		SmsMessageListenerEntry listenerEntry = new SmsMessageListenerEntry(listener, number, null);

		messageListeners.add(listenerEntry);
	}

	public void listenForMessage(ISmsMessageListener listener, String number, String messageContains) {
		SmsMessageListenerEntry listenerEntry = new SmsMessageListenerEntry(listener, number, messageContains);

		messageListeners.add(listenerEntry);
	}

	public void unlistenForMessage(SmsMessageListenerEntry listenerEntry) {
		messageListeners.remove(listenerEntry);
	}

	public void onReceiveSms(SmsMessage message) {
		String sender = message.getOriginatingAddress();
		String text = message.getMessageBody().toString();

		Log.i(TAG, "INFO: receiving message from number " + sender + ", text=" + text);

		for (SmsMessageListenerEntry entry : messageListeners) {
			if (PhoneNumberUtils.compare(sender, entry.number)) {
				if (entry.messageContains == null || text.contains(entry.messageContains)) {
					informListeners(entry, sender, text);
					break;
				}
			}
		}
	}

	private void informListeners(SmsMessageListenerEntry entry, String sender, String text) {
		ISmsMessageListener listener = entry.listener;

		if (listener != null) {
			listener.messageReceived(text);
			unlistenForMessage(entry);
		}
	}

}
