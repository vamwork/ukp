package ukp;

import java.io.*;

import javax.microedition.io.*;

public class ComPort {

    //IRZ
    private String conStr = "comm:COM1;blocking=off;baudrate=9600;autorts=off;autocts=off";

    //Novelti
    //private String conStr = "comm:COM0;blocking=off;baudrate=9600;autorts=off;autocts=off";    
    
    private CommConnection connection;

    private DataInputStream input = null;

    private DataOutputStream output = null;

    private int errCount = 0;

    public ComPort() {
        try {
            connection = (CommConnection) Connector.open(conStr);
            input = connection.openDataInputStream();
            output = connection.openDataOutputStream();
        } catch (Exception e) {
            Logger.ErrToLog("*ComPort[0001]" + e.toString());
        }
    }

    public void ReOpenComPort() {
        try {
            output.close();
            input.close();
            connection.close();

            long firstMsec = System.currentTimeMillis();
            while (firstMsec + 1000 > System.currentTimeMillis()) {
                // null
            }
            connection = (CommConnection) Connector.open(conStr);
            input = connection.openDataInputStream();
            output = connection.openDataOutputStream();

            errCount = 0;
        } catch (Exception e) {
            Logger.ErrToLog("*ComPort[0005]" + e.toString());
        }
    }

    // используется для сервисного режима
    public void PortListen() {
        try {
            if (output == null) {
                ReOpenComPort();
            }
            //ControlObject.dataOut = output;            
            int wait = 1;
            try {                
                if (input.available() > 0) {                  
                    int c = -1;
                    StringBuffer strResponse = new StringBuffer();                    
                    // слушаем порт
                    long firstMsec = System.currentTimeMillis();                    
                    while (firstMsec + wait > System.currentTimeMillis()) {
                        try {                            
                            if (input.available() > 0) {
                                c = input.read();
                                if (c >= 0) {
                                    strResponse.append((char) c);
                                    // данные пришли - отодвинем метку времени
                                    firstMsec = System.currentTimeMillis();
                                    // и изменим задержку ожидания
                                    wait = 75;
                                }
                            }
                        } catch (Exception e) {
                            Logger.ErrToLog("*ComPortListener[0001]" + e.toString());
                        }
                    }

                    byte[] buffer = strResponse.toString().getBytes();

                    if (buffer != null) {
                        // пришел пакет по порту
                        if ((buffer.length > 2) && (Global.CheckCRC(buffer))) {

                            // узнаем, кому предназначается пакет (нулевые адреса - контроллеру)
                            if ((buffer[0] == Controller.netAddr) | (buffer[0] == 0)) {
                                // это для контроллера

                                byte[] resp = Controller.Parse(buffer);

                                if (resp.length > 3) {
                                    output.write(resp);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.ErrToLog("*ComPort[0002]" + e.toString());
            }
        } catch (Exception ex) {
            Logger.ErrToLog("*ComPort[0003]" + ex.toString());
            ReOpenComPort();
        }
    }

    public byte[] Request(byte[] command, int wait) {
        StringBuffer strResponse = new StringBuffer();
        try {
            Global.Delay(10);
            
            Logger.AddToLog("Command: ", command);
            
            output.write(command);
            int c = -1;

            long firstMsec = System.currentTimeMillis();

            while (firstMsec + wait > System.currentTimeMillis()) {
                try {
                    if (input.available() > 0) {
                        c = input.read();
                        if (c >= 0) {
                            strResponse.append((char) c);
                            firstMsec = System.currentTimeMillis();
                            wait = 50;
                        }
                    }
                } catch (IOException e) {
                    Logger.ErrToLog("*ComPortListener[0001]" + e.toString());
                }
            }
        } catch (Exception e) {
            Logger.ErrToLog("*ComPort[0004]" + e.toString());
        }
        try {
            //dataOut.flush();

        } catch (Exception e) {
            Logger.ErrToLog("*ComPort[0006]" + e.toString());
        }

        Logger.AddToLog("Response: ", strResponse.toString().getBytes());
        
        return strResponse.toString().getBytes();
    }

    public void StringTo232(String text) {
        try {
            output.write(Global.StringToBytes(text));
        } catch (Exception e) {
        }
    }
}
