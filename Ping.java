import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Ping {
    short port;
    String IP;
    int numFiles;
    int sizeOfFiles;

    public Ping(short port, String IP, int numFiles, int sizeOfFiles) {
        this.port = port;
        this.IP = IP;
        this.numFiles = numFiles;
        this.sizeOfFiles = sizeOfFiles;
    }

    public Ping(byte[] bytes) throws UnknownHostException
    {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        byte[] ip = new byte[4];
        System.arraycopy(buffer.array(), 2, ip, 0, 4); // Get IP bytes

        this.port = buffer.getShort();
        this.IP = InetAddress.getByAddress(ip).getHostAddress();
        buffer.getInt(); // Move internal pointer ahead
        this.numFiles = buffer.getInt();
        this.sizeOfFiles = buffer.getInt();
    }

    public byte[] toBytes() throws UnknownHostException {
        byte[] bytes = new byte[14];

        ByteBuffer portBuf = ByteBuffer.allocate(2);
        portBuf.putShort(this.port);
        System.arraycopy(portBuf.array(), 0, bytes, 0, 2);

        byte[] ipBuf = InetAddress.getByName(this.IP).getAddress();
        System.arraycopy(ipBuf, 0, bytes, 2, ipBuf.length);

        ByteBuffer numBuf = ByteBuffer.allocate(8);
        numBuf.putInt(this.numFiles);
        numBuf.putInt(this.sizeOfFiles);
        System.arraycopy(numBuf.array(), 0, bytes, 2 + ipBuf.length, numBuf.array().length);

        return bytes;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj.getClass() != this.getClass())
            return false;

        Ping p = (Ping) obj;

        return p.IP.equals(this.IP) && p.port == this.port;
    }

    // for debugging
    public String toString()
    {
        return "Port: " + this.port + " IP: " + this.IP + " numFiles: " + this.numFiles + " sizeOfFiles: " + this.sizeOfFiles;
    }
}