package at.ftw.a1telecommander.matikbox;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import at.ftw.a1telecommander.settings.A1TelecommanderSettings;
import at.ftw.a1telecommander.sms.ISmsMessageListener;
import at.ftw.a1telecommander.sms.SmsTransceiver;

public class FakeMatikBoxInterface implements ISmsMessageListener, IMatikBoxInterface {
	static FakeMatikBoxInterface instance = null;
	final static String TAG = "A1Telecommander/MatikBoxIntertface";

	final static String maticBoxAnswerPrefix = "A1 MatikBox: ";

	A1TelecommanderSettings settings = A1TelecommanderSettings.getInstance();
	// Alarm system messages
	static String ALARM_SYSTEM_MESSAGE_CONTAINS = "Alarmeingang";
	static String ALARM_ANSWER_ALARM_RUNNING = "Alarmeingang ist aktiv";
	static String ALARM_ANSWER_ALARM_NOT_RUNNING = "Alarmeingang ist passiv";
	static String ALARM_SYSTEM_SET_MESSAGE_CONTAINS = "Alarmfunktion";
	static String ALARM_ANSWER_ALARM_ACTIVATED = "Alarmfunktion ist aktiviert";
	static String ALARM_ANSWER_ALARM_DEACTIVATED = "Alarmfunktion ist deaktiviert";
	static String ALARM_SYSTEM_OFF_MESSAGE = "AA";
	static String ALARM_SYSTEM_ON_MESSAGE = "AE";
	static String ALARM_SYSTEM_SET_TEL_NUMBER_MESSAGE = "TA%telnumber%=%message%";
	static String ALARM_ANSWER_SET_TEL_NUMBER_MESSAGE_CONTAINS = "SMS-Alarmrufnummer wurde ge�ndert";

	// Fire alarm messages

	static String FIRE_AND_GAS_ALARM_ACTIVATED = "aktiviert";
	static String FIRE_AND_GAS_ALARM_DEACTIVATED = "deaktiviert";
	static String FIRE_AND_GAS_ALARM_ON = "zu";
	static String FIRE_AND_GAS_ALARM_OFF = "offen";

	static String FIRE_ALARM_ON_MESSAGE = "EBE";
	static String FIRE_ALARM_OFF_MESSAGE = "EBA";
	static String GAS_ALARM_ON_MESSAGE = "EAE";
	static String GAS_ALARM_OFF_MESSAGE = "EAA";

	static String FIRE_ALARM_ANSWER_MESSAGE_CONTAINS = "Eingang A ist";
	static String FIRE_ALARM_ANSWER = "Eingang A ist %v%, B ist %v%, C ist %v%";

	// Door and sauna messages
	static String DOOR_OPEN = "AUF";
	static String DOOR_CLOSED = "ZU";
	static String SAUNA_ON = "EIN";
	static String SAUNA_OFF = "AUS";

	static String DOOR_OPEN_2 = "eingeschaltet";
	static String SAUNA_ON_2 = "eingeschaltet";

	static String DOOR_AND_SAUNA_SYSTEM_SET_MESSAGE_CONTAINS = "Ausgang ";
	static String DOOR_AND_SAUNA_SYSTEM_MESSAGE_CONTAINS = "TOR ";
	static String DOOR_AND_SAUNA_SYSTEM_ANSWER = "TOR %dv%, SAUNA %sv%";

	static String DOOR_OPEN_MESSAGE = "RAE";
	static String DOOR_CLOSE_MESSAGE = "RAA";
	static String SAUNA_START_MESSAGE = "RBE";
	static String SAUNA_STOP_MESSAGE = "RBA";

	// Heating system messages
	static String HEATING_ON_MESSAGE = "HE%d%";
	static String HEATING_OFF_MESSAGE = "HA";
	static String HEATING_ON = "eingeschaltet";
	static String HEATING_OFF = "ausgeschaltet";
	static String HEATING_SYSTEM_MESSAGE_CONTAINS = "Heizungssteuerung ";
	static String HEATING_SYSTEM_ANSWER = "Heizungssteuerung ist %v%";

	// Frost watcher
	static String FROST_WATCHER_ON = "eingeschaltet";
	static String FROST_WATCHER_OFF = "ausgeschaltet";
	static String FROST_WATCHER_MESSAGE_CONTAINS = "Frostw�chter mit";
	static String FROST_WATCHER_SYSTEM_ANSWER = "Frostw�chter mit %d% Grad %v%";

