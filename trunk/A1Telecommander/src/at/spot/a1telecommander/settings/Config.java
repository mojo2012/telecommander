package at.spot.a1telecommander.settings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import at.spot.a1telecommander.pt32.IThermostatInterface.HeatingMode;

public class Config {
	final static String				TAG						= "A1Telecommander/Settings";

	private static Config			instance				= null;
	private static String			PREFS_NAME				= "A1Telecommander";

	public Context					appContext				= null;
	SharedPreferences				preferences				= null;

	// Settings
	public String					telephoneNumber			= "";

	private HeatingMode				heatingMode				= HeatingMode.Unknown;
	private float					heatingDegreesActual	= -1;
	private float					heatingDegreesRequired	= -1;
	private int						signalStrenth			= -1;
	private Date					lastUpdate				= null;

	private boolean					isHeatingOn				= false;

	final static SimpleDateFormat	sdf						= new SimpleDateFormat("dd.MM.yyyy HH:mm");

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
		editor.putFloat("temperature.actual", heatingDegreesActual);
		editor.putFloat("temperature.required", heatingDegreesRequired);
		editor.putInt("signalstrength", signalStrenth);
		editor.putString("heatingmode", heatingMode.toString());

		if (lastUpdate != null)
			editor.putString("lastupdate", sdf.format(lastUpdate));

		editor.putBoolean("heatingon", isHeatingOn);

		editor.commit();
	}

	public void loadSettings() {
		telephoneNumber = preferences.getString("telephoneNumber", telephoneNumber);
		heatingDegreesActual = preferences.getFloat("temperature.actual", heatingDegreesActual);
		heatingDegreesRequired = preferences.getFloat("temperature.required", heatingDegreesRequired);
		signalStrenth = preferences.getInt("signalstrength", signalStrenth);
		isHeatingOn = preferences.getBoolean("heatingon", false);

		try {
			String d = preferences.getString("lastupdate", null);

			if (d != null && !d.equals(""))
				lastUpdate = sdf.parse(d);
		} catch (ParseException e) {
		}

		heatingMode = HeatingMode.valueOf(preferences.getString("heatingmode", HeatingMode.Unknown.toString()));
	}

	public void resetSettings() {
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}

	public SharedPreferences getPreferences() {
		return this.preferences;
	}

	public void setPreferences(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	public String getTelephoneNumber() {
		return this.telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public HeatingMode getHeatingMode() {
		return this.heatingMode;
	}

	public void setHeatingMode(HeatingMode heatingMode) {
		this.heatingMode = heatingMode;
	}

	public float getHeatingDegreesActual() {
		return this.heatingDegreesActual;
	}

	public void setHeatingDegreesActual(float heatingDegreesActual) {
		this.heatingDegreesActual = heatingDegreesActual;
	}

	public float getHeatingDegreesRequired() {
		return this.heatingDegreesRequired;
	}

	public void setHeatingDegreesRequired(float heatingDegreesRequired) {
		this.heatingDegreesRequired = heatingDegreesRequired;
	}

	public int getSignalStrenth() {
		return this.signalStrenth;
	}

	public void setSignalStrenth(int signalStrenth) {
		this.signalStrenth = signalStrenth;
	}

	public Date getLastUpdate() {
		return this.lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public boolean isHeatingOn() {
		return this.isHeatingOn;
	}

	public void setHeatingOn(boolean isHeatingOn) {
		this.isHeatingOn = isHeatingOn;
	}

}
