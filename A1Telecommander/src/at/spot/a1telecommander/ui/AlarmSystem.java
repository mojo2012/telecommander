package at.spot.a1telecommander.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;
import at.spot.a1telecommander.R;
import at.spot.a1telecommander.matikbox.IMatikBoxInterface;
import at.spot.a1telecommander.matikbox.IMatikBoxListener;
import at.spot.a1telecommander.matikbox.MatikBoxInterface;
import at.spot.a1telecommander.settings.A1TelecommanderSettings;
import at.spot.a1telecommander.ui.util.ViewHelper;

public class AlarmSystem extends Activity implements IMatikBoxListener {
	final static String		TAG					= "A1Telecommander/AlarmSystem";

	Button					enableAlarmButton	= null;
	Button					disableAlarmButton	= null;
	ToggleButton			alarmSystemState	= null;

	IMatikBoxInterface		matikBox			= MatikBoxInterface.getInstance();

	A1TelecommanderSettings	settings			= A1TelecommanderSettings.getInstance();

	ProgressDialog			progressDialog		= null;

	boolean					alarmSet			= false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_system);

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
		enableAlarmButton = (Button) findViewById(R.id.EnableAlarmButton);
		disableAlarmButton = (Button) findViewById(R.id.DisableAlarmButton);
		alarmSystemState = (ToggleButton) findViewById(R.id.AlarmSystemState);

		ViewHelper.setBackgroundColor(enableAlarmButton, A1TelecommanderSettings.actionButtonBackgroundColor);
		ViewHelper.setBackgroundColor(disableAlarmButton, A1TelecommanderSettings.actionButtonBackgroundColor);
		ViewHelper.setBackgroundColor(alarmSystemState, A1TelecommanderSettings.statusButtonBackgroundColor);

		enableAlarmButton.setTextColor(Color.parseColor(A1TelecommanderSettings.buttonForegroundColor));
		disableAlarmButton.setTextColor(Color.parseColor(A1TelecommanderSettings.buttonForegroundColor));
		alarmSystemState.setTextColor(Color.parseColor(A1TelecommanderSettings.buttonForegroundColor));
	}

	public void initGuiWidgetEventMethods() {
		enableAlarmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setAlarm(true);
			}
		});

		disableAlarmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setAlarm(false);
			}
		});
	}

	void setAlarm(boolean state) {
		showProgressDialog();
		alarmSet = true;
		matikBox.SetAlarmSystemState(state);
	}

	void showProgressDialog() {
		progressDialog = ProgressDialog.show(AlarmSystem.this, "Bitte Warten",
				"Warte auf Antwort von A1 MatikBox...", true);

	}

	void getStatus() {
		matikBox.listenForStateChanges(this);
		matikBox.RequestAlarmSystemStatusUpdate();
	}

	@Override
	public void onStateChanged() {
		if (progressDialog != null)
			progressDialog.dismiss();

		if (!IMatikBoxInterface.canceled) {
			if (alarmSet) {
				alarmSet = false;
				boolean enabled = matikBox.isAlarmEnabled();
				boolean running = matikBox.isAlarmRunning();

				String enabledText = "Ausgeschaltet";
				String runningText = "Läuft";

				if (enabled)
					enabledText = "Eingeschaltet";
				if (!running)
					runningText = runningText + " nicht";

				if (!enabled) {
					alarmSystemState.setTextOff(enabledText);
				} else {
					alarmSystemState.setTextOn(enabledText);
				}

				alarmSystemState.setChecked(enabled);

				// String message = "Alarm ist " + enabledText + " und " +
				// runningText + ".";
				// Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			}
		} else {
			alarmSystemState.setTextOff("Status unbekannt");
			alarmSystemState.setChecked(false);
		}
	}
}