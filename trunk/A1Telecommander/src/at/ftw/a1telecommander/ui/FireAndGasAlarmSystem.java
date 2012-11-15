package at.ftw.a1telecommander.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import at.ftw.a1telecommander.R;
import at.ftw.a1telecommander.matikbox.IMatikBoxInterface;
import at.ftw.a1telecommander.matikbox.IMatikBoxListener;
import at.ftw.a1telecommander.matikbox.MatikBoxInterface;
import at.ftw.a1telecommander.settings.A1TelecommanderSettings;

public class FireAndGasAlarmSystem extends Activity implements
		IMatikBoxListener {
	final static String TAG = "A1Telecommander/FireAndGasAlarmSystem";

	Button enableFireAlarmButton = null;
	Button disableFireAlarmButton = null;
	Button enableGasAlarmButton = null;
	Button disableGasAlarmButton = null;

	IMatikBoxInterface matikBox = MatikBoxInterface.getInstance();

	A1TelecommanderSettings settings = A1TelecommanderSettings.getInstance();

	ProgressDialog progressDialog = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fire_and_gas_alarm_system);

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
		enableFireAlarmButton = (Button) findViewById(R.id.EnableFireAlarmButton);
		enableGasAlarmButton = (Button) findViewById(R.id.EnableGasAlarmButton);
		disableFireAlarmButton = (Button) findViewById(R.id.DisableFireAlarmButton);
		disableGasAlarmButton = (Button) findViewById(R.id.DisableGasAlarmButton);
	}

	public void initGuiWidgetEventMethods() {
		enableFireAlarmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				matikBox.SetFireAlarmSystemState(true);
			}
		});

		enableGasAlarmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				matikBox.SetGasAlarmSystemState(true);
			}
		});

		disableFireAlarmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				matikBox.SetFireAlarmSystemState(false);
			}
		});

		disableGasAlarmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				matikBox.SetGasAlarmSystemState(false);
			}
		});
	}

	void showProgressDialog() {
		progressDialog = ProgressDialog.show(FireAndGasAlarmSystem.this,
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

		if (!matikBox.canceled) {
			String message = "";

			boolean gasAlarmEnabled = matikBox.isGasAlarmEnabled();
			boolean fireAlarmEnabled = matikBox.isFireAlarmEnabled();
			boolean gasAlarmRunning = matikBox.isGasAlarmRunning();
			boolean fireAlarmRunning = matikBox.isFireAlarmRunning();

			String enabledText = "ausgeschaltet";
			String runningText = "läuft";

			if (fireAlarmEnabled)
				enabledText = "eingeschaltet";
			if (!fireAlarmRunning)
				runningText = runningText + " nicht";

			message = "Feueralarm ist " + enabledText + " und " + runningText
					+ ".\n";

			enabledText = "ausgeschaltet";
			runningText = "läuft";

			if (gasAlarmEnabled)
				enabledText = "eingeschaltet";
			if (!gasAlarmRunning)
				runningText = runningText + " nicht";

			message += "Gasalarm ist " + enabledText + " und " + runningText
					+ ".";

			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}
}