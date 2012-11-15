package at.spot.a1telecommander.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.spot.a1telecommander.R;
import at.spot.a1telecommander.matikbox.IMatikBoxInterface;
import at.spot.a1telecommander.matikbox.IMatikBoxListener;
import at.spot.a1telecommander.matikbox.MatikBoxInterface;
import at.spot.a1telecommander.settings.A1TelecommanderSettings;
import at.spot.a1telecommander.ui.util.ViewHelper;

public class SystemStatus extends Activity implements IMatikBoxListener {
	final static String	TAG							= "A1Telecommander/SystemStatus";

	Button				requestStatusUpdateButton	= null;
	TextView			additionalStatusText		= null;

	ToggleButton		alarmSystemState			= null;
	ToggleButton		heatingState				= null;

	IMatikBoxInterface	matikBox					= MatikBoxInterface.getInstance();

	ProgressDialog		progressDialog				= null;

	boolean[]			requestSuccess				= { false, false };
	int					requestIndex				= 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.matixbox_status);

		initGuiWidgets();
		initGuiWidgetEventMethods();

		matikBox.listenForStateChanges(this);
	}

	@Override
	protected void onPause() {
		matikBox.unlistenForStateChanges(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		matikBox.listenForStateChanges(this);
		super.onResume();
	}

	@Override
	protected void onStop() {
		matikBox.unlistenForStateChanges(this);
		super.onStop();
	}

	public void initGuiWidgets() {
		requestStatusUpdateButton = (Button) findViewById(R.id.RequestStatusUpdateButton);

		alarmSystemState = (ToggleButton) findViewById(R.id.AlarmSystemState);
		heatingState = (ToggleButton) findViewById(R.id.HeatingState);

		ViewHelper.setBackgroundColor(requestStatusUpdateButton,
				A1TelecommanderSettings.actionButtonBackgroundColor);
		ViewHelper.setBackgroundColor(alarmSystemState,
				A1TelecommanderSettings.statusButtonBackgroundColor);
		ViewHelper.setBackgroundColor(heatingState,
				A1TelecommanderSettings.statusButtonBackgroundColor);

		requestStatusUpdateButton.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));

		alarmSystemState.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));
		heatingState.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));

		additionalStatusText = (TextView) findViewById(R.id.AdditionalStatusInfo);
	}

	public void initGuiWidgetEventMethods() {
		requestStatusUpdateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();

				getStatus();
			}
		});
	}

	void showProgressDialog() {
		progressDialog = ProgressDialog.show(SystemStatus.this, "Bitte Warten",
				"Warte auf Antwort von A1 MatikBox...", true);
	}

	void getStatus() {
		// matikBox.listenForStateChanges(this);

		matikBox.RequestAlarmSystemStatusUpdate();
	}

	@Override
	public void onStateChanged() {
		if (IMatikBoxInterface.canceled) {
			String unknownText = "Status unbekannt";

			switch (requestIndex) {
				case 0:
					alarmSystemState.setText("Alarmanlage: " + unknownText);
					alarmSystemState.setChecked(false);
					break;
				case 1:
					heatingState.setText("Heizung: " + unknownText);
					heatingState.setChecked(false);
					break;
				case 2:
					// stromausfall
					break;
			}

			Log.v(TAG, "Status request failed!");
		} else {
			if (requestIndex == 0) {
				if (matikBox.isAlarmEnabled()) {
					alarmSystemState.setTextOn("Alarmanlage: Eingeschaltet");
					alarmSystemState.setChecked(true);
				} else {
					alarmSystemState.setTextOff("Alarmanlage: Ausgeschaltet");
					alarmSystemState.setChecked(false);
				}

				Log.v(TAG, "Received alarm system state!");
				matikBox.RequestHeatingSystemStatusUpdate();
			}

			if (requestIndex == 1) {
				if (matikBox.isHeatingOn()) {
					heatingState.setTextOn("Heizung: Eingeschaltet mit " + matikBox.heatingDegrees() + "° C");
					heatingState.setChecked(true);
				} else {
					heatingState.setTextOff("Heizung: Ausgeschaltet");
					heatingState.setChecked(false);
				}

				Log.v(TAG, "Received heating system state!");
				// matikBox.RequestDoorAndSaunaSystemStatusUpdate();
				// request stromausfall status
			}

			// if (requestIndex == 2) {
			// if (matikBox.isDoorOpen()) {
			// doorSystemState.setTextOn("Offen");
			// doorSystemState.setChecked(true);
			// } else {
			// doorSystemState.setTextOff("Geschlossen");
			// doorSystemState.setChecked(false);
			// }
			//
			// if (matikBox.isSaunaRunning()) {
			// saunaSystemState.setTextOn("Läuft");
			// saunaSystemState.setChecked(true);
			//
			// } else {
			// saunaSystemState.setTextOff("Läuft nicht");
			// saunaSystemState.setChecked(false);
			// }
			//
			// Log.v(TAG, "Received door and sauna system state!");
			// }
		}

		requestSuccess[requestIndex] = true;
		requestIndex++;

		if (requestIndex == 2) {
			if (progressDialog != null)
				progressDialog.dismiss();

			for (boolean b : requestSuccess) {
				if (!b) {
					additionalStatusText
							.setText("ACHTUNG\nNicht alle Einstellungen konnten abgefragt werden! Die unten angegebenen Werte sind eventuell nicht alle aktuell!\n\n");
					break;
				}
			}

			Log.v(TAG, "System status update was successful!");

			requestIndex = 0;
		}
	}
}