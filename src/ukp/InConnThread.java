package ukp;

import com.cinterion.io.ATCommand;
import java.io.InputStream;
import java.io.OutputStream;

public class InConnThread extends Thread {

    private int waitMsec = 250; // время "ожидания" пакета
    private static final long GSM_WAIT = 30000; // время неактивности канала GSM    
    private InputStream dataIn;
    private OutputStream dataOut;
    public ATCommand atc;

    public InConnThread(ATCommand a) {
        atc = a;
    }

    public void run() {
        try {
            dataIn = atc.getDataInputStream();
            dataOut = atc.getDataOutputStream();

            

            long lastActivityTime = System.currentTimeMillis(); // время последней активности

            while (true) {
                // попытаемся получить пакет. Ошраничение по временни неактивности                        
                // подготовим буфер и т.д.
                StringBuffer strPackage = new StringBuffer();
                strPackage.delete(0, strPackage.length());
                long firstMsec = System.currentTimeMillis();
                int c = -1;

                if (dataIn.available() > 0) {
                    while (firstMsec + waitMsec > System.currentTimeMillis()) {
                        try {
                            if (dataIn.available() > 0) {
                                c = dataIn.read();
                                if (c >= 0) {
                                    strPackage.append((char) c);
                                    // данные пришли - отодвинем метку времени                                            
                                    firstMsec = System.currentTimeMillis();
                                }
                            }
                        } catch (Exception e) {
                            Logger.ErrToLog("*InConnThread[0008]" + e.toString());
                        }
                    }
                }
                if (strPackage.length() > 0) {
                    // получен пакет
                    byte[] buffer = strPackage.toString().getBytes();

                    // пролонгируем открытость канала, если конечно он уже был открыт
                    if (Controller.channelIsOpen > 0) {
                        Controller.SetChannelIsOpen();
                    }
                    lastActivityTime = System.currentTimeMillis(); // передвинем время последней активности
                    // проверим пакет на корректность crc
                    if ((buffer.length > 2) && (Global.CheckCRC(buffer))) {
                        // узнаем, кому предназначается пакет (нулевые адреса коротких пакетов - контроллеру)
                        if ((buffer[0] == Controller.netAddr) | (buffer[0] == 0)) {
                            // это для контроллера
                            byte[] resp = Controller.Parse(buffer);
                            if (resp.length > 3) {
                                dataOut.write(resp);
                            }
                        } else {
                            // это сквозной канал до счётчика передаём его счётчику и ждём ответ       
                            byte[] resp = Controller.cp.Request(buffer, 80);
                            if (resp != null) {
                                // ретранслируем ответ
                                dataOut.write(resp);
                            }
                        }
                    }
                }
                if ((lastActivityTime + GSM_WAIT) < System.currentTimeMillis()) {
                    // канал долго был в неактивном состоянии
                    dataIn.close();
                    dataOut.close();
                    try {
                        atc.breakConnection();
                        atc.send("ATH\r");
                    } catch (Exception e) {
                        Logger.ErrToLog("*InConnThread[0005]" + e.toString());
                    }
                }

            }
        } catch (Exception e) {
            Task.isChannel = false;
            Logger.ErrToLog("*InConnThread[0007]" + e.toString());
        } finally {
            // Разблокируем выполнение остальных задач
            Task.isChannel = false;
      
            try {
                if (dataIn != null) {
                    dataIn.close();
                }
                if (dataOut != null) {
                    dataOut.close();
                }

                atc.breakConnection();
                atc.send("ATH\r");
                
            } catch (Exception ex) {
                Logger.ErrToLog("*InConnThread[0006]" + ex.toString());
            }
        }
    }
}