	// Temperatur
	static String TEMPERATUR_MESSAGE_CONTAINS = "Die Raumtemperatur betr�gt";
	static String TEMPERATUR_SYSTEM_ANSWER = "Die Raumtemperatur betr�gt %v% Grad";

	// Requests
	static String DOOR_AND_SAUNA_SYSTEM_STATUS_REQUEST = "ABFRAGE";
	static String FIRE_ALARM_STATUS_REQUEST = "E?";
	static String HEATING_STATUS_REQUEST = "H?";
	static String ALARM_STATUS_REQUEST = "A?";
	static String FROST_WATCH_STATUS_REQUEST = "F?";
	static String TEMPERATUR_REQUEST = "T?";
	static String OUTLET_STATUS_REQUEST = "R?";

	SmsTransceiver smsTransceiver = null;
	String number = "";

	// boolean alarmEnabled = settings.alarmEnabled;
	boolean alarmRunning = false;

	// boolean fireAlarmEnabled = settings.fireAlarmEnabled;
	boolean fireAlarmRunning = false;
	// boolean gasAlarmEnabled = settings.gasAlarmEnabled;;
	boolean gasAlarmRunning = false;

	boolean doorOpen = false;
	boolean saunaRunning = false;

	boolean heatingOn = false;
	int heatingDegrees = -1;

	int frostWatchDegrees = -1;
	boolean frostWatchIsOn = false;

	int roomTemperature = -1;

	private String lastAlarmAnswer = "";
	private String lastFireAlarmAnswer = "";
	private String lastHeatingAnswer = "";
	private String lastDoorsAndSaunaAnswer = "";
	private String lastFrostWatcherAnswer = "";
	private String lastTemperaturAnswer = "";

	private final Boolean[] changedTelNumbers = { false, false, false, false };

	Timer timer = null;
	Timer fakeTimer = new Timer();

	boolean canceledTimer = false;
	int timeout = 300000;
	public boolean canceled = false;

	Handler handler = null;

	private FakeMatikBoxInterface() {
		number = settings.matikBoxTelephoneNumber;
		smsTransceiver = SmsTransceiver.getInstance();

		handler = new Handler() {
			public void handleMessage(Message msg) {
				messageReceived(msg.getData().getString("text"));
			}
		};
	}

	public static IMatikBoxInterface getInstance() {
		if (instance == null)
			instance = new FakeMatikBoxInterface();

		return instance;
	}

	boolean pendingRequests = false;

	public void RequestSystemStatusUpdate() {
		pendingRequests = true;

		fakeTimeout = 5000;

		RequestAlarmSystemStatusUpdate();
		RequestFireAndGasAlarmSystemStatusUpdate();
		RequestHeatingSystemStatusUpdate();
		RequestDoorAndSaunaSystemStatusUpdate();
		RequestTemperaturStatusUpdate();
		RequestFrostWatcherStatusUpdate();
	}

	public void SetAlarmSystemTelNumber(int telNumberIndex, String telNumber) {
		pendingRequests = true;

		changedTelNumbers[telNumberIndex - 1] = true;

		// smsTransceiver.listenForMessage(this, number,
		// ALARM_ANSWER_SET_TEL_NUMBER_MESSAGE_CONTAINS);
		//
		// String request = ALARM_SYSTEM_SET_TEL_NUMBER_MESSAGE.replace(
		// "%telnumber%", Integer.toString(telNumberIndex));
		// request = request.replace("%message%", telNumber);
		//
		// smsTransceiver.sendShortMessage(number, request);

		startFakeTimer("A1 MatikBox: SMS-Alarmrufnummer wurde ge�ndert ("
				+ telNumberIndex + ", " + telNumber + ")!");
		startTimer();
	}

	public void SetAlarmSystemState(boolean enabled) {
		// if (enabled) {
		// smsTransceiver.listenForMessage(this, number,
		// ALARM_SYSTEM_SET_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, ALARM_SYSTEM_ON_MESSAGE);
		// } else {
		// smsTransceiver.listenForMessage(this, number,
		// ALARM_SYSTEM_SET_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, ALARM_SYSTEM_OFF_MESSAGE);
		// }

		if (enabled)
			startFakeTimer("A1 MatikBox: Alarmfunktion ist aktiviert");
		else
			startFakeTimer("A1 MatikBox: Alarmfunktion ist deaktiviert");

		startTimer();
	}

