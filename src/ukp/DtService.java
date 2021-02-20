package ukp;

import java.util.Calendar;
import java.util.Date;

/**
 * New global DateTime service
 */
public class DtService {

    public static boolean inSync = false;

    public static DateTime GetDateTime(boolean isReal) {
        DateTime result;
        if ((Controller.timeCorrector == 0) && isReal) {            
            result = null;
        } else {
            result = new DateTime();
            result.second = (byte) GetSecond();
            result.minute = (byte) GetMinute();
            result.hour = (byte) GetHour();
            result.day = (byte) GetDay();
            result.month = (byte) GetMonth();
            result.year = GetYear();            
        }
        return result;
    }

    private static Calendar GetCurrentCalendar() {
        Date d = new Date();
        d.setTime(d.getTime() + Controller.timeCorrector);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
    }

    // возвращает текущий час
    private static int GetHour() {
        return (GetCurrentCalendar().get(Calendar.HOUR_OF_DAY));
    }

    // возвращает текущую минуту
    private static int GetMinute() {
        return (GetCurrentCalendar().get(Calendar.MINUTE));
    }

    //  возвращает текущую секунду
    private static int GetSecond() {
        return (GetCurrentCalendar().get(Calendar.SECOND));
    }

    // день
    private static int GetDay() {
        return (GetCurrentCalendar().get(Calendar.DATE));
    }

    // день недели
    private static int GetDayOfWeek() {
        return (GetCurrentCalendar().get(Calendar.DAY_OF_WEEK));
    }

    // месяц
    private static int GetMonth() {
        return (GetCurrentCalendar().get(Calendar.MONTH) + 1);
    }

    // год
    private static int GetYear() {
        return (GetCurrentCalendar().get(Calendar.YEAR));
    }

    //возвращает время в формате HHMMSS
    public static String GetHHMMSS() {
        String hh = Integer.toString(GetHour());
        String mm = Integer.toString(GetMinute());
        String ss = Integer.toString(GetSecond());

        if (hh.length() < 2) {
            hh = "0" + hh;
        }
        if (mm.length() < 2) {
            mm = "0" + mm;
        }
        if (ss.length() < 2) {
            ss = "0" + ss;
        }
        return hh + mm + ss;
    }

    public static String GetDDMMYY() {
        String dd = Integer.toString(GetDay());
        String mm = Integer.toString(GetMonth());
        String yy = Integer.toString(GetYear());

        if (dd.length() < 2) {
            dd = "0" + dd;
        }
        if (mm.length() < 2) {
            mm = "0" + mm;
        }
        if (yy.length() < 2) {
            yy = "0" + yy;
        }
        return dd + mm + yy;
    }
}
