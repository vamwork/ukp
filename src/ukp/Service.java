package ukp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class Service {

    public static Vector msgList = new Vector();

    public static boolean isAnySendSuccess = false;
    public static int sendErorCount = 0;

    // обработать очередь сообщений для отправки нескольких сообщений
    public static void OrderMultipleProcessing() {
        try {
            Logger.IamWork("-net-");
            Vector removed = new Vector(); // сюда будем "складывать" сообщения для удаления

            // TODO Подумать над оптимизацией
            if (msgList.size() >= 1) {
                int count = msgList.size();
                if (count > 10) {
                    count = 10;
                }

                Message msgs[] = new Message[count];

                for (int i = 0; i < count; i++) {
                    msgs[i] = (Message) msgList.elementAt(i);
                }

                if (!SendMultipleMessage(msgs)) {
                    // не получилось
                    for (int i = 0; i < count; i++) {
                        if (msgs[i].DecTryCounter() == 0) {
                            // попыток больше нет - запомним сообщение для удаления
                            removed.addElement(msgs[i]);
                        }
                    }
                } else {
                    // сообщения успешно отправлены - запомним сообщение для удаления
                    for (int i = 0; i < count; i++) {
                        removed.addElement(msgs[i]);
                    }
                }

                // удалим сообщения
                for (int i = 0; i < removed.size(); i++) {
                    Message msg = (Message) removed.elementAt(i);
                    if (msg.ISet != null) {
                        msg.ISet.SetAsSend();
                    }
                    msgList.removeElement(msg);
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*Service[0008]" + e.toString());
        }
    }

    // добавить сообщение в очередь
    public static void AddMsgToOrder(Message msg) {
        // проверим, н превышен ли максимальный размер msgList
        while (msgList.size() > 500) {
            msgList.removeElementAt(0);
        }

        // проверим, нет ли уже такого Message
        boolean isBe = false;
        for (int i = 0; i < msgList.size(); i++) {
            Message message = (Message) msgList.elementAt(i);
            if (message.EqaualsMessage(msg)) {
                isBe = true;
                break;
            }
        }
        if (!isBe) {
            msgList.addElement(msg);
        }
    }

    // отправить несколько сообщени на web-кэш
    private static boolean SendMultipleMessage(Message msg[]) {
        boolean result = false;
        int leng = 0;
               
        // определим длину итогового сообщения
        for (int i = 0; i < msg.length; i++) {
            leng = leng + msg[i].messageBody.length;
        }

        byte body[] = new byte[leng];
        int pos = 0;

        // сформируем "тело" сообщения
        for (int i = 0; i < msg.length; i++) {
            System.arraycopy(msg[i].messageBody, 0, body, pos, msg[i].messageBody.length);
            pos = pos + msg[i].messageBody.length;
        }

        String destHost = Controller.destHost;

        HttpConnection hc = null;
        InputStream is = null;
        OutputStream os = null;

        // проверим, указан ли destHost
        if (destHost.length() > 5) {
            // формат запроса M=, P=, V=
            String params = "M=SILESTA123" + "&P=" + Controller.netPassword + "&V=" + Global.ToString(Controller.version);

            String connProfile = Controller.connectionProfile;
            

            try {
                String openParm = "http://" + destHost + "?" + params + ";" + connProfile;
                
                Logger.AddToLog("openParm " + openParm);
                
                hc = (HttpConnection) Connector.open(openParm);
                hc.setRequestMethod(HttpConnection.POST);
                hc.setRequestProperty("Content-Length", Integer.toString(body.length));
                hc.setRequestProperty("Content-Language", "en-US");
                hc.setRequestProperty("Content-Type", "text");

                long firstTime = System.currentTimeMillis() + Controller.timeCorrector;
                long serverTime = 0;
                long lastTime = 0;

                os = hc.openOutputStream();
                os.write(body);
                
                Global.trafficCounter = Global.trafficCounter + body.length;

                int rc = hc.getResponseCode();
                lastTime = System.currentTimeMillis() + Controller.timeCorrector;

                if (rc != HttpConnection.HTTP_OK) {
                    throw new IOException("HTTP response code: " + rc);
                } else {
                    // Получено Ok                                                           
                    // Начнем отсчет усешных посылок
                    Service.isAnySendSuccess = true;
                    Service.sendErorCount = 0;
                    Controller.rebootCounter = 0;

                    is = hc.openInputStream();
                    int len = is.available();
                    byte[] data = new byte[len];
                    for (int i = 0; i < len; i++) {
                        byte b = (byte) is.read();
                        data[i] = b;
                    }
                    try {
                        serverTime = Long.parseLong(Global.ToString(data));
                        TimeService.TimeCheck(firstTime, serverTime, lastTime);
                    } catch (Exception ex) {
                        throw new IOException("TimeService Error: " + ex.toString());
                    }
                }
                result = true;

            } catch (Exception e) {
                //увеличим счетчик ошибок
                Service.sendErorCount++;                
                Logger.ErrToLog("*Service[0002]" + e.toString());
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        Logger.ErrToLog("*Service[0003]" + e.toString());
                    }
                }

                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
                        Logger.ErrToLog("*Service[0004]" + e.toString());
                    }
                }

                if (hc != null) {
                    try {
                        hc.close();
                    } catch (Exception e) {
                        Logger.ErrToLog("*Service[0005]" + e.toString());
                    }
                }
            }
        }
        return result;
    }
}
