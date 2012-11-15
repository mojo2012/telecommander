package at.spot.a1telecommander.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import at.spot.a1telecommander.R;
import at.spot.a1telecommander.settings.A1TelecommanderSettings;
import at.spot.a1telecommander.sms.SmsAlarm;
import at.spot.a1telecommander.ui.util.ViewHelper;

public class PowerOutageAlarmView extends Activity {
	final static String		TAG							= "A1Telecommander/AlarmView";

	Button					cancelRunningAlarmButton	= null;
	TextView				alarmInfoView				= null;

	A1TelecommanderSettings	settings					= null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.power_outage_info);

		initGuiWidgets();
		initGuiWidgetEventMethods();

		String text = alarmInfoView.getText().toString();

		SimpleDateFormat dayFormat = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");

		Date dateTime = Calendar.getInstance().getTime();

		text = text.replace("%d", dayFormat.format(dateTime));
		text = text.replace("%t", timeFormat.format(dateTime));

		alarmInfoView.setText(text);
	}

	public void initGuiWidgets() {
		alarmInfoView = (TextView) findViewById(R.id.alarmInfoView);
		cancelRunningAlarmButton = (Button) findViewById(R.id.cancelAlarmButton);

		ViewHelper.setBackgroundColor(cancelRunningAlarmButton, A1TelecommanderSettings.statusButtonBackgroundColor);
		cancelRunningAlarmButton.setTextColor(Color.parseColor(A1TelecommanderSettings.buttonForegroundColor));
	}

	public void initGuiWidgetEventMethods() {
		cancelRunningAlarmButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				SmsAlarm.cancelRunningAlarm();
				finish();
				return false;
			}
		});
	}
}
