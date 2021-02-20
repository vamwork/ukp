package ukp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;

import javax.microedition.io.file.FileConnection;

public class DigitalModule {

    private static final byte ADAM4051 = 1;

    private static final byte ADAM4014 = 2;

    private static final String netTableFileName = "digital.net";

    private static byte[] netAddrTable = null; // список адресов модулей

    // прочитать таблицу адресов доступных модулей
    private void LoadNetTable() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + netTableFileName);
            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                if (is.available() != 0) {
                    netAddrTable = new byte[is.available()];
                    is.read(netAddrTable);
                }
                is.close();
                fConn.close();
            }
        } catch (Exception ex) {
            Logger.ErrToLog("*DigitalModule[LoadNetTable]" + ex.toString());
        }
    }

// записать таблицу адресов доступных модулей
    private void SaveNetTable() {
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + netTableFileName);
            if (!fConn.exists()) {
                fConn.create();
            }
            OutputStream os = fConn.openOutputStream(0);
            os.write(netAddrTable);
            os.close();
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*DigitalModule[SaveNetTable]" + ex.toString());
        }
    }
}
