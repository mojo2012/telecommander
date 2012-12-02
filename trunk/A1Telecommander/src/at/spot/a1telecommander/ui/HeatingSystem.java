package at.spot.a1telecommander.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import at.spot.a1telecommander.pt32.IPT32BoxListener;
import at.spot.a1telecommander.pt32.IThermostatInterface;
import at.spot.a1telecommander.pt32.PT32Interface;
import at.spot.a1telecommander.settings.A1TelecommanderSettings;
import at.spot.a1telecommander.ui.util.ViewHelper;

public class HeatingSystem extends Activity implements IPT32BoxListener {
	final static String		TAG						= "A1Telecommander/HeatingSystem";

	final int				MAX_VALUE				= 45;
	final int				MIN_VALUE				= 10;

	Button					startHeatingButton		= null;
	Button					stopHeatingButton		= null;
	EditText				heatingDegreesText		= null;
	Button					decrementTemperature	= null;
	Button					incrementTemperature	= null;

	IThermostatInterface	matikBox				= PT32Interface.getInstance();

	A1TelecommanderSettings	settings				= A1TelecommanderSettings.getInstance();

	ProgressDialog			progressDialog			= null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.view_heating_system);

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
		// startHeatingButton = (Button) findViewById(R.id.StartHeatingButton);
		// stopHeatingButton = (Button) findViewById(R.id.StopHeatingButton);

		// decrementTemperature = (Button)
		// findViewById(R.id.DecremtTemperature);
		// incrementTemperature = (Button)
		// findViewById(R.id.IncrementTemperature);

		// heatingDegreesText = (EditText) findViewById(R.id.HeatingDegrees);

		int degrees = matikBox.heatingDegrees();

		if (degrees == -1)
			degrees = 21;

		heatingDegreesText.setText(Integer.toString(degrees));

		heatingDegreesText.setEnabled(false);

		ViewHelper.setBackgroundColor(startHeatingButton,
				A1TelecommanderSettings.actionButtonBackgroundColor);
		ViewHelper.setBackgroundColor(stopHeatingButton,
				A1TelecommanderSettings.actionButtonBackgroundColor);
		ViewHelper.setBackgroundColor(decrementTemperature,
				A1TelecommanderSettings.actionButtonBackgroundColor);
		ViewHelper.setBackgroundColor(incrementTemperature,
				A1TelecommanderSettings.actionButtonBackgroundColor);

		startHeatingButton.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));
		stopHeatingButton.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));
		decrementTemperature.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));
		incrementTemperature.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));
	}

	public void initGuiWidgetEventMethods() {
		startHeatingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				matikBox.SetHeatingSystemState(true, Integer
						.parseInt(heatingDegreesText.getText().toString()));
			}
		});

		stopHeatingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				matikBox.SetHeatingSystemState(false, Integer
						.parseInt(heatingDegreesText.getText().toString()));
			}
		});

		decrementTemperature.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Integer degress = Integer.parseInt(heatingDegreesText.getText()
						.toString());

				if (degress > MIN_VALUE) {
					degress -= 1;
				} else {
					degress = MIN_VALUE;

					Toast.makeText(getApplicationContext(),
							"Temperatur nucht weiter gesenkt werden!",
							Toast.LENGTH_LONG).show();
				}

				heatingDegreesText.setText(degress.toString());
			}
		});

		incrementTemperature.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Integer degress = Integer.parseInt(heatingDegreesText.getText()
						.toString());

				if (degress < MAX_VALUE) {
					degress += 1;
				} else {
					degress = MAX_VALUE;

					Toast.makeText(getApplicationContext(),
							"Temperatur nucht weiter erhöht werden!",
							Toast.LENGTH_LONG).show();
				}

				heatingDegreesText.setText(degress.toString());
			}
		});
	}

	void showProgressDialog() {
		progressDialog = ProgressDialog.show(HeatingSystem.this,
				"Bitte Warten", "Warte auf Antwort von A1 MatikBox...", true);

	}

	void getStatus() {
		matikBox.listenForStateChanges(this);
		matikBox.RequestFireAndGasAlarmSystemStatusUpdate();
	}

	@Override
	public void onStateChanged() {
		if (progressDialog != null)
			progressDialog.dismiss();

		if (!IThermostatInterface.canceled) {

			String message = "Heizung ist mit ";

			boolean heatingStatus = matikBox.isHeatingOn();

			if (heatingStatus) {
				int degrees = matikBox.heatingDegrees();
				message += Integer.toString(degrees) + " eingeschaltet.";
			} else {
				message += "ausgeschaltet.";
			}

			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}
}