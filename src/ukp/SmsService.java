package ukp;

import java.util.Vector;

public class SmsService {

    public static AtListener atListener;

    public static Vector phones = new Vector();

    public static void SendSms(String phone, String message) {

        String ans = atListener.SendAtCommand("AT+CMGS=\"" + phone + "\"\r");

        if (ans.toUpperCase().indexOf("ERROR") >= 0) {

        } else {
            ans = atListener.SendAtCommand(message + "\32\r");

        }
    }

    public static void MiltiSend(String message) {
        if (phones.size() > 0) {
            for (int i = 0; i < phones.size(); i++) {
                String number = (String)phones.elementAt(i);
                SendSms(number, message);
            }
        }
    }
}
