package at.spot.a1telecommander.pt32;

public interface IThermostatInterface {

	public void RequestSystemStatusUpdate();

	public void SetAlarmSystemTelNumber(int telNumberIndex, String telNumber);

	public void SetAlarmSystemState(boolean enabled);

	public void SetFireAlarmSystemState(boolean enabled);

	public void SetGasAlarmSystemState(boolean enabled);

	public void SetDoorSystemState(boolean opened);

	public void SetHeatingSystemState(boolean enabled, int degrees);

	public void SetSaunaSystemState(boolean enabled);

	public void RequestAlarmSystemStatusUpdate();

	public void RequestFireAndGasAlarmSystemStatusUpdate();

	public void RequestDoorAndSaunaSystemStatusUpdate();

	public void RequestHeatingSystemStatusUpdate();

	public void RequestFrostWatcherStatusUpdate();

	public void RequestTemperaturStatusUpdate();

	public int roomTemperature();

	public boolean isFrostWatcherOn();

	public int frostWatcherDegrees();

	public boolean isSaunaRunning();

	public boolean isDoorOpen();

	public boolean isHeatingOn();

	public int heatingDegrees();

	public boolean isFireAlarmRunning();

	public boolean isFireAlarmEnabled();

	public boolean isGasAlarmRunning();

	public boolean isGasAlarmEnabled();

	public boolean isAlarmEnabled();

	public boolean isAlarmRunning();

	public void messageReceived(String message);
	
	public void listenForStateChanges(IPT32BoxListener listener);

	public void unlistenForStateChanges(IPT32BoxListener listener);
	
	public boolean canceled = false;
	
}