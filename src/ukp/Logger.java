package ukp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

class Logger {

    public static boolean isWorkChar = false;

    public static Vector externalLog = new Vector();

    public static boolean isDateTime = false;

    public static String error;

    public static void AddToLog(String txt, int value) {
        AddToLog(txt + ": " + String.valueOf(value));
    }

    private static void AddToLog(String txt, float value) {
        AddToLog(txt + ": " + String.valueOf(value));
    }

    private static void AddToLogController() {
        Logger.AddToLog("<Controller>");
        Logger.AddToLog(Controller.netAddr);
        Logger.AddToLog(Controller.destHost);
        Logger.AddToLog(Controller.connectionProfile);
        Logger.AddToLog("</Controllrt>");
    }

    private static void AddToLog(short f) {
        AddToLog(Integer.toString(f));
    }

    private static void AddToLog(long l) {
        AddToLog(Long.toString(l));
    }

    private static void AddToLog(float f) {
        AddToLog(Double.toString(f));
    }

    private static void AddToLog(byte[] buffer) {
        String s = "";

        for (int i = 0; i < buffer.length; i++) {
            s = s + "  [" + Integer.toHexString(Global.ToShort(buffer[i])) + "]";
        }
        AddToLog(s);
    }

    private static void AddToLog(boolean b) {
        if (b) {
            AddToLog("TRUE");
        } else {
            AddToLog("FALSE");
        }
    }
       
    private static String BoolToString(boolean f) {
        String result = "FALSE";
        if (f) {
            result = "TRUE";
        }
        return result;
    }

    private static void AddToLog(Mercury m) {
        Logger.AddToLog("<Mercury>");
        Logger.AddToLog(m.netAddr);
        Logger.AddToLog(m.password);
        Logger.AddToLog(m.maxI);
        Logger.AddToLog(m.maxU);
        Logger.AddToLog(m.minI);
        Logger.AddToLog(m.minU);
        Logger.AddToLog(String.valueOf(m.zrU));
        Logger.AddToLog(String.valueOf(m.zrI));
        Logger.AddToLog("Values:");
        Logger.AddToLog(m.currentI1.value);
        Logger.AddToLog(m.currentI2.value);
        Logger.AddToLog(m.currentI3.value);
        Logger.AddToLog(m.currentU1.value);
        Logger.AddToLog(m.currentU2.value);
        Logger.AddToLog(m.currentU3.value);
        Logger.AddToLog("----------------------");
        Logger.AddToLog("----------------------");

        Logger.AddToLog("</Mercury>");
    }

    public static void AddToLog(String s1, byte[] data) {
        String s = "";
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                String h = Integer.toHexString(Global.ToShort(data[i]));
                if (h.length() == 1) {
                    h = "0" + h;
                }
                s = s + h + ", ";
            }
        }
        AddToLog(s1 + ": " + s);
    }

    private static void AddToLog(String s1, int[] data) {
        String s = "";
        for (int i = 0; i < data.length; i++) {
            String h = Integer.toString(data[i]);
            s = s + h + ", ";
        }
        AddToLog(s1 + ": " + s);
    }

    public static void IamWork(String c) {
        System.out.print(c);
        isWorkChar = true;
    }

    public static void ErrToLog(String s) {
        AddToLog(s);
    }
        
    public static void AddToLog(String s) {
        if (isWorkChar) {
            //System.out.println("");
            isWorkChar = false;
        }

        System.out.println(DtService.GetHHMMSS() + ": " + s);
        //LogToServer(s);
        //SendToServer();
    }

    private static void LogToServer(String s) {
        externalLog.addElement(s);
    }

    public static void SendToServer() {
        if (externalLog.size() > 0) {

            String connProfile = Controller.connectionProfile;
            String s = "";

            Vector buferLog = externalLog;
            externalLog = new Vector();

            for (Enumeration e = buferLog.elements(); e.hasMoreElements();) {
                String part = (String) e.nextElement();
                s = s + part + "@";
            }

            HttpConnection hc = null;
            OutputStream os = null;

            try {
                String openParm = "http://grid-rt.azurewebsites.net/externallog/postlogrecord;" + connProfile;
                hc = (HttpConnection) Connector.open(openParm);
                hc.setRequestMethod(HttpConnection.POST);
                hc.setRequestProperty("Content-Length", Integer.toString(s.getBytes().length));
                hc.setRequestProperty("Content-Language", "en-US");
                hc.setRequestProperty("Content-Type", "text");

                os = hc.openOutputStream();
                os.write(s.getBytes());
                int rc = hc.getResponseCode();

                if (rc != HttpConnection.HTTP_OK) {
                    System.out.println("LogToServer: answer no Ok");
                }
                System.out.println("LogToServer: answer Ok");
            } catch (Exception ex) {
                System.out.println("LogToServer: " + ex.getMessage());
            } finally {

                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception ex) {
                        System.out.println("LogToServer: " + ex.getMessage());
                    }
                }

                if (hc != null) {
                    try {
                        hc.close();
                    } catch (Exception ex) {
                        System.out.println("LogToServer: " + ex.getMessage());
                    }
                }
            }
        }
    }
}
