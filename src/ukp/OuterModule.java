package ukp;

//класс-оболочка для всех внешниех модулей
public abstract class OuterModule {

    public static final int ANSWER_WAIT = 150;

    protected byte errCount; // количество ошибок связи

    public byte groupLikeNumber; // порядковый номер среди "себе подобных"

    public byte netAddr;

    public String name;

    public byte outerModuleType; // 1-DIN, 2-DOUT

    public static byte[] SendCommand(char prefix, byte netAddr, String body) {

        byte[] answer = null;
        // составим полный текст команды
        String addr = Integer.toHexString(netAddr).toUpperCase();
        if (addr.length() < 2) {
            addr = "0" + addr;
        }
        String command = prefix + addr + body + "\r";
        byte[] cmd = new byte[command.length()];
        for (int j = 0; j < cmd.length; j++) {
            cmd[j] = (byte) command.charAt(j);
        }
        // отправим команду
        try {
            answer = Controller.cp.Request(cmd, ANSWER_WAIT);
            if (answer != null) {
                if (answer.length < 1) {
                    answer = null;
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*OuterModuler[0001]" + e.toString());
        }
        return answer;
    }

    public abstract boolean CheckLike(OuterModule outerModule);

    public OuterModule() {
    }

    public OuterModule(byte netAddr) {
        errCount = 3; // для первого раза установим критическое количеество ошибок
        groupLikeNumber = 0;
        this.netAddr = netAddr;
    }
}
