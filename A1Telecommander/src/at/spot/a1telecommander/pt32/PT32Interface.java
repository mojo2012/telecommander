package at.spot.a1telecommander.pt32;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Looper;
import android.util.Log;
import at.spot.a1telecommander.settings.A1TelecommanderSettings;
import at.spot.a1telecommander.sms.ISmsMessageListener;
import at.spot.a1telecommander.sms.SmsTransceiver;

public class PT32Interface implements ISmsMessageListener,
		IThermostatInterface {

	static PT32Interface	instance		= null;
	final static String		TAG				= "A1Telecommander/PT32Intertface";

	A1TelecommanderSettings	settings		= A1TelecommanderSettings.getInstance();
	SmsTransceiver			smsTransceiver	= null;
	String					number			= "";

	HeatingMode				heatingMode		= HeatingMode.Unknown;
	int						heatingDegrees	= -1;

	String					lastAnswer		= "";

	Timer					timer			= null;
	boolean					canceledTimer	= false;
	int						timeout			= 300000;
	public boolean			canceled		= false;

	private PT32Interface() {
		number = settings.telephoneNumber;
		smsTransceiver = SmsTransceiver.getInstance();
	}

	public static boolean	fakeMode	= false;

	public static IThermostatInterface getInstance() {
		if (instance == null)
			instance = new PT32Interface();

		return instance;
	}

	boolean	pendingRequests	= false;

	public void RequestStatusUpdate() {
		pendingRequests = true;

		RequestStatusUpdate();
	}

	public void SetHeatingMode(String mode) {
		// smsTransceiver.listenForMessage(this, number,
		// HEATING_SYSTEM_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number,
		// HEATING_ON_MESSAGE.replace("%d%", mode));

		startTimer();
	}

	public void SetHeatingTemperature(int degrees) {
		// smsTransceiver.listenForMessage(this, number,
		// HEATING_SYSTEM_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number,
		// HEATING_ON_MESSAGE.replace("%d%", degrees + ""));

		startTimer();
	}

	@Override
	public void messageReceived(String message) {
		Log.d(TAG, message);

		// if (message.contains(HEATING_SYSTEM_MESSAGE_CONTAINS)) {
		// String[] answer = message.split(" ");
		//
		// String value = answer[answer.length - 1];
		//
		// if (value.equals(HEATING_ON)) {
		// heatingMode = true;
		// heatingDegrees = Integer.parseInt(answer[answer.length - 3]);
		// } else {
		// heatingMode = false;
		// heatingDegrees = -1;
		// }
		//
		// settings.heatingOn = heatingMode;
		// settings.heatingDegrees = heatingDegrees;
		// } else {
		// Log.i(TAG, "WARNING: unknown message=" + message);
		// }

		if (isFullUpdateComplete()) {
			stopTimer();

			pendingRequests = false;
			settings.saveSettings();
		}

		if (!pendingRequests) {
			for (IPT32BoxListener listener : stateListeners) {
				if (listener != null)
					listener.onStateChanged();
			}
		}
	}

	boolean isFullUpdateComplete() {
		boolean completed = true;

		if (pendingRequests) {
			if (lastAnswer == null | lastAnswer.equals("")) {
				return false;
			}
		}
		return completed;
	}

	private final HashSet<IPT32BoxListener>	stateListeners	= new HashSet<IPT32BoxListener>();

	public void listenForStateChanges(IPT32BoxListener listener) {
		stateListeners.add(listener);
	}

	public void unlistenForStateChanges(IPT32BoxListener listener) {
		stateListeners.remove(listener);
	}

	void startTimer() {
		canceled = false;

		timer = new Timer();

		// wait xx secs and then cancel the pending update
		timer.schedule(new CancelTimeTask(), timeout);
	}

	class CancelTimeTask extends TimerTask {
		@Override
		public void run() {
			cancelPendingUpdate();
		}
	}

	void stopTimer() {
		if (timer != null)
			timer.cancel();
		timer.purge();
	}

	void cancelPendingUpdate() {
		if (!canceledTimer) {
			Looper.prepare();

			// onStateChanged();
			canceled = true;

			stopTimer();

			// Toast
			// .makeText(
			// settings.appContext,
			// "A1 MatikBox antwortet nicht! Bitte versuchen Sie es später noch einmal.",
			// Toast.LENGTH_LONG).show();

			for (IPT32BoxListener listener : stateListeners) {
				if (listener != null)
					listener.onStateChanged();
			}
		}
	}
}
