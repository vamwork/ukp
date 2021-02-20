package ukp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class Global {

    public static int trafficCounter = 0;

    public static byte wwwLedSate = 0;

    public static byte prevWwwLedState = 0;

    private static final long MLSEC_IN_DAY = 86400000;

    public static Date dt;

    private static byte[] initCrc = {(byte) 0xFF, (byte) 0xFF};

    private static short[] srCRCHi = {0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
        0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01,
        0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
        0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
        0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00,
        0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
        0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
        0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
        0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01,
        0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80,
        0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1,
        0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01,
        0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
        0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80,
        0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
        0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00,
        0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
        0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81,
        0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
        0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01,
        0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40};

    private static short[] srCRCLo = {0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03,
        0x02, 0xC2, 0xC6, 0x06, 0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04, 0xCC,
        0x0C, 0x0D, 0xCD, 0x0F, 0xCF, 0xCE, 0x0E, 0x0A, 0xCA, 0xCB, 0x0B,
        0xC9, 0x09, 0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA,
        0x1A, 0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC, 0x14, 0xD4,
        0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12, 0x13, 0xD3, 0x11,
        0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3, 0xF2, 0x32,
        0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4, 0x3C, 0xFC, 0xFD,
        0x3D, 0xFF, 0x3F, 0x3E, 0xFE, 0xFA, 0x3A, 0x3B, 0xFB, 0x39, 0xF9,
        0xF8, 0x38, 0x28, 0xE8, 0xE9, 0x29, 0xEB, 0x2B, 0x2A, 0xEA, 0xEE,
        0x2E, 0x2F, 0xEF, 0x2D, 0xED, 0xEC, 0x2C, 0xE4, 0x24, 0x25, 0xE5,
        0x27, 0xE7, 0xE6, 0x26, 0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20,
        0xE0, 0xA0, 0x60, 0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62, 0x66, 0xA6,
        0xA7, 0x67, 0xA5, 0x65, 0x64, 0xA4, 0x6C, 0xAC, 0xAD, 0x6D, 0xAF,
        0x6F, 0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68,
        0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA, 0xBE, 0x7E, 0x7F,
        0xBF, 0x7D, 0xBD, 0xBC, 0x7C, 0xB4, 0x74, 0x75, 0xB5, 0x77, 0xB7,
        0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71, 0x70, 0xB0, 0x50,
        0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92, 0x96, 0x56, 0x57, 0x97,
        0x55, 0x95, 0x94, 0x54, 0x9C, 0x5C, 0x5D, 0x9D, 0x5F, 0x9F, 0x9E,
        0x5E, 0x5A, 0x9A, 0x9B, 0x5B, 0x99, 0x59, 0x58, 0x98, 0x88, 0x48,
        0x49, 0x89, 0x4B, 0x8B, 0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F, 0x8D,
        0x4D, 0x4C, 0x8C, 0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86,
        0x82, 0x42, 0x43, 0x83, 0x41, 0x81, 0x80, 0x40};

    private static byte[] UpdateCrc(byte c, byte[] oldCrc) {
        int i = ToShort(oldCrc[1]) ^ ToShort(c);
        byte[] crc = new byte[2];
        crc[1] = (byte) (oldCrc[0] ^ (byte) srCRCHi[i]);
        crc[0] = (byte) srCRCLo[i];
        return crc;
    }

    public static byte[] GetCrc(byte[] b) {
        byte[] crc = UpdateCrc(b[0], initCrc);
        for (int i = 1; i < b.length; i++) {
            crc = UpdateCrc(b[i], crc);
        }
        return crc;
    }

    public static short ToShort(byte b) {
        short res;
        if (b < 0) {
            res = (short) (256 + b);
        } else {
            res = b;
        }
        return res;
    }

    public static void CopyArray(byte[] base, byte[] part, int from) {
        for (int i = 0; i < part.length; i++) {
            base[i + from] = part[i];
        }
    }

    public static boolean CompareArray(byte[] b1, byte[] b2) {
        boolean result = false;
        if (b1.length == b2.length) {
            result = true;
            for (int i = 0; i < b1.length; i++) {
                if (b1[i] != b2[i]) {
                    result = false;
                }
            }
        }
        return result;
    }

    // сформировать команду: добавить адрес и CRC
    public static byte[] MakeCommand(byte[] b, byte addr) {
        byte[] cmdAndAddr = new byte[b.length + 1];
        cmdAndAddr[0] = addr;
        for (int i = 1; i < cmdAndAddr.length; i++) {
            cmdAndAddr[i] = b[i - 1];
        }
        byte[] crc = GetCrc(cmdAndAddr);
        byte[] fullCommand = new byte[b.length + 3];
        for (int i = 0; i < cmdAndAddr.length; i++) {
            fullCommand[i] = cmdAndAddr[i];
        }
        fullCommand[cmdAndAddr.length] = crc[1];
        fullCommand[cmdAndAddr.length + 1] = crc[0];
        return fullCommand;
    }

    public static boolean CheckCRC(byte[] b) {
        // проверка crc        
        byte[] forCheckCrc = new byte[b.length - 2];
        for (int i = 0; i < forCheckCrc.length; i++) {
            forCheckCrc[i] = b[i];
        }
        byte[] checkCrc = GetCrc(forCheckCrc);
        return ((b[b.length - 2] == checkCrc[1]) & (b[b.length - 1] == checkCrc[0]));
    }

    private static Calendar GetCurrentCalendar() {
        Date d = new Date();
        // System.currentTimeMillis() + Controller.timeCorrector);
        //long delta = ((System.currentTimeMillis() - TimeService.lastSync) / 12);     //12 - поправка на дурацкие часы модуля
        //d.setTime(d.getTime() + Controller.timeCorrector - delta);
        d.setTime(d.getTime() + Controller.timeCorrector);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
    }

    //возвращает число предыдущего дня
    public static int GetPrevDay() {
        Date d = new Date();
        d.setTime(GetCurrentCalendar().getTime().getTime() - 86400000);

        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    // возвращает время в милисекундах
    public static long timeAsMilliseconds(int day, int month, int year, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DATE, day);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime().getTime();
    }

//    // возвращает текущий час
//    private static int GetHour() {
//        return (GetCurrentCalendar().get(Calendar.HOUR_OF_DAY));
//    }
    // возвращает текущую минуту
//    public static int GetMinute() {
//        return (GetCurrentCalendar().get(Calendar.MINUTE));
//    }
    public static int AnyMinuteTo30Min(int anyMinute) {
        int result = anyMinute;
        if (anyMinute >= 30) {
            result = 30;
        }
        if (anyMinute < 30) {
            result = 0;
        }

        return result;
    }

    // возвращает текущую секунду
//    public static int GetSecond() {
//        return (GetCurrentCalendar().get(Calendar.SECOND));
//    }
//
//    // день
//    public static int GetDay() {
//        return (GetCurrentCalendar().get(Calendar.DATE));
//    }
//
//    // день недели
//    public static int GetDayOfWeek() {
//        return (GetCurrentCalendar().get(Calendar.DAY_OF_WEEK));
//    }
//
//    // месяц
//    public static int GetMonth() {
//        return (GetCurrentCalendar().get(Calendar.MONTH) + 1);
//    }
//
//    // год
//    public static int GetYear() {
//        return (GetCurrentCalendar().get(Calendar.YEAR));
//    }
    // возвращает время в формате HHMMSS
//    private static String GetHHMMSS() {
//        String hh = Integer.toString(GetHour());
//        String mm = Integer.toString(GetMinute());
//        String ss = Integer.toString(GetSecond());
//
//        if (hh.length() < 2) {
//            hh = "0" + hh;
//        }
//        if (mm.length() < 2) {
//            mm = "0" + mm;
//        }
//        if (ss.length() < 2) {
//            ss = "0" + ss;
//        }
//        return hh + mm + ss;
//    }
    // преобразование b в двоично-десятичный формат
    public static byte ToDecBin(byte b) {
        return (byte) (((b / 10) << 4) | (b % 10));
    }

    // преобразование из двоично-десятичного формата
    public static byte ToDec(byte b) {
        return (byte) ((b >> 4) * 10 + (b & 0xF));
    }

    public static byte[] IntToBytes(int v) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[3 - i] = (byte) (v >>> (i * 8));
        }
        return result;
    }

    public static String ToString(byte[] b) {
        char[] c = new char[b.length];
        for (int i = 0; i < b.length; i++) {
            c[i] = (char) b[i];
        }
        return new String(c);
    }

    public static String ArrayToHexString(byte[] buffer) {
        String result = "";

        for (int i = 0; i < buffer.length; i++) {
            result = result + "  [" + Integer.toHexString(Global.ToShort(buffer[i])) + "]";
        }
        return result;
    }

    public static String GetRightPart(String str, String left) {
        String result = "";
        String baseStr = str.toLowerCase();
        String leftStr = left.toLowerCase();

        if (baseStr.indexOf(leftStr) == 0) {
            result = str.substring(left.length());
        }
        return result;
    }

    public static byte[] StringToBytes(String s) {
        byte[] result = new byte[s.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) s.charAt(i);
        }
        return result;
    }

    public static byte HexCharToByte(char c) {
        byte result = 0;
        final byte[] d = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        for (int i = 0; i < d.length; i++) {
            if (d[i] == c) {
                result = (byte) i;
            }
        }
        return result;
    }

    public static String AsString(int v, int len) {
        String result = Integer.toString(v);
        while (result.length() < len) {
            result = "0" + result;
        }
        return result;
    }

