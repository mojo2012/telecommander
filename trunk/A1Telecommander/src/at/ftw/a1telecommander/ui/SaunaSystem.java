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

public class SaunaSystem extends Activity implements IMatikBoxListener {
	final static String TAG = "A1Telecommander/SaunaSystem";

	Button startSaunaButton = null;
	Button stopSaunaButton = null;

	IMatikBoxInterface matikBox = MatikBoxInterface.getInstance();

	A1TelecommanderSettings settings = A1TelecommanderSettings.getInstance();

	ProgressDialog progressDialog = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sauna_system);

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
		startSaunaButton = (Button) findViewById(R.id.StartSaunaButton);
		stopSaunaButton = (Button) findViewById(R.id.StopSaunaButton);
	}

	public void initGuiWidgetEventMethods() {
		startSaunaButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				matikBox.SetSaunaSystemState(true);
			}
		});

		stopSaunaButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				matikBox.SetSaunaSystemState(false);
			}
		});
	}

	void showProgressDialog() {
		progressDialog = ProgressDialog.show(SaunaSystem.this, "Bitte Warten",
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
			String message = "Sauna ist ";

			boolean doorStatus = matikBox.isSaunaRunning();

			if (doorStatus)
				message += "eingeschaltet.";
			else
				message += "ausgeschaltet.";

			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}
}