import java.util.ArrayList;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Gnutella {

    public final static int DEFAULT_PORT = 6345;
    public final static int MAX_CONNECTIONS = 10;

    public static void main(String[] args) {

    }

    ArrayList<Peer> peers;
    Pong pong;
    String IP;

    Gnutella() {
        this.peers = new ArrayList<>();
        this.pong = new Pong(DEFAULT_PORT, getLocalIP(), 0, 0);
        this.IP = getLocalIP();
    }

    public void connect(String ip) {
        connect(ip, DEFAULT_PORT);
    }

    public void connect(String ip, int port) {

    }

    private class PingListener extends Thread {

        Pong pong;

        public PingListener(Pong pong) {
            this.pong = pong;
        }

        public void updatePong(Pong pong) {
            this.pong = pong;
        }

        public void run() {
            
        }
    }

    private static String getLocalIP() {
        String ip = "";
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            System.err.println("Error getting local IP: " + e.getMessage());
            e.printStackTrace();
        }

        return ip;
    }
}