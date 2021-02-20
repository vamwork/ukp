package ukp;

import java.util.Calendar;
import java.util.Date;

//класс, инкапсулирующий запись профиля мощности
public class PowerProfileRecord {
    // дата, время записи

    public long dateTime;
    public int day;
    public int month;
    public int year;
    public int hour;
    public int minute;
    public boolean isNotNull = false; // признак того, что запись прочитана
    public boolean isSended = false; // признак того, что запись отправлена
    public int bad = 3; // признак того, что запись испорчена (как только дойдет до нуля, значит bad)
    public byte[] data;
    public int pointer; //адрес, который должен быть в адресном пространстве счётчика  
    public byte bit17 = 0;

    public String GetDateTimeAsString() {
        String dd = Integer.toString(day);
        String mm = Integer.toString(month);
        String yy = Integer.toString(year);
        String hh = Integer.toString(hour);
        String mn = Integer.toString(minute);
        String ss = Integer.toString(0);

        if (dd.length() < 2) {
            dd = "0" + dd;
        }
        if (mm.length() < 2) {
            mm = "0" + mm;
        }
        if (yy.length() < 2) {
            yy = "0" + yy;
        }

        if (hh.length() < 2) {
            hh = "0" + hh;
        }
        if (mn.length() < 2) {
            mn = "0" + mn;
        }
        if (ss.length() < 2) {
            ss = "0" + ss;
        }

        return dd + mm + yy + hh + mn + ss;
    }

    public PowerProfileRecord() {
    }

    public PowerProfileRecord(long time) {
        dateTime = time;
        Date d = new Date();
        d.setTime(dateTime);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        day = c.get(Calendar.DATE);
        month = c.get(Calendar.MONTH) + 1;
        year = c.get(Calendar.YEAR);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        data = null;
        pointer = -1;
    }
}
