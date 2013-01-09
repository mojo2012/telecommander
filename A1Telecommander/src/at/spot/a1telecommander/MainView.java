package at.spot.a1telecommander;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import at.spot.a1telecommander.pt32.IPT32BoxListener;
import at.spot.a1telecommander.pt32.IThermostatInterface.HeatingMode;
import at.spot.a1telecommander.pt32.PT32Interface;
import at.spot.a1telecommander.settings.A1TelecommanderSettings;
import at.spot.a1telecommander.ui.util.ViewHelper;

public class MainView extends Activity implements IPT32BoxListener {
	final static String		TAG							= "A1Telecommander/MainView";

	Button					heatingSetModeButton		= null;
	Button					heatingSetTemperatureButton	= null;
	Button					requestStatusUpdateButton	= null;

	ImageView				logo						= null;

	A1TelecommanderSettings	settings					= null;

	PT32Interface			pt32Interface				= null;

	ProgressDialog			loadingDialog				= null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_main);

		initGuiWidgets();
		initGuiWidgetEventMethods();

		settings = A1TelecommanderSettings.getInstance(getBaseContext());

		checkDefaultSettings();

		pt32Interface = PT32Interface.getInstance();
		pt32Interface.listenForStateChanges(this);
	}

	public void initGuiWidgets() {
		heatingSetModeButton = (Button) findViewById(R.id.HeatingSetMode);
		heatingSetTemperatureButton = (Button) findViewById(R.id.HeatingSetTemperature);
		requestStatusUpdateButton = (Button) findViewById(R.id.RequestStatusUpdateButton);

		logo = (ImageView) findViewById(R.id.Logo);
	}

	void startActivity(Class<? extends Activity> cls) {
		Intent i = new Intent(MainView.this, cls);
		startActivity(i);
	}

	public void initGuiWidgetEventMethods() {
		heatingSetModeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showHeatingModeDialog();
			}
		});

		requestStatusUpdateButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// startActivity(SystemStatus.class);
				return true;
			}
		});

		logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "http://www.spot-innovativecoding.com";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));

				startActivity(i);
			}
		});
	}

	private void showHeatingModeDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final ProgressDialog progressDialog = new ProgressDialog(this);

		progressDialog.setTitle("Stelle Heizung ein");
		progressDialog.setMessage("Bitte warten ...");

		loadingDialog = progressDialog;

		final String[] values = new String[HeatingMode.values().length];

		for (int x = 0; x < HeatingMode.values().length; x++) {
			if (HeatingMode.values()[x] != HeatingMode.Unknown)
				values[x] = HeatingMode.values()[x].toString();
		}

		builder.setTitle(R.string.set_heating_mode_text);
		builder.setItems(values, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				pt32Interface.SetHeatingMode(values[which]);
				progressDialog.show();
			}
		});

		builder.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu_main, menu);
		return true;
	}

	void checkDefaultSettings() {
		if (settings.telephoneNumber == null | settings.telephoneNumber.equals("")) {
			showEnterTelNumberDialog("+43");

			ViewHelper.showDialogBox(
					"A1 Telecommander",
					"Um dieses Programm verwenden zu können, müssen Sie zuerst die Telefonnummer des PG32 GST angeben (im folgenden Dialog möglich).",
					this);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.set_tel_number:
				showEnterTelNumberDialog(settings.telephoneNumber);
				break;
			case R.id.reset_settings:
				settings.resetSettings();
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
				break;
			case R.id.info_dialog:
				ViewHelper.showDialogBox(
						"A1 Telecommander",
						"(c) 2012 spOt - innovative coding\n" +
								"web: www.spOt-innovativecoding.com",
						this);
				break;
		}
		return true;
	}

	void showEnterTelNumberDialog(String defaultValue) {
		showInputDialog("A1 Telecommander", "Bitte Telefonnummer des PT32 GST festlegen!", defaultValue);
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

				settings.telephoneNumber = value;
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

	@Override
	public void onStateChanged(PT32State state, boolean success) {
		loadingDialog.dismiss();

		String msg = "";

		if (success) {
			if (state == PT32State.HeatingModeSet) {
				msg = "Der Heizungsmodus wurde erfolgreich eingestellt!";
			} else if (state == PT32State.TemperatureSet) {
				msg = "Die Temperatur wurde erfolgreich eingestellt!";
			}
		} else {
			msg = "Es ist ein Fehler aufgetreten (das Gerät antwortet nicht)";
		}

		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
}
