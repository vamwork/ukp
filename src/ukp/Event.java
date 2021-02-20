package ukp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;

import javax.microedition.io.file.FileConnection;

public class Event implements ISetAsSend {

    /* 001 - первичная установка времеи
	 * 002 - "я работаю"
	 * 003 - загрузка времени с сервера
	 * 004 - полная кооретировка времнеи счетчика
	 * 005 - частичная кооретировка времнеи счетчика
     */
    private static final String eventFileName = "event.evn";

    // Статические поля
    private static boolean isSaved = false;

    public static Vector eventList = new Vector();

    // Описание события
    private String dateTime; // временная метка события

    private byte type; // тип события 1-уведомления 2-ошибки и т.д.

    private String body; // текст сообщения

    private boolean isSendet; // признак того, что событие передано на ВУ

    // Методы
    public String AsString() {
        String s = "N";
        if (isSendet) {
            s = "Y";
        }
        return "<Event>" + dateTime + ", " + String.valueOf(type) + ", " + body
                + ", " + s + "</Event>";
    }

    public static void ReadEvents() {
        // предварительно очистим список
        eventList.removeAllElements();
        try {
            FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + eventFileName);
            if (fConn.exists()) {
                InputStream is = fConn.openInputStream();

                while (is.available() != 0) {
                    Event e = new Event();

                    e.dateTime = Global.ReadString(is);
                    e.type = (byte) is.read();
                    e.body = Global.ReadString(is);
                    e.isSendet = is.read() == 1;
                    eventList.addElement(e);
                }
                is.close();
            }
            fConn.close();
        } catch (Exception ex) {
            Logger.ErrToLog("*Event[ReadEvents]" + ex.toString());
        }
        isSaved = true;
    }

    public static void SaveEvents() {
        if (!isSaved) {
            try {
                FileConnection fConn = (FileConnection) Connector.open("file:///a:/" + eventFileName);
                if (!fConn.exists()) {
                    fConn.create();
                }
                OutputStream os = fConn.openOutputStream(0);
                for (Enumeration e = eventList.elements(); e.hasMoreElements();) {
                    Event event = (Event) e.nextElement();
                    os.write(Global.GetStringAsArray(event.dateTime));
                    os.write(event.type);
                    os.write(Global.GetStringAsArray(event.body));
                    if (event.isSendet) {
                        os.write((byte) 1);
                    } else {
                        os.write((byte) 0);
                    }
                }
                os.close();
                isSaved = true;
                fConn.close();
            } catch (Exception ex) {
                Logger.ErrToLog("*Event[SaveEvents]" + ex.toString());
            }
        }
    }

    public static void AddEvent(byte type, String body) {
        //if (!TimeService.timeIsCorrectFromServer) {
        if (true) {
            Event event = new Event();
            //event.dateTime = Global.GetDateTime();

            event.dateTime = DtService.GetDateTime(false).toString();

            event.type = type;
            event.body = body;
            event.isSendet = false;
            eventList.addElement(event);

            if (eventList.size() > 20) {
                eventList.removeElementAt(0);
            }

            isSaved = false;

            DateTime dt = DtService.GetDateTime(false);

            Message msg = new Message(0, (byte) 99, dt.day, dt.month, dt.year, dt.hour, dt.second, dt.second, Global.GetStringAsArray(body));

            Logger.AddToLog("Event type:", type);
            Logger.AddToLog("DateTime:" + dt.ToString());
            Logger.AddToLog("Body:" + body);

            msg.ISet = (ISetAsSend) event;
            Service.AddMsgToOrder(msg);
        }
    }

    public void SetAsSend() {
        isSendet = true;
        isSaved = false;
    }
}
