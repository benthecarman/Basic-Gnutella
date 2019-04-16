import java.util.ArrayList;
import java.io.*;
import java.net.*;

public class Gnutella {

    public final static int DEFAULT_PORT = 6345;
    public final static int MAX_CONNECTIONS = 10;
    public final static int SLEEP_TIME_SECONDS = 5;

    public static void main(String[] args) {

    }

    ArrayList<Peer> peers;
    Pong pong;

    Gnutella() {
        this.peers = new ArrayList<>();
        this.pong = new Pong(DEFAULT_PORT, getLocalIP(), 0, 0);
    }

    public void start() {
        PingListener pingListener = new PingListener();
        PingSender pingSender = new PingSender();
    }

    public void connect(String ip) {
        connect(ip, DEFAULT_PORT);
    }

    public void connect(String ip, int port) {

    }

    private class PingSender extends Thread {

        public PingSender() {
        }

        public void run() {
            while (true) {
                for (Peer p : peers) {
                    
                }

                TimeUnit.SECONDS.sleep(SLEEP_TIME_SECONDS);
            }
        }
    }

    private class PingListener extends Thread {

        public PingListener() {
        }

        public void run() {
            DatagramSocket serverSocket = new DatagramSocket(DEFAULT_PORT);
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                System.out.println("RECEIVED: " + sentence);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String capitalizedSentence = sentence.toUpperCase();
                sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }

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