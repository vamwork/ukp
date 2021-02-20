package ukp;

public class NL2R extends OutDiscretModule {

    private static final byte REPEAT_COUNT = 3;

    public NL2R(byte netAddr) {
        super(netAddr);
        name = "NL2R";
        channelsCount = 2;
    }

    public boolean CheckLike(OuterModule outerModule) {
        boolean result = false;
        if (outerModule.name.equals(this.name)) {
            result = true;
        }
        return result;
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
}
