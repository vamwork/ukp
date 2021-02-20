package ukp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.microedition.io.Connector;

import javax.microedition.io.file.FileConnection;

public class Archiver {

    public static Archiver profile = new Archiver(24, ".arh");

    public static Archiver energy = new Archiver(84, ".enr");

    private byte[][] archive; // архив за сутки

    private int archiveLength;

    private String archExtension;

    Archiver(int archLen, String archExt) {
        archiveLength = archLen;
        archExtension = archExt;
    }

    // читает архив из файла, при этом выполняет разбор по реальным получасовкам
    public byte ReadArchive(byte day, byte month, byte year, byte netAddr) {
        byte result = 0;
        archive = new byte[48][];
        for (int i = 0; i < 48; i++) {
            archive[i] = null;
        }

        byte[] record = new byte[archiveLength];
        String dd = Integer.toString(day);
        String mm = Integer.toString(month);
        String yy = Integer.toString(2000 + year);

        if (dd.length() < 2) {
            dd = "0" + dd;
        }
        if (mm.length() < 2) {
            mm = "0" + mm;
        }
        if (yy.length() < 2) {
            yy = "0" + yy;
        }
        String fileName = "a:\\storage\\" + dd + mm + yy + archExtension;

        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + dd + mm + yy + archExtension);
            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                while (is.available() != 0) {
                    if (record[0] == netAddr) {
                        byte hour = record[1];
                        byte minute = record[2];
                        byte indx = (byte) (hour * 2 + (byte) (minute / 30)); // разбор по                        
                        archive[indx] = record;
                        record = new byte[archiveLength];
                        result++; // считаем количество записей
                    }
                }
                is.close();
            }
            fConn.close();

        } catch (Exception ex) {
            Logger.ErrToLog("*BaseArchiver" + archExtension + "[0001]" + ex.toString());
        }
        return result;
    }

    // вернуть заполнение
    public byte[] GetInfill() {
        byte[] result = new byte[48];
        byte[] record;
        for (int i = 0; i < 48; i++) {
            record = archive[i];
            result[i] = (byte) (record == null ? 0 : 1);
        }
        return result;
    }

    // вернуть запись indx архива
    public byte[] GetRecord(byte indx) {
        byte[] result = archive[indx];
        if (result == null) {
            result = new byte[24];
            for (int i = 0; i < result.length; i++) {
                result[i] = (byte) 0xFF;
            }
        }
        return result;
    }

    // добавить запись к архиву
    public void SaveArchRecord(byte[] archRec) {
        // определим имя файла
        try {
//            String netTableFileName;
//
//            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + DtService.GetDDMMYY() + archExtension);
//            if (!fConn.exists()) {
//                fConn.create();
//            }
//
//            OutputStream os = fConn.openOutputStream(fConn.totalSize());  //записать в конец файла
//            os.write(archRec, 0, archRec.length);
//            os.close();
//            fConn.close();

        } catch (Exception ex) {
            Logger.ErrToLog("*BaseArchiver" + archExtension + "[0002]" + ex.toString());
        }
    }

    // полная очистка storage
    public static void FullCleaning() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/");
            Enumeration fileEnum;
            fileEnum = fConn.list();
            while (fileEnum.hasMoreElements()) {
                try {
                    String currentFile = "file:///a:/" + (String) fileEnum.nextElement();
                    FileConnection f = (FileConnection) Connector.open(currentFile);
                    f.delete();
                    f.close();
                } catch (Exception e) {
                    Logger.ErrToLog("*BaseArchiver FullCleaning no critical " + e.toString());
                }
            }
            //Restart           
            String ans = Dialer.atListener.SendAtCommand("AT+CFUN=1,1\r");
            Logger.ErrToLog("Restart:" + ans);
        } catch (Exception ex) {
            Logger.ErrToLog("*BaseArchiver FullCleaning" + ex.toString());
        }
    }

    // полное удаление архива
    public void ArchiveCollector() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/");

            Enumeration fileEnum;
            fileEnum = fConn.list();
            while (fileEnum.hasMoreElements()) {
                String currentFile = (String) fileEnum.nextElement();

                if (currentFile.indexOf(archExtension) > 0) {
                    FileConnection f = (FileConnection) Connector.open(currentFile);
                    f.delete();
                    f.close();
                }
            }
        } catch (Exception ex) {
            Logger.ErrToLog("*BaseArchiver [ArchiveCollector]:" + ex.toString());
        }
    }

    // удаляет старые файлы архивов
    // TODO работать не будет
    public void GarbageCollector() {
        try {
            String crnt = DtService.GetDDMMYY();

            int crntDay = Integer.valueOf(crnt.substring(0, 2)).intValue();
            int crntMonth = Integer.valueOf(crnt.substring(2, 4)).intValue();
            int crntYear = Integer.valueOf(crnt.substring(4, 6)).intValue();
            int crntDays = crntYear * 365 + crntMonth * 31 + crntDay;

            FileConnection fConn = (FileConnection) Connector.open("file:///a:/");
            Enumeration fileEnum;
            fileEnum = fConn.list();
            while (fileEnum.hasMoreElements()) {
                String currentFile = (String) fileEnum.nextElement();

                if (currentFile.indexOf(archExtension) > 0) {

                    int day = Integer.valueOf(currentFile.substring(0, 2)).intValue();
                    int month = Integer.valueOf(currentFile.substring(2, 4)).intValue();
                    int year = Integer.valueOf(currentFile.substring(4, 6)).intValue();
                    int days = year * 365 + month * 31 + day;

                    if (days < crntDays - 62) {
                        FileConnection f = (FileConnection) Connector.open(currentFile);
                        f.delete();
                        f.close();
                    }
                }
            }
        } catch (Exception ex) {
            Logger.ErrToLog("*BaseArchiver [GarbageCollector]:" + ex.toString());
        }
    }
}
