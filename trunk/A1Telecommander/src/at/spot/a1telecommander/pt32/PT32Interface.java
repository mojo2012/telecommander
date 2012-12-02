package at.spot.a1telecommander.pt32;

import java.util.HashSet;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Looper;
import android.util.Log;
import at.spot.a1telecommander.settings.A1TelecommanderSettings;
import at.spot.a1telecommander.sms.ISmsMessageListener;
import at.spot.a1telecommander.sms.SmsTransceiver;

public class PT32Interface implements ISmsMessageListener,
		IThermostatInterface {

	static PT32Interface	instance				= null;
	final static String		TAG						= "A1Telecommander/PT32Intertface";

	A1TelecommanderSettings	settings				= A1TelecommanderSettings.getInstance();
	SmsTransceiver			smsTransceiver			= null;

	int						signalStrength			= -1;
	boolean					isHeatingOn				= false;
	HeatingMode				heatingMode				= HeatingMode.Unknown;
	float					heatingActualDegrees	= -1;
	float					heatingRequiredDegrees	= -1;

	String					lastAnswer				= "";

	Timer					timer					= null;
	boolean					canceledTimer			= false;
	int						timeout					= 300000;
	public boolean			canceled				= false;

	private PT32Interface() {
		smsTransceiver = SmsTransceiver.getInstance();
	}

	public static synchronized PT32Interface getInstance() {
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
		smsTransceiver.listenForMessage(this, settings.telephoneNumber);
		smsTransceiver.sendShortMessage(settings.telephoneNumber, mode);

		startTimer();
	}

	public void SetHeatingTemperature(int degrees) {
		smsTransceiver.listenForMessage(this, settings.telephoneNumber);
		smsTransceiver.sendShortMessage(settings.telephoneNumber, "temp " + degrees);

		startTimer();
	}

	@Override
	public void messageReceived(String message) {
		Log.d(TAG, message);

		String[] parts = message.split(";");

		int x = 0;

		for (String p : parts) {
			String[] tmp = p.split(":");

			if (tmp.length == 1) {
				tmp = p.split(" ");
			}

			String key = "";
			String value = "";

			if (tmp.length == 2) {
				key = tmp[0];
				value = tmp[1];
			} else {
				key = "";
				value = tmp[0];
			}

			x++;

			try {
				switch (x) {
					case 0: // required temperature (float value)
						heatingRequiredDegrees = Float.parseFloat(value);
						break;
					case 1: // Actual temperature (float value)
						heatingActualDegrees = Float.parseFloat(value);
						break;
					case 2: // heating status (on, off)
						isHeatingOn = value.toLowerCase(Locale.GERMAN).equals("on");
						break;
					case 3: // heating mode
						heatingMode = HeatingMode.valueOf(value);
						break;
					case 4: // Signal strength (0=no signal; 1=weak; 5=good)
						signalStrength = Integer.parseInt(value);
						break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Log.i(TAG, "WARNING: could not parse message: message=" + message);
			}
		}

		lastAnswer = message;

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
