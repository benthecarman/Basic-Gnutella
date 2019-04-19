import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Random;

public class Query {

    public final static int BASE_PACKET_LENGTH = 30;
    
    String requestIP;
    short requestPort;
    String searchString;
    int id;
    long timeToLive;
    long timestamp;

    public Query(String requestIP, short requestPort, String searchString, long timeToLive) {
        this.requestIP = requestIP;
        this.requestPort = requestPort;
        this.searchString = searchString;
        this.timeToLive = timeToLive;
        this.timestamp = System.currentTimeMillis();
        this.id = new Random().nextInt();
        if (this.id < 0)
            this.id *= -1;
    }

    public Query(byte[] bytes) throws UnknownHostException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        this.requestPort = buffer.getShort();
        byte[] ip = new byte[4];
        buffer.get(ip);
        this.requestIP = InetAddress.getByAddress(ip).getHostAddress();
        this.id = buffer.getInt();
        this.timeToLive = buffer.getLong();
        this.timestamp = buffer.getLong();
        int strlen = buffer.getInt();
        byte[] str = new byte[strlen];
        buffer.get(str);
        this.searchString = new String(str);
    }

    public byte[] toBytes() throws UnknownHostException {
        byte[] bytes = new byte[BASE_PACKET_LENGTH + this.searchString.length()];

        ByteBuffer portBuf = ByteBuffer.allocate(2);
        portBuf.putShort(this.requestPort);
        System.arraycopy(portBuf.array(), 0, bytes, 0, portBuf.capacity());

        byte[] ipBuf = InetAddress.getByName(this.requestIP).getAddress();
        System.arraycopy(ipBuf, 0, bytes, 2, ipBuf.length);

        ByteBuffer numBuf = ByteBuffer.allocate(4 + 8 + 8 + 4);
        numBuf.putInt(this.id);
        numBuf.putLong(this.timeToLive);
        numBuf.putLong(this.timestamp);
        numBuf.putInt(this.searchString.length());
        System.arraycopy(numBuf.array(), 0, bytes, portBuf.capacity() + ipBuf.length, numBuf.capacity());
        System.arraycopy(this.searchString.getBytes(), 0, bytes, portBuf.capacity() + ipBuf.length + numBuf.capacity(), this.searchString.length());

        return bytes;
    }

    public String toString() {
        String ret = id + "\n";
        ret += requestPort + "\n";
        ret += requestIP + "\n";
        ret += searchString + "\n";
        ret += timeToLive + "\n";
        ret += timestamp + "\n";

        return ret;
    }
}