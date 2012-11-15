package at.spot.a1telecommander.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import at.spot.a1telecommander.R;
import at.spot.a1telecommander.matikbox.IMatikBoxInterface;
import at.spot.a1telecommander.matikbox.IMatikBoxListener;
import at.spot.a1telecommander.matikbox.MatikBoxInterface;
import at.spot.a1telecommander.settings.A1TelecommanderSettings;
import at.spot.a1telecommander.ui.util.ViewHelper;

public class AlarmSystemSettings extends Activity implements IMatikBoxListener {
	final static String		TAG					= "A1Telecommander/AlarmSystem";

	Button					saveTelNumbers		= null;

	EditText[]				alarmTelNoEditTexts	= null;

	IMatikBoxInterface		matikBox			= MatikBoxInterface.getInstance();

	A1TelecommanderSettings	settings			= A1TelecommanderSettings.getInstance();

	ProgressDialog			progressDialog		= null;

	Boolean[]				editTextChanged		= { false, false, false, false };

	boolean					alarmSet			= false;
	boolean					telNumbersChanged	= false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_system_settings);

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
	protected void onStop() {
		matikBox.unlistenForStateChanges(this);
		super.onStop();
	}

	@Override
	protected void onResume() {
		matikBox.listenForStateChanges(this);
		super.onResume();
	}

	public void initGuiWidgets() {
		saveTelNumbers = (Button) findViewById(R.id.SaveAlarmSettings);

		alarmTelNoEditTexts = new EditText[4];

		alarmTelNoEditTexts[0] = (EditText) findViewById(R.id.AlarmTelNumber1);
		alarmTelNoEditTexts[1] = (EditText) findViewById(R.id.AlarmTelNumber2);
		alarmTelNoEditTexts[2] = (EditText) findViewById(R.id.AlarmTelNumber3);
		alarmTelNoEditTexts[3] = (EditText) findViewById(R.id.AlarmTelNumber4);

		ViewHelper.setBackgroundColor(saveTelNumbers, "#606060");
		saveTelNumbers.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));

		// ViewHelper.setBackgroundColor(alarmTelNoEditTexts[0], "#606060");
		// ViewHelper.setBackgroundColor(alarmTelNoEditTexts[1], "#606060");
		// ViewHelper.setBackgroundColor(alarmTelNoEditTexts[2], "#606060");
		// ViewHelper.setBackgroundColor(alarmTelNoEditTexts[3], "#606060");

		for (int i = 0; i < settings.alarmTelNumbers.length; i++) {
			EditText editText = alarmTelNoEditTexts[i];
			try {
				editText.setText(settings.alarmTelNumbers[i].trim());
			} catch (Exception e) {
			}
		}
	}

	public void initGuiWidgetEventMethods() {
		saveTelNumbers.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();

				telNumbersChanged = true;

				boolean appliedSomething = false;

				for (int i = 0; i < editTextChanged.length; i++) {
					if (editTextChanged[i]) {
						appliedSomething = true;
						matikBox.SetAlarmSystemTelNumber(i + 1,
								alarmTelNoEditTexts[i].getText().toString()
										.trim());
						editTextChanged[i] = false;
					}
				}

				if (!appliedSomething) {
					nothingToSave();
				}
			}
		});

		for (int i = 0; i < alarmTelNoEditTexts.length; i++) {
			EditText editText = alarmTelNoEditTexts[i];

			final int index = i;

			editText.addTextChangedListener(new TextWatcher() {
				int	editTextIndex	= index;

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					editTextChanged[editTextIndex] = true;
				}

				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}
			});
		}
	}

	void nothingToSave() {
		progressDialog.dismiss();
		Toast.makeText(this, "Die Telefonnummern wurden nicht verändert!",
				Toast.LENGTH_LONG).show();
	}

	void showProgressDialog() {
		progressDialog = ProgressDialog.show(AlarmSystemSettings.this,
				"Bitte Warten", "Warte auf Antwort von A1 MatikBox...", true);

	}

	void getStatus() {
		matikBox.listenForStateChanges(this);
		matikBox.RequestAlarmSystemStatusUpdate();
	}

	@Override
	public void onStateChanged() {
		if (progressDialog != null)
			progressDialog.dismiss();

		if (!matikBox.canceled) {

			String message = "";

			if (telNumbersChanged) {
				message = "Telefonnummern gespeichert.";
				telNumbersChanged = false;
				Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			}
		}
	}
}