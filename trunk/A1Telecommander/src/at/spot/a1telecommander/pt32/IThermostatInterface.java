package at.spot.a1telecommander.pt32;

public interface IThermostatInterface {

	public void RequestStatusUpdate();

	public void SetHeatingMode(String mode);

	public void SetHeatingTemperature(int degrees);

	public void messageReceived(String message);

	public void listenForStateChanges(IPT32BoxListener listener);

	public void unlistenForStateChanges(IPT32BoxListener listener);

	public boolean	canceled	= false;

	public enum HeatingMode {
		Auto,
		Manu,
		Off,
		Unknown
	}
}