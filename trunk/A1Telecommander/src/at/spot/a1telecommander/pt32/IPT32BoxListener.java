package at.spot.a1telecommander.pt32;

public interface IPT32BoxListener {
	public enum PT32TransactionMode {
		SetHeating,
		SetTemperature,
		Idle,
	}

	public enum PT32TransactionErrorReason {
		Timeout,
		CommandNotAccepted
	}

	void onStateChanged(PT32TransactionMode state, boolean success, PT32TransactionErrorReason errorReason);
}
