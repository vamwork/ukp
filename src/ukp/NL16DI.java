package ukp;

public class NL16DI extends InDiscretModule {

    public NL16DI(byte netAddr) {
        super(netAddr);
        name = "NL16";
        channelsCount = 16;
    }

    public boolean CheckLike(OuterModule outerModule) {
        boolean result = false;
        if (outerModule.name.equals(this.name)) {
            result = true;
        }
        return result;
    }

    public byte[] GetChannelsState() {
        byte[] result = new byte[channelsCount];
        String commandBody = "6";

        //заранее переведём в неопределенное состояние
        for (int i = 0; i < result.length; i++) {
            result[i] = 2;
        }

        byte[] answer = OuterModule.SendCommand('$', netAddr, commandBody);
        errCount++;
        if (answer != null) {
            // проверим корректность ответа
            if (answer.length == 8) {
                if (answer[0] == 0x21) {
                    // расшифровка ответа
                    for (int i = 6; i > 2; i--) {
                        short b = Global.HexCharToByte((char) answer[i]);
                        for (int j = 0; j < 4; j++) {
                            result[16 - (i - 2) * 4 + j] = (byte) (((b & (short) (1 << j)) != 0) ? 1 : 0);
                        }
                    }
                    errCount = 0;
                }
            }
        }
        // если много ошибок, переведем каналы в "неопределенное" состояние
        if (errCount > 3) {
            for (int i = 0; i < result.length; i++) {
                result[i] = 2;
            }
        }
        return result;
    }
}
