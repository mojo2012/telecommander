package at.spot.a1telecommander.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import at.spot.a1telecommander.pt32.IThermostatInterface.HeatingMode;

public class Config {
	final static String		TAG				= "A1Telecommander/Settings";

	private static Config	instance		= null;
	private static String	PREFS_NAME		= "A1Telecommander";

	public Context			appContext		= null;
	SharedPreferences		preferences		= null;

	// Settings
	public String			telephoneNumber	= "";

	public HeatingMode		heatingMode		= HeatingMode.Unknown;
	public int				heatingDegrees	= -1;

	public String getTelephoneNumber() {
		return this.telephoneNumber;
	}

	public HeatingMode getHeatingMode() {
		return this.heatingMode;
	}

	public int getHeatingDegrees() {
		return this.heatingDegrees;
	}

	private Config(Context context) {
		appContext = context;

		preferences = appContext.getSharedPreferences(PREFS_NAME, 0);
		// saveSettings();
		loadSettings();
	}

	public static Config getInstance() {
		return instance;
	}

	public static Config getInstance(Context context) {
		if (instance == null)
			instance = new Config(context);

		return instance;
	}

	public void saveSettings() {
		Editor editor = preferences.edit();

		editor.putString("telephoneNumber", telephoneNumber);
		editor.commit();
	}

	public void loadSettings() {
		telephoneNumber = preferences.getString("telephoneNumber", telephoneNumber);
	}

	public void resetSettings() {
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}
}
