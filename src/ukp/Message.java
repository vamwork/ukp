package ukp;

//класс, инкапсулирующий "сообщение" для web-кэша
public class Message {

    public static final int MAX_TRYCOUNTER = 25;

    public byte[] messageBody;

    public byte tryCounter;

    public int waitCounter;

    public ISetAsSend ISet = null;

    // конструктор
    public Message(int sdNetAddr, int dType, int dd, int mm, int yy, int hh, int mi, int ss, byte[] body) {

        messageBody = new byte[body.length + 11];

        messageBody[0] = (byte) messageBody.length;
        messageBody[1] = Controller.netAddr;
        messageBody[2] = Controller.extendedNetAddr;
        messageBody[3] = (byte) sdNetAddr;
        messageBody[4] = (byte) dType;
        messageBody[5] = (byte) dd;
        messageBody[6] = (byte) mm;
        messageBody[7] = (byte) (yy - 2000);
        messageBody[8] = (byte) hh;
        messageBody[9] = (byte) mi;
        messageBody[10] = (byte) ss;

        System.arraycopy(body, 0, messageBody, 11, body.length);

        tryCounter = MAX_TRYCOUNTER;
        SetWaitCounter();
    }

    // стравнить два Message
    public boolean EqaualsMessage(Message msg) {
        boolean result = true;

        if (messageBody.length != msg.messageBody.length) {
            result = false;
        } else {
            for (int i = 1; i < messageBody.length; i++) {
                if (messageBody[i] != msg.messageBody[i]) {
                    result = false;
                }
            }
        }
        return result;
    }

    // установить новое значение счётчика задержки
    private void SetWaitCounter() {
        waitCounter = (MAX_TRYCOUNTER - tryCounter) * 1;
    }

    // уменьшить количство попыток отправки
    public int DecTryCounter() {
        if (tryCounter > 0) {
            tryCounter--;
            SetWaitCounter();
        }
        return tryCounter;
    }
}
