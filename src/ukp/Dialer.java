package ukp;

//класс, отвечающий за дозвон
public class Dialer {

    public static final int MAX_TRY = 3; // количество попыток

    public static final int DIALING_WAIT = 10000 / Task.period; // время мс м/у дозвонами

    public static final int SMALL_DIALING_WAIT = 4000 / Task.period;

    public static int tryCounter = 0; // счётчик попыток дозвона

    public static int toNextDialingWait = 0; // счётчик мс до следующего дозвона

    public static AtListener atListener;

    // дозвон
    public static void Dial() {
        if (tryCounter > 0) {
            long startTime = 0;
            long endTime = 0;
            //замеряется время между подачей сигнала и получением ответа. 
            //Если это время меньше 100мс, считаем, что реальный вызов не проходил
            if (Controller.phone[0] != 0) {
                try {
                    startTime = System.currentTimeMillis();
                    String command = "ATD" + Global.ToString(Controller.phone) + "\r";
                    String ans = atListener.SendAtCommand(command);
                    endTime = System.currentTimeMillis();
                } catch (Exception e) {
                    Logger.ErrToLog("*Dialer[0001]" + e.toString());
                }
            }
            if ((endTime - startTime) > 1000) {
                //была полноценная, но не удачная попытка дозвона
                tryCounter--;
                toNextDialingWait = DIALING_WAIT;
            }
            toNextDialingWait = SMALL_DIALING_WAIT; //была "холостая" попытка дозвона
        }
    }

    // установка дозвона
    public static void SetDialMode(int eventType) {
        if (tryCounter == 0) {
            toNextDialingWait = SMALL_DIALING_WAIT; //прежде чем звонить, чуть-чуть подождем...
        }
        // eventType 1-аналоговый, 2-дискретный
        if (Controller.toDialing == 4) {
            // по любому событию
            tryCounter = MAX_TRY;
        } else if (eventType == 1 & Controller.toDialing == 3) {
            // "аналоговое" событие
            tryCounter = MAX_TRY;
        } else if (eventType == 1 & Controller.toDialing == 3) {
            // "дискретное" событие
            tryCounter = MAX_TRY;
        }
    }
}
