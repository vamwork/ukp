package ukp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;

import javax.microedition.io.file.FileConnection;

public class MercEvent {

    public OneMercEvent[][] eventsArray = new OneMercEvent[18][11];

    public Mercury mercury;

    public MercEvent(Mercury m) {
        mercury = m;
        for (int i = 1; i < 18; i++) {
            for (int j = 0; j < 11; j++) {
                eventsArray[i][j] = new OneMercEvent();
            }
        }
    }

    public void ReadFullEventsArray2() {
        try {
            for (int i = 1; i < 18; i++) {

                for (int j = 0; j < 10; j++) {
                    byte[] r = mercury.ReadEvent((byte) i, (byte) j);
                    if (r != null) {
                        eventsArray[i][j].body = r;
                        eventsArray[i][j].SetAfterReadStatus();
                    }
                }
                byte[] r = mercury.ReadEvent((byte) i, (byte) 0xFF);
                if (r != null) {
                    eventsArray[i][10].body = r;
                    eventsArray[i][10].SetAfterReadStatus();
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*MercEvent[0001]" + e.toString());
        }
    }

    public void LoadMercEvents() {
        try {
            String fileName = "merc" + Integer.toString(mercury.netAddr) + ".evnt";
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + fileName);
            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                while (is.available() != 0) {
                    byte i = (byte) is.read();
                    byte len = (byte) is.read();
                    byte[] body = new byte[len];
                    is.read(body);

                    eventsArray[i][10].body = body;
                    eventsArray[i][10].SetAfterReadStatus();
                }
                is.close();
            }
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*MercEvent[0005]" + ex.toString());
        }
    }

    public void SaveMercEvents() {
        try {
            String fileName = "merc" + Integer.toString(mercury.netAddr) + ".evnt";
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + fileName);

            if (!fConn.exists()) {
                fConn.create();
            }
            OutputStream os = fConn.openOutputStream(0);

            for (int i = 1; i < 18; i++) {
                OneMercEvent evnt = eventsArray[i][10];
                if (!evnt.isNull) {
                    os.write((byte) i);
                    os.write((byte) evnt.body.length);
                    os.write(evnt.body);
                }
            }
            os.close();
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*MercEvent[0003]" + ex.toString());
        }
    }

    public void ReadNewEvent() {
        try {
            boolean isNewEvent = false;
            for (int i = 1; i < 18; i++) {
                byte[] r = mercury.ReadEvent((byte) i, (byte) 0xFF);
                if (r != null) {
                    byte oldLastRecord = eventsArray[i][10].body[eventsArray[i][10].body.length - 1];
                    byte newLastRecord = r[r.length - 1];

                    if (!eventsArray[i][10].isReaded) {
                        oldLastRecord = 99;
                    }

                    if (newLastRecord != oldLastRecord) {
                        isNewEvent = true;

                        eventsArray[i][10].body = r;
                        eventsArray[i][10].SetAfterReadStatus();

                        while (oldLastRecord != newLastRecord) {
                            if (oldLastRecord == 99) {
                                oldLastRecord = newLastRecord;
                            }
                            oldLastRecord++;
                            if (oldLastRecord > 10) {
                                oldLastRecord = 0;
                            }
                            r = mercury.ReadEvent((byte) i, oldLastRecord);
                            if (r != null) {
                                eventsArray[i][oldLastRecord].body = r;
                                eventsArray[i][oldLastRecord].SetAfterReadStatus();
                            }
                        }
                    }
                }
            }
            if (isNewEvent) {
                SaveMercEvents();
            }
        } catch (Exception e) {
            Logger.ErrToLog("*MercEvent[0002]" + e.toString());
        }
    }

    public void MakeMessages() {
        for (int i = 1; i < 18; i++) {
            for (int j = 0; j < 10; j++) {
                OneMercEvent evnt = (OneMercEvent) eventsArray[i][j];
                if (!evnt.isNull) {
                    if (!evnt.isSended) {

                        byte[] body = new byte[evnt.body.length + 3];
                        body[0] = (byte) 1; // Меркурий 230
                        body[1] = (byte) i;
                        body[2] = (byte) j;

                        System.arraycopy(evnt.body, 0, body, 3, evnt.body.length);

                        DateTime dt = DtService.GetDateTime(true);

                        Message msg = new Message(mercury.netAddr, (byte) 7, dt.day, dt.month, dt.year, dt.hour, dt.minute, dt.second, body);
                        msg.ISet = (ISetAsSend) evnt;
                        Service.AddMsgToOrder(msg);
                    }
                }
            }
        }
    }
}
