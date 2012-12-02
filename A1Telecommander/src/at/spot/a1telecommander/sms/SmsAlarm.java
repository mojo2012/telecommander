package at.spot.a1telecommander.sms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.widget.Toast;
import at.spot.a1telecommander.MainView;
import at.spot.a1telecommander.R;

public class SmsAlarm {
	static final String			TAG						= "A1Telecommander/SmsAlarm";

	static MediaPlayer			mMediaPlayer			= null;
	static NotificationManager	mNotificationManager	= null;
	static Vibrator				vibrator				= null;
	static Context				mContext				= null;

	static void showNotificationMessage(String message) {
		Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
	}

	static void playAlarmNoise() {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		mMediaPlayer = new MediaPlayer();

		try {
			mMediaPlayer.setDataSource(mContext, alert);

			final AudioManager audioManager = (AudioManager) mContext
					.getSystemService(Context.AUDIO_SERVICE);

			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.setLooping(true);
				mMediaPlayer.prepare();
				mMediaPlayer.start();
			}
		} catch (Exception e) {
		}
	}

	public static void cancelRunningAlarm() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
		}

		if (mNotificationManager != null) {
			mNotificationManager.cancel(-1);
		}

		if (vibrator != null) {
			vibrator.cancel();
		}

	}

	static void showNotificationIcon(String text) {
		mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(R.drawable.control,
														"A1 Matikbox Alarm", System.currentTimeMillis());

		CharSequence contentTitle = "A1 Matikbox";

		CharSequence contentText = "Alarm ausgelöst!";

		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;

		Intent notificationIntent = new Intent(mContext, MainView.class);

		// This is who should be launched if the user selects our notification.
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(mContext, contentTitle, contentText,
				contentIntent);

		mNotificationManager.notify(-1, notification);
	}

	static void vibrate() {
		vibrator = (Vibrator) mContext
				.getSystemService(Context.VIBRATOR_SERVICE);

		// 1. Vibrate for 1000 milliseconds
		long milliseconds = 30000;
		vibrator.vibrate(milliseconds);

		// 2. Vibrate in a Pattern with 500ms on, 500ms off for 5 times
		// long[] pattern = { 500, 300 };
		// vibrator.vibrate(pattern, 5000);
	}
}
