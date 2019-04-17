import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.*;

public class Gnutella {

    public final static short DEFAULT_PORT = 6345;
    public final static int MAX_CONNECTIONS = 10;
    public final static int SLEEP_TIME_SECONDS = 5;

    public static void main(String[] args) {
        Gnutella g;

        if (args.length == 1)
            g = new Gnutella(Short.parseShort(args[0]));
        else if (args.length == 2)
            g = new Gnutella(Short.parseShort(args[0]), args[1]);
        else
            g = new Gnutella();
        g.start();
    }

    ArrayList<Peer> peers;
    Ping myPing;

    Gnutella() {
        this.peers = new ArrayList<>();
        this.myPing = new Ping(DEFAULT_PORT, getLocalIP(), 0, 0);
    }

    Gnutella(short port) {
        this.peers = new ArrayList<>();
        this.myPing = new Ping(port, getLocalIP(), 0, 0);
    }

    Gnutella(short port, String addr) {
        this.peers = new ArrayList<>();
        this.myPing = new Ping(port, getLocalIP(), 0, 0);

        String[] split = addr.split(":", 2);
        String IP = split[0];
        short connectPort = Short.parseShort(split[1]);

        peers.add(new Peer(new Ping(connectPort, IP, 0, 0), System.currentTimeMillis()));
    }

    public void start() {
        PingListener pingListener = new PingListener();
        PingSender pingSender = new PingSender();

        pingListener.start();
        pingSender.start();
    }

    private class PingSender extends Thread {

        public PingSender() {
        }

        public void run() {
            while (true) {
                try (DatagramSocket clientSocket = new DatagramSocket()) {
                    for (Peer peer : peers) {
                        if (System.currentTimeMillis() - peer.lastMessage > SLEEP_TIME_SECONDS * 1000 * 3)
                            peers.remove(peer);
                        else {
                            byte[] sendData = myPing.toBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                                    InetAddress.getByName(peer.ping.IP), peer.ping.port);
                            clientSocket.send(sendPacket);

                            System.out.println(peer);
                        }
                    }

                    TimeUnit.SECONDS.sleep(SLEEP_TIME_SECONDS);
                } catch (Exception e) {
                    System.err.println("PingSender Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private class PingListener extends Thread {

        public PingListener() {
        }

        public void run() {
            try {
                DatagramSocket serverSocket = new DatagramSocket(myPing.port);
                byte[] receiveData = new byte[14];

                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    Ping recievedPing = new Ping(receivePacket.getData());
                    ProccessPing pp = new ProccessPing(recievedPing);
                    pp.start();
                }
            } catch (Exception e) {
                System.err.println("PingListener Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private class ProccessPing extends Thread {

        Ping ping;

        public ProccessPing(Ping ping) {
            this.ping = ping;
        }

        public void run() {
            for (Peer peer : peers) {
                if (peer.ping.equals(ping)) {
                    peer.lastMessage = System.currentTimeMillis();
                    return;
                }
            }

            if (peers.size() <= MAX_CONNECTIONS)
                peers.add(new Peer(ping, System.currentTimeMillis()));

            try (DatagramSocket clientSocket = new DatagramSocket()) {
                byte[] sendData = myPing.toBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName(ping.IP), ping.port);
                clientSocket.send(sendPacket);

                sendData = ping.toBytes();
                for (Peer peer : peers) {
                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(peer.ping.IP),
                            peer.ping.port);
                }
            } catch (Exception e) {
                System.err.println("ProccessPing Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static String getLocalIP() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 8080);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            System.err.println("Error getting local IP: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}