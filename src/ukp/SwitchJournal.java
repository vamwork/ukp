package ukp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import com.cinterion.io.ATCommand;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.file.FileConnection;

public class SwitchJournal {

    private static final String journalFileName = "switch.jrn";

    private static Vector records = new Vector();

    public static int GetCount() {
        return records.size();
    }

    public static byte[] GetRecord(int n) {
        byte[] result = {0x1};
        try {
            result = (byte[]) records.elementAt(n - 1);
        } catch (Exception e) {
            Logger.ErrToLog("*SwitchJournal[0005]" + e.toString());
        }
        return result;
    }

    public static void AddRecord(byte n, byte key, int operation, int result) {
        try {
            byte[] record = new byte[10];
            
            DateTime dt = DtService.GetDateTime(true);
                        
            record[0] = dt.day;
            record[1] = dt.month;
            record[2] = (byte) (dt.year - 2000);
            record[3] = dt.hour;
            record[4] = dt.minute;
            record[5] = dt.second;
            record[6] = n;
            record[7] = key;
            record[8] = (byte) operation;
            record[9] = (byte) result;

            records.insertElementAt(record, 0);

            while (records.size() > 25) {
                records.removeElementAt(records.size() - 1);
            }
            SaveJournal();
        } catch (Exception e) {
            Logger.ErrToLog("*SwitchJournal[0001]" + e.toString());
        }
    }

    public static void LoadJournal() {
        records.removeAllElements();
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + journalFileName);
            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                byte[] record = new byte[10];
                while (is.available() >= 10) {
                    is.read(record, 0, 10);
                    records.addElement(record);
                    record = new byte[10];
                }
                is.close();
            }
            fConn.close();
        } catch (IOException ex) {
            Logger.ErrToLog("*SwitchJornal[0004]" + ex.toString());
        }
    }

    private static void SaveJournal() {
        try {
        FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + journalFileName);
            if (!fConn.exists()) {
                fConn.create();
            }                                     
            OutputStream os = fConn.openOutputStream(0);

            byte[] record;
            for (int i = 0; i < records.size(); i++) {
                record = (byte[]) records.elementAt(i);
                os.write(record);
            }        
            os.close();
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*SwitchJournal[0002]" + ex.toString());
        }       
    }
}
