package ukp;

import javax.microedition.midlet.*;
import java.util.Date;
import com.cinterion.io.ATCommand;
import com.cinterion.misc.Watchdog2;
import java.util.Timer;
import java.util.TimerTask;

public class IMlet extends MIDlet {

    public Task task;
    ATCommand atc;
    String ans = "";
    public static Date currentDate;
    public AtListener atListener;
    Watchdog2 wd;

    public void startApp() {
        try {
            atc = new ATCommand(false);
            atc.send("at+cfun=1\r");
        } catch (Exception e) {
            Logger.ErrToLog("*Midlet[0001]" + e.toString());
        }

        // подготовительные операции
        String ans;
        byte[] simCid = new byte[20];
        byte[] serialNumber = new byte[15];
        boolean localMode = false; // флаг "локального" режима
        try {
            atListener = new AtListener();

            Dialer.atListener = atListener; // atListener для "дозвонщика"
            SmsService.atListener = atListener;

            //Настроить WD
            ans = atListener.SendAtCommand("AT^SCFG=\"Userware/Watchdog\",\"1\"\r");
            Logger.AddToLog("WD " + ans);
            try {
                Logger.AddToLog("WD1");
                wd = new Watchdog2();
                Logger.AddToLog("WD2");
                wd.start(240);
            } catch (Exception ex) {
                Logger.AddToLog("WD error:" + ex.toString());
            }

            // отключить режим ЭХО
            ans = atListener.SendAtCommand("ATE0\r");
            // для того, что бы работал ats0=n, необходимо отключить привязку
            // текстовый режим SMS
            ans = atListener.SendAtCommand("AT+CMGF=1\r");

            // узнаем simCid & serialNumber
            Logger.AddToLog("Check sim card...");
            ans = atListener.SendAtCommand("AT+CCID\r");
            Logger.AddToLog("Sim card is " + ans);
            if (ans.toUpperCase().indexOf("ERROR") >= 0) {
                // SIM-карта не установлена
                localMode = true;
            } else {
                System.arraycopy(Global.StringToBytes(ans), 9, simCid, 0, 20);
                ans = atListener.SendAtCommand("AT+CGSN\r");
                System.arraycopy(Global.StringToBytes(ans), 2, serialNumber, 0, 15);
                // брать трубку после первого звонка
                ans = atListener.SendAtCommand("ATS0=1\r");
                //Только GSM 2G
                ans = atListener.SendAtCommand("at^sxrat=0\r");

            }
        } catch (Exception e) {
            Logger.ErrToLog("*Midlet[0002]" + e.toString());
        }

        Controller.isLocalMode = localMode;

        System.out.println("Start application (" + Global.ToString(Controller.version) + ").");

        Logger.AddToLog(System.getProperty("microedition.commports"));
        Logger.AddToLog("------------------------------------------------");
        Logger.AddToLog("Start application (" + Global.ToString(Controller.version) + ").");
        Logger.AddToLog("Free memory: " + String.valueOf(Controller.runtime.freeMemory() / 1024) + " Kbyte");
        Logger.AddToLog("Total memory: " + String.valueOf(Controller.runtime.totalMemory() / 1024) + " Kbyte");
        Logger.AddToLog("------------------------------------------------");

        try {
            Controller.atc = new ATCommand(false);
            Controller.atc.send("AT^SPIO=1\r");
            Controller.atc.send("at^scpin=1,4,1,0\r");
        } catch (Exception e) {
            Logger.ErrToLog("*Midlet[0006]" + e.toString());
        }

        try {
            //TODO            
            //Controller.timeIsSynchro = !(NoveltiTimer.GetTime(true) == null);
        } catch (Exception ex) {
            Logger.ErrToLog("*Midlet[0007]" + ex.toString());
        }

//        Controller.LoadPhones();        
//        SmsService.MiltiSend("CONTROLLER IN WORK.");
        Controller.LoadConfig();
        Controller.LoadShedule();
        Mercury.LoadConfig();
        Merc200.LoadConfig();
        SwitchJournal.LoadJournal();
        AnJournal.Load(); // прочитаем предыдущие события аналогового журнала

        task = new Task();

        // если карты нет, переходим в режим работы по com-порту
        if (localMode) {
            // режим без SIM-карты
            Logger.AddToLog("No sim mode");
            for (;;) {
                try {
                    Controller.cp.PortListen();
                } catch (Exception e) {
                    Logger.ErrToLog("*Midlet[0003]" + e.toString());
                }
            }
        } else {
            Logger.AddToLog("Start app");
            // собственно это и есть запуск приложения
            //int toNetworkTestCounter = 0;
            //int networkErrorCounter = 0;

            int gsmCounter = 0;
            //int rebootCounter = 0;

            long prMillis = System.currentTimeMillis();

            for (;;) {
                try {

                    if (System.currentTimeMillis() > (prMillis + 2000)) {
                        Controller.rebootCounter++;
                        gsmCounter++;

                        //TODO
                        //I2CService.GetDisState();
                        prMillis = System.currentTimeMillis();

                        Logger.IamWork("_" + Integer.toString(Controller.rebootCounter));

                        boolean isWork = true;

                        if (!Service.isAnySendSuccess && Controller.rebootCounter > 240 && Controller.destHost.length() > 15) {
                            isWork = false;
                            Logger.AddToLog("isWork = false 1");
                        }

                        if (!Service.isAnySendSuccess && Controller.rebootCounter > 2400 && Controller.destHost.length() < 15) {
                            isWork = false;
                            Logger.AddToLog("isWork = false 2");
                        }

                        if (Service.isAnySendSuccess && Service.sendErorCount > 5) {
                            isWork = false;
                            Logger.AddToLog("isWork = false 3");
                        }

                        if (Service.sendErorCount > 25) {
                            isWork = false;
                            Logger.AddToLog("isWork = false 4");
                        }
                        
                        if (isWork) {
                            Logger.IamWork("w");
                            wd.kick();
                        } else {
                            Logger.IamWork("r");
                        }

                        if (gsmCounter > 1000) {
//                            ans = atListener.SendAtCommand("AT+CFUN=4\r");
//                            Logger.AddToLog("AT+CFUN=4 " + ans);
//                            Global.Delay(3000);
//
//                            ans = atListener.SendAtCommand("AT+CFUN=1\r");
//                            Logger.AddToLog("AT+CFUN=1 " + ans);
//
//                            Global.Delay(5000);
//
//                            atListener.SendAtCommand("at^sxrat=0\r");
//                            Logger.AddToLog("at^sxrat=0 " + ans);
                            gsmCounter = 0;
                        }

                        if (task.RunTask()) {
                            // Перерегистрация в сети                            
                        }
                    }
                } catch (Exception e) {
                    // ничего не делаем
                    Logger.ErrToLog("*Midlet[0005]" + e.toString());
                }
            }
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        wd.start(0);
        notifyDestroyed();
    }
}
