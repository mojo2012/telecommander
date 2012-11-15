package at.ftw.a1telecommander.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;
import at.ftw.a1telecommander.R;
import at.ftw.a1telecommander.matikbox.IMatikBoxInterface;
import at.ftw.a1telecommander.matikbox.IMatikBoxListener;
import at.ftw.a1telecommander.matikbox.MatikBoxInterface;
import at.ftw.a1telecommander.settings.A1TelecommanderSettings;
import at.ftw.a1telecommander.ui.util.ViewHelper;

public class DoorSystem extends Activity implements IMatikBoxListener {
	final static String TAG = "A1Telecommander/DoorSystem";

	Button openDoorButton = null;
	Button closeDoorButton = null;
	ToggleButton doorSystemState = null;

	IMatikBoxInterface matikBox = MatikBoxInterface.getInstance();

	A1TelecommanderSettings settings = A1TelecommanderSettings.getInstance();

	ProgressDialog progressDialog = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.doors_system);

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
		openDoorButton = (Button) findViewById(R.id.OpenDoorButton);
		closeDoorButton = (Button) findViewById(R.id.CloseDoorButton);
		doorSystemState = (ToggleButton) findViewById(R.id.DoorSystemState);

		ViewHelper.setBackgroundColor(openDoorButton, A1TelecommanderSettings.actionButtonBackgroundColor);
		ViewHelper.setBackgroundColor(closeDoorButton, A1TelecommanderSettings.actionButtonBackgroundColor);
		ViewHelper.setBackgroundColor(doorSystemState, A1TelecommanderSettings.statusButtonBackgroundColor);
		
		openDoorButton.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));
		closeDoorButton.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));
		doorSystemState.setTextColor(Color
				.parseColor(A1TelecommanderSettings.buttonForegroundColor));
	}

	public void initGuiWidgetEventMethods() {
		openDoorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				matikBox.SetDoorSystemState(true);
			}
		});

		closeDoorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				matikBox.SetDoorSystemState(false);
			}
		});
	}

	void showProgressDialog() {
		progressDialog = ProgressDialog.show(DoorSystem.this, "Bitte Warten",
				"Warte auf Antwort von A1 MatikBox...", true);

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
			String message = "Tür ist ";

			boolean doorStatus = matikBox.isDoorOpen();

			if (doorStatus)
				message += "offen.";
			else
				message += "geschlossen.";
			
			if (doorStatus) {
				doorSystemState.setTextOn(message);
			} else {
				doorSystemState.setTextOff(message);
			}

			doorSystemState.setChecked(doorStatus);

			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}
}