	public void SetFireAlarmSystemState(boolean enabled) {
		// if (enabled) {
		// smsTransceiver.listenForMessage(this, number,
		// FIRE_ALARM_ANSWER_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, FIRE_ALARM_ON_MESSAGE);
		// } else {
		// smsTransceiver.listenForMessage(this, number,
		// FIRE_ALARM_ANSWER_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, FIRE_ALARM_OFF_MESSAGE);
		// }

		String gas = "deaktiviert";

		if (settings.gasAlarmEnabled)
			gas = "aktiviert";

		if (enabled)
			startFakeTimer("A1 MatikBox: Eingang A ist " + gas
					+ ", B ist aktiviert, C ist aktiviert");
		else
			startFakeTimer("A1 MatikBox: Eingang A ist " + gas
					+ ", B ist deaktiviert, C ist aktiviert");

		startTimer();
	}

	public void SetGasAlarmSystemState(boolean enabled) {
		// if (enabled) {
		// smsTransceiver.listenForMessage(this, number,
		// FIRE_ALARM_ANSWER_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, GAS_ALARM_ON_MESSAGE);
		// } else {
		// smsTransceiver.listenForMessage(this, number,
		// FIRE_ALARM_ANSWER_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, GAS_ALARM_OFF_MESSAGE);
		// }

		String fire = "deaktiviert";

		if (settings.fireAlarmEnabled)
			fire = "aktiviert";

		if (enabled)
			startFakeTimer("A1 MatikBox: Eingang A ist aktiviert, B ist "
					+ fire + ", C ist aktiviert");
		else
			startFakeTimer("A1 MatikBox: Eingang A ist deaktiviert, B ist "
					+ fire + ", C ist aktiviert");

		startTimer();
	}

	public void SetDoorSystemState(boolean opened) {
		// if (opened) {
		// smsTransceiver.listenForMessage(this, number,
		// DOOR_AND_SAUNA_SYSTEM_SET_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, DOOR_OPEN_MESSAGE);
		// } else {
		// smsTransceiver.listenForMessage(this, number,
		// DOOR_AND_SAUNA_SYSTEM_SET_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, DOOR_CLOSE_MESSAGE);
		// }

		String sauna = "ausgeschaltet";

		if (settings.saunaRunning)
			sauna = "eingeschaltet";

		if (opened)
			startFakeTimer("A1 MatikBox: Ausgang A ist eingeschaltet, B ist "
					+ sauna + ", C ist eingeschaltet");
		else
			startFakeTimer("A1 MatikBox: Ausgang A ist ausgeschaltet, B ist "
					+ sauna + ", C ist eingeschaltet");

		startTimer();
	}

	public void SetHeatingSystemState(boolean enabled, int degrees) {
		// if (enabled) {
		// smsTransceiver.listenForMessage(this, number,
		// HEATING_SYSTEM_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, HEATING_ON_MESSAGE.replace(
		// "%d%", Integer.toString(degrees)));
		// } else {
		// smsTransceiver.listenForMessage(this, number,
		// HEATING_SYSTEM_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, HEATING_OFF_MESSAGE);
		// }

		if (enabled)
			startFakeTimer("A1 MatikBox: Heizungssteuerung mit " + degrees
					+ " Grad eingeschaltet");
		else
			startFakeTimer("A1 MatikBox: Heizungssteuerung ist ausgeschaltet");

		startTimer();
	}

	public void SetSaunaSystemState(boolean enabled) {
		// if (enabled) {
		// smsTransceiver.listenForMessage(this, number,
		// DOOR_AND_SAUNA_SYSTEM_SET_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, SAUNA_START_MESSAGE);
		// } else {
		// smsTransceiver.listenForMessage(this, number,
		// DOOR_AND_SAUNA_SYSTEM_SET_MESSAGE_CONTAINS);
		// smsTransceiver.sendShortMessage(number, SAUNA_STOP_MESSAGE);
		// }

		String door = "ausgeschaltet";

		if (settings.doorOpened)
			door = "eingeschaltet";

		if (enabled)
			startFakeTimer("A1 MatikBox: Ausgang A ist " + door
					+ ", B ist eingeschaltet, C ist eingeschaltet");
		else
			startFakeTimer("A1 MatikBox: Eingang A ist " + door
					+ ", B ist ausgeschaltet, C ist eingeschaltet");

		startTimer();
	}

