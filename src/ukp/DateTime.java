package ukp;

import java.util.Calendar;
import java.util.Date;

public class DateTime {

    public int year = 0;
    public byte month = 0;
    public byte day = 0;
    public byte hour = 0;
    public byte minute = 0;
    public byte second = 0;

    public DateTime() {
    }

    public String ToString() {
        String dd = Integer.toString(day);
        String mm = Integer.toString(month);
        String yy = Integer.toString(year);

        if (dd.length() < 2) {
            dd = "0" + dd;
        }
        if (mm.length() < 2) {
            mm = "0" + mm;
        }
        if (yy.length() < 2) {
            yy = "0" + yy;
        }

        String hh = Integer.toString(hour);
        String mnt = Integer.toString(minute);
        String ss = Integer.toString(second);

        if (hh.length() < 2) {
            hh = "0" + hh;
        }
        if (mnt.length() < 2) {
            mnt = "0" + mnt;
        }
        if (ss.length() < 2) {
            ss = "0" + ss;
        }
        return dd + "." + mm + "." + yy + " " + hh + ":" + mnt + ":" + ss;        
    }
    
    //используется для обновления времени в счетчиках
    public DateTime(long millis) {
        Calendar c = Calendar.getInstance();
        Date d = new Date();
        d.setTime(millis);
        c.setTime(d);
        year = (byte) (c.get(Calendar.YEAR) - 2000);
        month = (byte) (c.get(Calendar.MONTH) + 1);
        day = (byte) (c.get(Calendar.DAY_OF_MONTH) + 1);
        hour = (byte) c.get(Calendar.HOUR);
        minute = (byte) c.get(Calendar.MINUTE);
        second = (byte) c.get(Calendar.SECOND);
    }
}
