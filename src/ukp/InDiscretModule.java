package ukp;

public abstract class InDiscretModule extends OuterModule {

    protected byte channelsCount;

    public InDiscretModule(byte netAddr) {
        super(netAddr);
        outerModuleType = 1;  //DIN
    }

    public byte GetChannelsCount() {
        return channelsCount;
    }

    public abstract byte[] GetChannelsState();

}
