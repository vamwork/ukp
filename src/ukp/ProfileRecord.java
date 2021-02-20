package ukp;

import java.util.Calendar;
import java.util.Date;

//класс, инкапсулирующий запись профиля мощности
public class ProfileRecord {

    // дата, время записи
    public long dateTime;

    public int day;

    public int month;

    public int year;

    public int hour;

    public int minute;

    public boolean isNotNull = false; // признак того, что запись прочитана

    public boolean isSended = false; // признак того, что запись отправлена

    public boolean isBad = false; // признак того, что запись испорчена

    public byte[] data;

    public int pointer; //адрес, который должен быть в адресном пространстве счётчика  

    public ProfileRecord(long time) {
        dateTime = time;
        Date d = new Date();
        d.setTime(dateTime);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        day = c.get(Calendar.DATE);
        month = c.get(Calendar.MONTH);
        year = c.get(Calendar.YEAR);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        pointer = -1;
    }
}
