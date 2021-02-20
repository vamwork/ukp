package ukp;

public class NL8R extends OutDiscretModule {

	private static final byte REPEAT_COUNT = 3;

	public NL8R(byte netAddr) {
		super(netAddr);
		name = "NL8R";
		channelsCount = 8;
	}

	public boolean SetChannelState(int channel, int state) {
		// формируем команду
		String commandBody = "1" + Integer.toString(Global.ToDecBin((byte) channel)) + "0"
				+ Integer.toString(Global.ToDecBin((byte) state));

		byte[] answer = null;

		int repeator = REPEAT_COUNT;
		boolean result = false;

		do {
			answer = OuterModule.SendCommand('#', netAddr, commandBody);
			if (answer != null) {
				result = true;
			}
			repeator--;
		} while (repeator > 0 & !result);

		return result;
	}

	public boolean CheckLike(OuterModule outerModule) {
		boolean result = false;
		if (outerModule.name.equals(this.name)) {
			result = true;
		}
		return result;
	}

}