	public void RequestAlarmSystemStatusUpdate() {
		lastAlarmAnswer = "";

		// smsTransceiver.listenForMessage(this, number,
		// ALARM_SYSTEM_MESSAGE_CONTAINS);
		//
		// smsTransceiver.sendShortMessage(number, ALARM_STATUS_REQUEST);

		startFakeTimer("A1 MatikBox: Alarmeingang ist passiv");
		startTimer();
	}

	public void RequestFireAndGasAlarmSystemStatusUpdate() {
		lastFireAlarmAnswer = "";

		// smsTransceiver.listenForMessage(this, number,
		// FIRE_ALARM_ANSWER_MESSAGE_CONTAINS);
		//
		// smsTransceiver.sendShortMessage(number, FIRE_ALARM_STATUS_REQUEST);

		startFakeTimer("A1 MatikBox: Eingang A ist offen, B ist offen, C ist offen");
		startTimer();
	}

	public void RequestDoorAndSaunaSystemStatusUpdate() {
		lastDoorsAndSaunaAnswer = "";

		// smsTransceiver.listenForMessage(this, number,
		// DOOR_AND_SAUNA_SYSTEM_MESSAGE_CONTAINS);
		//
		// smsTransceiver.sendShortMessage(number,
		// DOOR_AND_SAUNA_SYSTEM_STATUS_REQUEST);

		String message = "A1 MatikBox: ";

		if (doorOpen)
			message += "TOR AUF, ";
		else
			message += "TOR ZU, ";

		if (saunaRunning)
			message += "SAUNA EIN, AUS";
		else
			message += "SAUNA AUS, AUS";

		startFakeTimer(message);
		startTimer();
	}

	public void RequestHeatingSystemStatusUpdate() {
		lastHeatingAnswer = "";

		// smsTransceiver.listenForMessage(this, number,
		// HEATING_SYSTEM_MESSAGE_CONTAINS);
		//
		// smsTransceiver.sendShortMessage(number, HEATING_STATUS_REQUEST);

		String message = "A1 MatikBox: Heizungssteuerung ";

		if (heatingOn)
			message += "mit " + settings.heatingDegrees + " Grad eingeschaltet";
		else
			message += "ist ausgeschaltet";

		startFakeTimer(message);
		startTimer();
	}

	public void RequestFrostWatcherStatusUpdate() {
		lastFireAlarmAnswer = "";
		// smsTransceiver.listenForMessage(this, number,
		// FROST_WATCHER_MESSAGE_CONTAINS);
		//
		// smsTransceiver.sendShortMessage(number, FROST_WATCH_STATUS_REQUEST);

		String message = "A1 MatikBox: Frostw�chter mit 8 Grad eingeschaltet";

		startFakeTimer(message);
		startTimer();
	}

	public void RequestTemperaturStatusUpdate() {
		lastTemperaturAnswer = "";

		// smsTransceiver.listenForMessage(this, number,
		// TEMPERATUR_MESSAGE_CONTAINS);
		//
		// smsTransceiver.sendShortMessage(number, TEMPERATUR_REQUEST);

		String message = "A1 MatikBox: Die Raumtemperatur betr�gt 22 Grad";

		startFakeTimer(message);
		startTimer();
	}

	public int roomTemperature() {
		return this.roomTemperature;
	}

	public boolean isFrostWatcherOn() {
		return settings.frostWatchIsOn;
	}

	public int frostWatcherDegrees() {
		return settings.frostWatchDegrees;
	}

	public boolean isSaunaRunning() {
		return settings.saunaRunning;
	}

	public boolean isDoorOpen() {
		return settings.doorOpened;
	}

	public boolean isHeatingOn() {
		return settings.heatingOn;
	}

	public int heatingDegrees() {
		return settings.heatingDegrees;
	}

	public boolean isFireAlarmRunning() {
		return this.fireAlarmRunning;
	}

	public boolean isFireAlarmEnabled() {
		return settings.fireAlarmEnabled;
	}

	public boolean isGasAlarmRunning() {
		return this.gasAlarmRunning;
	}

	public boolean isGasAlarmEnabled() {
		return settings.gasAlarmEnabled;
	}

