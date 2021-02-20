package ukp;

import com.cinterion.io.ATCommand;
import com.cinterion.io.ATCommandFailedException;
import com.cinterion.io.ATCommandListener;

public class AtListener implements ATCommandListener {

    //private int waitMsec = 15; // время "ожидания" пакета
    //private static final long GSM_WAIT = 30000; // время неактивности канала GSM
    //private InputStream dataIn;
    //private OutputStream dataOut;
    private InConnThread inConnThread;

    public ATCommand atc;

    public AtListener() {
        try {
            atc = new ATCommand(true);
            atc.addListener(this);
        } catch (ATCommandFailedException e) {
            Logger.ErrToLog("*AtListener[0001]" + e.toString());
        }
    }

    public String SendAtCommand(String cmd) {
        String answer = "";
        try {
            answer = atc.send(cmd);
        } catch (Exception e) {
            Logger.ErrToLog("*AtListener[0002]" + e.toString());
        }
        return answer;
    }

    public void ATEvent(String Event) {
//        if (Event.indexOf("CUSD") > 0) {
//            Controller.timeStampOfBalans = Global.GetDateTime();
//            Controller.toSendOfBalans = true;
//        }
    }

    public void RINGChanged(boolean SignalState) {

    }

    public void DCDChanged(boolean SignalState) {

    }

    public void DSRChanged(boolean SignalState) {

    }

    public void CONNChanged(boolean SignalState) {
        try {
            if (SignalState) {
                inConnThread = new InConnThread(atc);
                Task.isChannel = true;
                inConnThread.start();                               
            }
        } catch (Exception ex) {
            Logger.ErrToLog("*AtListener[0001]" + ex.toString());
        }
    }
}
