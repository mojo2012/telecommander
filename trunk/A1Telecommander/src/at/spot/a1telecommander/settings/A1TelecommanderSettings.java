package at.spot.a1telecommander.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class A1TelecommanderSettings {
	final static String						TAG							= "A1Telecommander/A1TelecommanderSettings";

	private static A1TelecommanderSettings	instance					= null;
	private static String					PREFS_NAME					= "A1Telecommander";

	public Context							appContext					= null;

	SharedPreferences						preferences					= null;

	public static final String				actionButtonBackgroundColor	= "#808080";
	public static final String				statusButtonBackgroundColor	= "#D4A61A";
	public static final String				buttonForegroundColor		= "#DDDDDD";

	// settings
	public String							telephoneNumber				= "";

	public String[]							alarmTelNumbers				= { "", "", "", "" };

	public boolean							heatingOn					= false;
	public int								heatingDegrees				= 21;

	private A1TelecommanderSettings(Context context) {
		appContext = context;

		preferences = appContext.getSharedPreferences(PREFS_NAME, 0);
		// saveSettings();
		loadSettings();
	}

	public static A1TelecommanderSettings getInstance() {
		return instance;
	}

	public static A1TelecommanderSettings getInstance(Context context) {
		if (instance == null)
			instance = new A1TelecommanderSettings(context);

		return instance;
	}

	public void saveSettings() {
		Editor editor = preferences.edit();

		String numbers = "";

		for (String entry : alarmTelNumbers)
			if (entry == null)
				numbers += ";";
			else
				numbers += entry + ";";

		for (int b = alarmTelNumbers.length; b < 4; b++)
			numbers += " ;";

		editor.putString("alarmTelNumbers", numbers);

		editor.putString("telephoneNumber", telephoneNumber);

		editor.commit();
	}

	public void loadSettings() {
		telephoneNumber = preferences.getString(
				"telephoneNumber", telephoneNumber);
		String[] telNumbers = preferences.getString("alarmTelNumbers", "")
				.split(";");

		if (alarmTelNumbers != null)
			alarmTelNumbers = telNumbers;
	}

	public void resetSettings() {
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}
}
