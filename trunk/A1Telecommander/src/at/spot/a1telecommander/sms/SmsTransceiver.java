package at.spot.a1telecommander.sms;

import java.util.ArrayList;

import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SmsTransceiver {
	final static String				TAG			= "A1Telecommander/SmsTransceiver";
	static private SmsTransceiver	instance	= null;
	SmsManager						sm			= SmsManager.getDefault();

	private SmsTransceiver() {
	}

	public static SmsTransceiver getInstance() {
		if (instance == null)
			instance = new SmsTransceiver();

		return instance;
	}

	public void sendShortMessage(String number, String message) {
		sm.sendTextMessage(number, null, message, null, null);
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

		for (SmsMessageListenerEntry entry : messageListeners) {
			if (sender.equals(entry.number))
				if (text.contains(entry.messageContains)) {
					ISmsMessageListener listener = entry.listener;

					if (listener != null) {
						listener.messageReceived(text);
						unlistenForMessage(entry);
					}
					break;
				}
		}
	}
}
