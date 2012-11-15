package at.spot.a1telecommander.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
	final static String TAG = "A1Telecommander/SmsReceiver";
	static final int MSG_DELETE_SMS = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		// ---get the SMS message passed in---
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;

		if (bundle != null) {
			// ---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];

			for (int i = 0; i < msgs.length; i++) {
				SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdus[i]);

				Object[] handlerArgs = new Object[2];
				handlerArgs[0]= msg;
				handlerArgs[1]= context;
				handler.sendMessageDelayed(handler.obtainMessage(MSG_DELETE_SMS, handlerArgs), 2500);

				if (!SmsAlarm.checkIfIsAlarmSms(msg, context)) {
					SmsTransceiver.getInstance().onReceiveSms(msg);
				}

			}
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DELETE_SMS:
				Object[] args = (Object[])msg.obj;
				SmsMessage message =(SmsMessage) args[0];
				Context context =(Context) args[1];
				
				Log.v(TAG, "Deleting received message.");
				
				deleteSms(message, context);
				
				break;

			}
		}
	};

	private void deleteSms(SmsMessage message, Context context) {
		Uri deleteUri = Uri.parse("content://sms");
		SmsMessage msg = (SmsMessage) message;

		context.getContentResolver().delete(
				deleteUri,
				"address=? and date=?",
				new String[] { msg.getOriginatingAddress(),
						String.valueOf(msg.getTimestampMillis()) });
	}
}
