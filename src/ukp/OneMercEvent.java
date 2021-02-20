package ukp;

public class OneMercEvent implements ISetAsSend {

    public boolean isNull = true;

    public boolean isSended = false;

    public boolean isReaded = false;

    //public boolean isSaved = false;
    public byte[] body = new byte[13];

    public OneMercEvent() {
        for (int i = 0; i < 13; i++) {
            body[i] = 0;
        }
    }

    public void SetAfterReadStatus() {
        isReaded = true;
        for (int i = 0; i < body.length; i++) {
            if (body[i] != 0) {
                isNull = false;
                isSended = false;
                //isSaved = false;
            }
        }
    }

    public void SetAsSend() {
        isSended = true;
    }
}
