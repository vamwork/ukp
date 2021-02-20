package ukp;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.UDPDatagramConnection;

public class SntpClient {

    private String profile;
    private String apn_accesspoint = "";
    private static UDPDatagramConnection udpc;
        
    public double queryNtpServer(String servername) throws IOException {
        double result = 0;
        String serverName = servername;
        profile = "datagram://" + serverName + ":123;dns=0.0.0.0;" + apn_accesspoint + ";";        
        udpc = (UDPDatagramConnection) Connector.open(profile);
        byte[] buf = new NtpMessage().toByteArray();
        Datagram packet = udpc.newDatagram(buf, buf.length);

        NtpMessage.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);

        udpc.send(packet);

        packet = udpc.newDatagram(buf, buf.length);
        udpc.receive(packet);

        // Immediately record the incoming timestamp
        double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;
        
        NtpMessage msg = new NtpMessage(packet.getData());
                
        double roundTripDelay = (destinationTimestamp - msg.originateTimestamp) - (msg.transmitTimestamp - msg.receiveTimestamp);

        double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;
        
        result = localClockOffset;
        udpc.close();
        return result;
    }

    public void setApn_accesspoint(String apn_accesspoint) {
        this.apn_accesspoint = apn_accesspoint;
    }
}
