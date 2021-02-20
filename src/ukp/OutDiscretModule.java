package ukp;

public abstract class OutDiscretModule extends OuterModule {

    protected byte channelsCount;  //количество каналов

    public OutDiscretModule(byte netAddr) {
        super(netAddr);
        outerModuleType = 2;  //DOUT
    }

    public byte GetChannelsCount() {
        return channelsCount;
    }

    public abstract boolean SetChannelState(int channel, int state);
}
