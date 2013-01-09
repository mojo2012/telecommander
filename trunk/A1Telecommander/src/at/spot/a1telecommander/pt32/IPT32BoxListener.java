package at.spot.a1telecommander.pt32;

public interface IPT32BoxListener {
	public enum PT32State {
		HeatingModeSet,
		TemperatureSet,
		Idle,
	}

	void onStateChanged(PT32State state, boolean success);
}
