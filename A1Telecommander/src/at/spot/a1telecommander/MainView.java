package at.spot.a1telecommander;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import at.spot.a1telecommander.pt32.PT32Interface;
import at.spot.a1telecommander.settings.A1TelecommanderSettings;
import at.spot.a1telecommander.ui.HeatingSystem;
import at.spot.a1telecommander.ui.SystemStatus;
import at.spot.a1telecommander.ui.util.ViewHelper;

public class MainView extends Activity {
	final static String		TAG							= "A1Telecommander/MainView";

	Button					alarmSystemButton			= null;
	Button					heatingSystemButton			= null;
	Button					requestStatusUpdateButton	= null;

	ImageView				logo						= null;

	A1TelecommanderSettings	settings					= null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_main);

		initGuiWidgets();
		initGuiWidgetEventMethods();

		settings = A1TelecommanderSettings.getInstance(getBaseContext());

		checkDefaultSettings();
	}

	public void initGuiWidgets() {
		heatingSystemButton = (Button) findViewById(R.id.HeatingSetMode);
		requestStatusUpdateButton = (Button) findViewById(R.id.RequestStatusUpdateButton);

		logo = (ImageView) findViewById(R.id.Logo);

		heatingSystemButton.setTextColor(Color.parseColor(A1TelecommanderSettings.buttonForegroundColor));
		requestStatusUpdateButton.setTextColor(Color.parseColor(A1TelecommanderSettings.buttonForegroundColor));

		ViewHelper.setBackgroundColor(heatingSystemButton, A1TelecommanderSettings.actionButtonBackgroundColor);
		ViewHelper.setBackgroundColor(requestStatusUpdateButton, A1TelecommanderSettings.statusButtonBackgroundColor);
	}

	void startActivity(Class cls) {
		Intent i = new Intent(MainView.this, cls);
		startActivity(i);
	}

	public void initGuiWidgetEventMethods() {
		heatingSystemButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				startActivity(HeatingSystem.class);
				return true;
			}
		});
		heatingSystemButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(HeatingSystem.class);
			}
		});

		requestStatusUpdateButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				startActivity(SystemStatus.class);
				return true;
			}
		});
		requestStatusUpdateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(SystemStatus.class);
			}
		});

		logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "http://www.ftw.at";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));

				startActivity(i);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu_main, menu);
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
			// TelephonyManager tMgr = (TelephonyManager)
			// getSystemService(Context.TELEPHONY_SERVICE);
			// settings.alarmTelNumbers[0] = tMgr.getLine1Number();
			// settings.saveSettings();

			ViewHelper
					.showDialogBox(
							"A1 Telecommander",
							"Um dieses Programm verwenden zu können, müssen Sie zuerst die Telefonnummer der A1 Matikbox angeben (im folgenden Dialog möglich)."
									+ "\n\n"
									+ "Zusätzlich sollten Sie eine Alarmnummer im Menü \"Alarmanlage\" eingeben, um informiert zu werden, wenn ein Alarm ausgelöst wird.",
							this);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.set_matikbox_tel_number:
				showEnterTelNumberDialog(settings.matikBoxTelephoneNumber);
				break;
			case R.id.reset_settings:
				settings.resetSettings();
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
				break;
			case R.id.info_dialog:
				ViewHelper.showDialogBox(
						"A1 Telecommander",
						"(c) 2011 FTW Forschungszentrum Telekommunikation Wien\n"
								+ "\n\n"
								+ "Autor:\nMatthias Fuchs\n(mfuchs@ftw.at)",
						this);
				break;
		}
		return true;
	}

	void showEnterTelNumberDialog(String defaultValue) {
		showInputDialog("A1 Telecommander", "Bitte Telefonnummer der MatikBox festlegen!", defaultValue);
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
