package at.ftw.a1telecommander;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import at.ftw.a1telecommander.matikbox.MatikBoxInterface;
import at.ftw.a1telecommander.settings.A1TelecommanderSettings;
import at.ftw.a1telecommander.ui.AlarmSystem;
import at.ftw.a1telecommander.ui.DoorSystem;
import at.ftw.a1telecommander.ui.FireAndGasAlarmSystem;
import at.ftw.a1telecommander.ui.HeatingSystem;
import at.ftw.a1telecommander.ui.SaunaSystem;
import at.ftw.a1telecommander.ui.SystemStatus;

public class MainView extends Activity {
	final static String TAG = "A1Telecommander/MainView";

	Button alarmSystemButton = null;
	Button fireAndGasAlarmSystemButton = null;
	Button doorSystemButton = null;
	Button heatingSystemButton = null;
	Button saunaSystemButton = null;
	Button requestStatusUpdateButton = null;

	A1TelecommanderSettings settings = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initGuiWidgets();
		initGuiWidgetEventMethods();

		settings = A1TelecommanderSettings.getInstance(getBaseContext());

		checkDefaultSettings();
	}

	public void initGuiWidgets() {
		alarmSystemButton = (Button) findViewById(R.id.AlarmSystemButton);
		fireAndGasAlarmSystemButton = (Button) findViewById(R.id.FireAndGasAlarmButton);
		doorSystemButton = (Button) findViewById(R.id.DoorSystemButton);
		heatingSystemButton = (Button) findViewById(R.id.HeatingButton);
		saunaSystemButton = (Button) findViewById(R.id.SaunaButton);
		requestStatusUpdateButton = (Button) findViewById(R.id.RequestStatusUpdateButton);
	}

	public void initGuiWidgetEventMethods() {
		alarmSystemButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent i = new Intent(MainView.this, AlarmSystem.class);
				startActivity(i);
				return false;
			}
		});

		fireAndGasAlarmSystemButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent i = new Intent(MainView.this,
						FireAndGasAlarmSystem.class);
				startActivity(i);
				return false;
			}
		});

		doorSystemButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent i = new Intent(MainView.this, DoorSystem.class);
				startActivity(i);
				return false;
			}
		});

		heatingSystemButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent i = new Intent(MainView.this, HeatingSystem.class);
				startActivity(i);
				return false;
			}
		});

		saunaSystemButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent i = new Intent(MainView.this, SaunaSystem.class);
				startActivity(i);
				return false;
			}
		});

		requestStatusUpdateButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent i = new Intent(MainView.this, SystemStatus.class);
				startActivity(i);
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.main_menu, menu);
		return true;
	}

	void checkDefaultSettings() {
		if (settings.matikBoxTelephoneNumber.equals("")) {
			showEnterTelNumberDialog("+43");
		}

		boolean atLeastOneNumber = false;

		for (String number : settings.alarmTelNumbers) {
			if (!number.equals("")) {
				atLeastOneNumber = true;
				break;
			}
		}

		if (!atLeastOneNumber) {
			TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			settings.alarmTelNumbers[0] = tMgr.getLine1Number();
			settings.saveSettings();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.set_matikbox_tel_number:
			showEnterTelNumberDialog(settings.matikBoxTelephoneNumber);
			break;
		case R.id.toggle_fake_mode:
			MatikBoxInterface.fakeMode = !MatikBoxInterface.fakeMode;

			if (MatikBoxInterface.fakeMode) {
				Toast.makeText(this, "Fake-Modus aktiviert", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(this, "Fake-Modus deaktiviert", Toast.LENGTH_LONG)
						.show();
			}

			break;
		case R.id.reset_settings:
			settings.resetSettings();
			finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		}
		return true;
	}

	void showEnterTelNumberDialog(String defaultValue) {
		showInputDialog("Achtung",
				"Bitte Telefonnummer der MatikBox festlegen!", defaultValue);
	}

	void showInputDialog(String title, String message, String defaultValue) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(title);
		alert.setMessage(message);

		// Set an EditText view to get user input
		final EditText input = new EditText(this);

		input.setText(defaultValue);
		input.setInputType(InputType.TYPE_CLASS_PHONE);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();

				settings.matikBoxTelephoneNumber = value;
				settings.saveSettings();
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Close the app?
					}
				});

		alert.show();
	}
}