//    private static String GetDateTime() {
//        String result = GetDDMMYY() + "_" + Integer.toString(GetHour()) + ":"
//                + Integer.toString(GetMinute()) + "."
//                + Integer.toString(GetSecond());
//        return result;
//    }
    // сравнение двух массивов
    public static boolean ArrayEquals(byte[] arr1, byte[] arr2) {
        boolean result = true;
        if (arr1.length != arr2.length) {
            result = false;
        } else {
            for (int i = 0; i < arr1.length; i++) {
                if (arr1[i] != arr2[i]) {
                    result = false;
                }
            }
        }
        return result;
    }

    public static byte[] Crypt(byte[] sensitive, int key) {
        byte[] result = new byte[sensitive.length];
        Random r = new Random(key);
        for (int i = 0; i < sensitive.length; i++) {
            result[i] = (byte) (sensitive[i] + r.nextInt(111 + i));
        }
        return result;
    }

    public static byte[] UnCrypt(byte[] secret, int key) {
        byte[] result = new byte[secret.length];
        Random r = new Random(key);
        for (int i = 0; i < secret.length; i++) {
            result[i] = (byte) (secret[i] - r.nextInt(111 + i));
        }
        return result;
    }

    // прочитать из ByteArrayInputStream строку
    public static String ReadString(InputStream is) throws IOException {
        StringBuffer str = new StringBuffer();
        int cnt = (byte) is.read();
        for (int i = 0; i < cnt; i++) {
            str.append((char) is.read());
        }
        return str.toString();
    }

    public static byte[] GetStringAsArray(String s) {
        byte[] result = new byte[s.length() + 1];

        result[0] = (byte) s.length();
        for (int i = 0; i < s.length(); i++) {
            result[i + 1] = (byte) s.charAt(i);
        }
        return result;
    }

    public static void SaveString(ByteArrayOutputStream os, String s) {
        os.write((byte) s.length());
        for (int i = 0; i < s.length(); i++) {
            os.write((byte) s.indexOf(i));
        }
    }

    // задержка на mSec [млс.]
    public static void Delay(int mSec) {
        try {
            Thread.sleep(mSec);
        } catch (InterruptedException ex) {
            Logger.ErrToLog("Global[001]" + ex.toString());
        }
    }

    public static String ToStringWithZerroSymbol(int v) {
        String result = String.valueOf(v);
        if (result.length() < 2) {
            result = "0" + result;
        }
        return result;
    }

    public static String GetCalendarAsDateTimeString(Calendar c) {
        String result = "";
        String day = Global.ToStringWithZerroSymbol(c.get(Calendar.DAY_OF_MONTH));
        String month = Global.ToStringWithZerroSymbol(c.get(Calendar.MONTH));
        String year = String.valueOf(c.get(Calendar.YEAR));
        String hour = Global.ToStringWithZerroSymbol(c.get(Calendar.HOUR_OF_DAY));
        String minute = Global.ToStringWithZerroSymbol(c.get(Calendar.MINUTE));
        String second = Global.ToStringWithZerroSymbol(c.get(Calendar.SECOND));
        result = result + day + "." + month + "." + year + " " + hour + ":" + minute + ":" + second;

        return result;
    }

    private Global() {
    }
}
