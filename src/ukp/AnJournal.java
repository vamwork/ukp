package ukp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;

import javax.microedition.io.file.FileConnection;

// нужно проверить, нет ли особенностей работы с вектором. Дело в том, что при
// записи используется
// интерфейс enumeration, а при чтении записи добавляются.
public class AnJournal {

    private static final int maxJournalLength = 100; // максимальный размер журнала

    private static Vector eventList = new Vector();

    private static boolean toSave = false; // признак необходимости записи на диск

    // собственно событие
    private static byte[] eventRecord;

    public static void AddEvent(Mercury mercury, byte parNumber, byte event) {
        try {
            eventRecord = new byte[18];
            eventRecord[0] = mercury.netAddr;
            
            DateTime dt = DtService.GetDateTime(true);

            eventRecord[1] = (byte) (dt.year - 2000);
            eventRecord[2] = dt.month;
            eventRecord[3] = dt.day;
            eventRecord[4] = dt.hour;
            eventRecord[5] = dt.minute;
            eventRecord[6] = dt.second;
            eventRecord[7] = parNumber;
            eventRecord[8] = event;
            if (parNumber > 3) {
                // напряжения
                Global.CopyArray(eventRecord, Mercury.ToBytes(mercury.currentU1.value, 100), 9);
                Global.CopyArray(eventRecord, Mercury.ToBytes(mercury.currentU2.value, 100), 12);
                Global.CopyArray(eventRecord, Mercury.ToBytes(mercury.currentU3.value, 100), 15);
            } else {
                // токи
                Global.CopyArray(eventRecord, Mercury.ToBytes(mercury.currentI1.value, 1000), 9);
                Global.CopyArray(eventRecord, Mercury.ToBytes(mercury.currentI2.value, 1000), 12);
                Global.CopyArray(eventRecord, Mercury.ToBytes(mercury.currentI3.value, 1000), 15);
            }
            // сообщим "дозвонщику", что было аналоговое событие
            Dialer.SetDialMode(1);

            eventList.insertElementAt(eventRecord, 0);
            toSave = true;
            // удалим, при необходимости, записи
            if (eventList.size() > maxJournalLength) {
                eventList.removeElementAt(eventList.size() - 1);
            }
        } catch (Exception e) {
            Logger.ErrToLog("*AnJournal[0001]" + e.toString());
        }
    }
   
    public static void Save() {
        try {
            if (toSave) {
                FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + "analog.jrn");
                if (!fConn.exists()) {
                    fConn.create();
                }
                OutputStream os = fConn.openOutputStream(0);
                for (Enumeration e = eventList.elements(); e.hasMoreElements();) {
                    eventRecord = (byte[]) e.nextElement();
                    try {
                        os.write(eventRecord);
                    } catch (Exception ex) {
                        Logger.ErrToLog("*AnJournal[0002]" + ex.toString());
                    }
                }
                os.close();
                fConn.close();
                toSave = false;
            }
        } catch (Exception ex) {
            Logger.ErrToLog("*AnJournal[0003]" + ex.toString());
        }
    }

    public static void Load() {
        // очистим eventList
        eventList.removeAllElements();

        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + "analog.jrn");

            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                while (is.available() != 0) {
                    eventRecord = new byte[18];
                    is.read(eventRecord, 0, 18);
                    eventList.addElement(eventRecord);
                }
                is.close();
            }
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*AnJournal[0004]" + ex.toString());
        }
    }

    public static int EventsCount() {
        return eventList.size();
    }

    public static void Clear() {
        eventList.removeAllElements();
        toSave = true;
        Save();
    }

    public static byte[] GetEvent(byte n) {
        eventRecord = null;
        try {
            eventRecord = (byte[]) eventList.elementAt(n);
        } catch (Exception e) {
            Logger.ErrToLog("*AnJournal[0005]" + e.toString());
        }
        return eventRecord;
    }
}