	public boolean isAlarmEnabled() {
		return settings.alarmEnabled;
	}

	public boolean isAlarmRunning() {
		return this.alarmRunning;
	}

	@Override
	public void messageReceived(String message) {
		Log.d(TAG, message);

		message = message.replace(maticBoxAnswerPrefix, "");

		if (message.contains(ALARM_SYSTEM_SET_MESSAGE_CONTAINS)) {
			if (message.equals(ALARM_ANSWER_ALARM_ACTIVATED)) {
				settings.alarmEnabled = true;
			} else if (message.equals(ALARM_ANSWER_ALARM_DEACTIVATED)) {
				settings.alarmEnabled = false;
			}
		} else if (message.contains(ALARM_SYSTEM_MESSAGE_CONTAINS)) {
			if (message.equals(ALARM_ANSWER_ALARM_RUNNING)) {
				alarmRunning = true;
			} else if (message.equals(ALARM_ANSWER_ALARM_NOT_RUNNING)) {
				alarmRunning = false;
			}

			lastAlarmAnswer = message;
		} else if (message.contains(FIRE_ALARM_ANSWER_MESSAGE_CONTAINS)) {
			String[] answer = message.split(",");

			String gas = answer[0].split(" ")[3];
			String fire = answer[1].split(" ")[3];

			// gas alarm
			if (gas.equals(FIRE_AND_GAS_ALARM_ON)) {
				fireAlarmRunning = true;
			} else if (gas.equals(FIRE_AND_GAS_ALARM_OFF)) {
				fireAlarmRunning = false;
			} else if (gas.equals(FIRE_AND_GAS_ALARM_ACTIVATED)) {
				settings.gasAlarmEnabled = true;
			} else if (gas.equals(FIRE_AND_GAS_ALARM_DEACTIVATED)) {
				settings.gasAlarmEnabled = false;
			}

			// fire alarm
			if (fire.equals(FIRE_AND_GAS_ALARM_ON)) {
				fireAlarmRunning = true;
			} else if (fire.equals(FIRE_AND_GAS_ALARM_OFF)) {
				fireAlarmRunning = false;
			} else if (fire.equals(FIRE_AND_GAS_ALARM_ACTIVATED)) {
				settings.fireAlarmEnabled = true;
			} else if (fire.equals(FIRE_AND_GAS_ALARM_DEACTIVATED)) {
				settings.fireAlarmEnabled = false;
			}

			// for (byte b = 0; b < answer.length; b++) {
			// int start = answer[b].lastIndexOf(" ") + 1;
			// int end = answer[b].length();
			// String value = answer[b].substring(start, end);
			//
			// if (value.equals(FIRE_AND_GAS_ALARM_OFF))
			// fireAlarmRunning[b] = true;
			// else
			// fireAlarmRunning[b] = false;
			// }

			lastFireAlarmAnswer = message;
		} else if (message.contains(DOOR_AND_SAUNA_SYSTEM_SET_MESSAGE_CONTAINS)) {
			String[] answer = message.split(",");

			String door = answer[0].split(" ")[3];
			String sauna = answer[1].split(" ")[3];

			if (door.equals(DOOR_OPEN_2))
				doorOpen = true;
			else
				doorOpen = false;

			settings.doorOpened = doorOpen;

			if (sauna.equals(SAUNA_ON_2))
				saunaRunning = true;
			else
				saunaRunning = false;

			settings.saunaRunning = saunaRunning;

			lastDoorsAndSaunaAnswer = message;
		} else if (message.contains(DOOR_AND_SAUNA_SYSTEM_MESSAGE_CONTAINS)) {
			String[] answer = message.split(",");

			int index = answer[0].lastIndexOf(" ");
			String doorValue = answer[0].substring(index + 1, answer[0]
					.length());

			if (doorValue.equals(DOOR_OPEN)) {
				doorOpen = true;

			} else {
				doorOpen = false;
			}

			settings.doorOpened = doorOpen;

			index = answer[1].lastIndexOf(" ");
			String saunaValue = answer[1].substring(index + 1, answer[1]
					.length());

			if (saunaValue.equals(SAUNA_ON))
				saunaRunning = true;
			else
				saunaRunning = false;

			settings.saunaRunning = saunaRunning;

			lastDoorsAndSaunaAnswer = message;
		} else if (message.contains(HEATING_SYSTEM_MESSAGE_CONTAINS)) {
			String[] answer = message.split(" ");

			String value = answer[answer.length - 1];

			if (value.equals(HEATING_ON)) {
				heatingOn = true;
				heatingDegrees = Integer.parseInt(answer[answer.length - 3]);
			} else {
				heatingOn = false;
				heatingDegrees = -1;
			}

			settings.heatingOn = heatingOn;
			settings.heatingDegrees = heatingDegrees;

			lastHeatingAnswer = message;
		} else if (message.contains(FROST_WATCHER_MESSAGE_CONTAINS)) {
			String[] answer = message.split(" ");

			frostWatchDegrees = Integer.parseInt(answer[2]);
			String on = answer[4];

			if (on.equals(FROST_WATCHER_ON))
				frostWatchIsOn = true;
			else
				frostWatchIsOn = false;

			settings.frostWatchIsOn = frostWatchIsOn;
			settings.frostWatchDegrees = frostWatchDegrees;

			lastFrostWatcherAnswer = message;
		} else if (message.contains(TEMPERATUR_MESSAGE_CONTAINS)) {
			String[] answer = message.split(" ");

			roomTemperature = Integer.parseInt(answer[3]);

			lastTemperaturAnswer = message;
		} else if (message
				.contains(ALARM_ANSWER_SET_TEL_NUMBER_MESSAGE_CONTAINS)) {
			int start = message.indexOf("(") + 1;
			int end = message.indexOf(")");

			String[] answer = message.substring(start, end).split(",");

			int index = Integer.parseInt(answer[0]) - 1;

			changedTelNumbers[index] = false;

			settings.alarmTelNumbers[index] = answer[1];

			boolean allChanged = true;

			for (Boolean entry : changedTelNumbers) {
				if (entry)
					allChanged = false;
			}

			if (allChanged) {
				pendingRequests = false;
				settings.saveSettings();
			}
		}

		if (isFullUpdateComplete()) {
			stopTimer();

			pendingRequests = false;
			settings.saveSettings();
		}

		if (!pendingRequests) {
			for (IMatikBoxListener listener : stateListeners) {
				if (listener != null)
					listener.onStateChanged();
			}
		}
	}

