package ukp;

public class TimeService {

    private static final long MAX_ANSWER_TIME = 5000; // макисмальное время ответа сервера [млc]

    public static final long MAX_DELTA_TIME = 5000; // допустимое время расхождения сервера и контроллера [млс]

    private static long firstTime = 0;

    private static long serverTime = 0;

    private static long lastTime = 0;

    public static boolean timeIsCorrectFromServer = false; // признак того, что время контроллера синхронизировано с сервером

    public static void TimeCheck(long t1, long t2, long t3) {                      
        if (((t3 - t1) <= MAX_ANSWER_TIME) || Controller.timeCorrector == 0) {
            // получено время с сервера
            long t4 = (long) ((t3 - t1) / 2);

            if (Math.abs(t2 - (t1 + t4)) > MAX_DELTA_TIME) {
                // расхождение больше 5 сек
                Controller.timeCorrector = Controller.timeCorrector + (t2 - (t1 + t4));
                Controller.timeIsSynchro = true;
                
                Event.AddEvent((byte) 1, "003%" + String.valueOf((t2 - (t1 + t4))) + "%");
                Event.SaveEvents();
            }
            timeIsCorrectFromServer = true;
        }
    }
}