	boolean isFullUpdateComplete() {
		boolean completed = true;

		if (pendingRequests) {
			if (lastAlarmAnswer == "")
				completed = false;
			if (lastDoorsAndSaunaAnswer == "")
				completed = false;
			if (lastFireAlarmAnswer == "")
				completed = false;
			if (lastFrostWatcherAnswer == "")
				completed = false;
			if (lastTemperaturAnswer == "")
				completed = false;
			if (lastHeatingAnswer == "")
				completed = false;
		}
		return completed;
	}

	private final ArrayList<IMatikBoxListener> stateListeners = new ArrayList<IMatikBoxListener>();

	public void listenForStateChanges(IMatikBoxListener listener) {
		stateListeners.add(listener);
	}
	
	public void unlistenForStateChanges(IMatikBoxListener listener) {
		stateListeners.remove(listener);
	}

	int fakeTimeout = 1000;

	void startFakeTimer(String fakeSmsText) {
		fakeTimer = new Timer();

		fakeTimeout += 1000;

		fakeTimer.schedule(new FakeTimeTask(fakeSmsText), fakeTimeout);
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

	class FakeTimeTask extends TimerTask {
		String fakeSmsText = "";

		public FakeTimeTask(String fakeSmsText) {
			this.fakeSmsText = fakeSmsText;
		}

		@Override
		public void run() {
			Looper.prepare();

			Message msg = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putString("text", fakeSmsText);
			msg.setData(b);

			handler.sendMessage(msg);
		}
	}

	void stopTimer() {
		timer.cancel();
		timer.purge();
	}

	void cancelPendingUpdate() {
		if (!canceledTimer) {
			Looper.prepare();

			// onStateChanged();
			canceled = true;

			stopTimer();

			Toast
					.makeText(
							settings.appContext,
							"A1 MatikBox antwortet nicht! Bitte versuchen Sie es sp�ter noch einmal.",
							Toast.LENGTH_LONG).show();

			for (IMatikBoxListener listener : stateListeners) {
				if (listener != null)
					listener.onStateChanged();
			}
		}
	}

}
