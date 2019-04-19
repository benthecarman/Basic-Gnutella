import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.*;
import java.nio.file.Files;

public class Gnutella {

    public final static short DEFAULT_PORT = 6345;
    public final static int MAX_CONNECTIONS = 40;
    public final static int SLEEP_TIME_SECONDS = 60;
    public static String DEFAULT_DIR = System.getenv("HOME") + "/.gnutella-dir/";

    public static void main(String[] args) throws Exception {
        Gnutella g = new Gnutella();

        for (int i = 0; i < args.length; ++i) {
            try {
                switch (args[i++]) {
                case "--port":
                    g.setPort(Integer.parseInt(args[i]));
                    break;
                case "--connect":
                    g.connect(args[i]);
                    break;
                case "--query":
                    g.addQuery(args[i], Long.parseLong(args[++i]));
                    break;
                case "--dir":
                    g.setDir(args[i]);
                    break;
                default:
                    System.err.println("Invalid argument: " + args[i - 1]);
                    return;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Invalid arguments");
            }
        }

        g.start();
    }

    ArrayList<Peer> peers = new ArrayList<>();
    ArrayList<Integer> servicedQueries = new ArrayList<>();
    ArrayList<File> files = new ArrayList<>();
    ArrayList<Query> queries = new ArrayList<>();

    String dir = DEFAULT_DIR;
    Ping myPing;

    Gnutella() {
        this.myPing = new Ping(DEFAULT_PORT, getLocalIP(), 0, 0);
    }

    public void setPort(int port) {
        myPing.port = (short) port;
    }

    public void connect(String addr) {
        String[] split = addr.split(":", 2);
        String ip = split[0];
        if (ip.equalsIgnoreCase("localhost") || ip.equalsIgnoreCase("127.0.0.1"))
        {
            ip = getLocalIP();
        }
        short connectPort = DEFAULT_PORT;
        if (split.length > 1)
            connectPort = Short.parseShort(split[1]);

        peers.add(new Peer(new Ping(connectPort, ip, 0, 0), System.currentTimeMillis()));
    }

    public void addQuery(String search, long timeToLive) {
        queries.add(new Query(myPing.IP, (short) (myPing.port + queries.size() + 1), search, timeToLive));
    }

    public void setDir(String dir) {
        if (!dir.endsWith("/"))
            dir += "/";
        this.dir = dir;
    }

    public void start() {
        PingListener pingListener = new PingListener();
        PingSender pingSender = new PingSender();
        FileSystem fileSystem = new FileSystem(this.dir);
        ArrayList<QuerySender> querySenders = new ArrayList<>();
        for (Query query : queries) {
            querySenders.add(new QuerySender(query));
        }

        fileSystem.start();
        pingListener.start();
        pingSender.start();

        for (QuerySender querySender : querySenders) {
            querySender.start();
        }
    }

    private class PingSender extends Thread {

        public PingSender() {
        }

        public void run() {
            while (true) {
                try (DatagramSocket clientSocket = new DatagramSocket()) {
                    ArrayList<Peer> toRemovePeers = new ArrayList<>();

                    for (Peer peer : peers) {
                        if (System.currentTimeMillis() - peer.lastMessage > SLEEP_TIME_SECONDS * 1000 * 5)
                            toRemovePeers.add(peer);
                        else {
                            byte[] sendData = myPing.toBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                                    InetAddress.getByName(peer.ping.IP), peer.ping.port);
                            clientSocket.send(sendPacket);
                        }
                    }
                    if (toRemovePeers.size() > 0)
                        peers.removeAll(toRemovePeers);

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
            try (DatagramSocket serverSocket = new DatagramSocket(myPing.port)) {

                while (true) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    byte[] data = receivePacket.getData();
                    if (receivePacket.getLength() == Ping.BASE_PACKET_LENGTH) {
                        Ping recievedPing = new Ping(data);
                        ProccessPing pp = new ProccessPing(recievedPing);
                        pp.start();
                    } else { // Is a query
                        Query recievedQuery = new Query(data);
                        ProccessQuery pq = new ProccessQuery(recievedQuery);
                        pq.start();
                    }
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
            if (ping.equals(myPing))
                return;

            boolean found = false;
            for (Peer peer : peers) {
                if (peer.ping.equals(ping)) {
                    peer.lastMessage = System.currentTimeMillis();
                    peer.ping = ping;
                    found = true;
                }
            }

            if (!found && peers.size() <= MAX_CONNECTIONS)
                peers.add(new Peer(ping, System.currentTimeMillis()));

            try (DatagramSocket clientSocket = new DatagramSocket()) {
                byte[] sendData = myPing.toBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName(ping.IP), ping.port);
                clientSocket.send(sendPacket);

                sendData = ping.toBytes();
                for (Peer peer : peers) {
                    if (peer.ping.equals(ping))
                        continue;
                    sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(peer.ping.IP),
                            peer.ping.port);
                    clientSocket.send(sendPacket);
                }
            } catch (Exception e) {
                System.err.println("ProccessPing Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private class QuerySender extends Thread {

        Query query;

        public QuerySender(Query query) {
            this.query = query;
            servicedQueries.add(query.id);
        }

        public void run() {
            try (DatagramSocket clientSocket = new DatagramSocket()) {
                for (Peer peer : peers) {
                    byte[] sendData = this.query.toBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                            InetAddress.getByName(peer.ping.IP), peer.ping.port);
                    clientSocket.send(sendPacket);
                }
            } catch (Exception e) {
                System.err.println("QuerySender Error: " + e.getMessage());
                e.printStackTrace();
            }

            try (ServerSocket serverSocket = new ServerSocket(query.requestPort)) {
                Socket socket = serverSocket.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());
                int dataLength = in.readInt();
                byte[] data = new byte[dataLength];
                in.read(data);
                FileOutputStream fos = new FileOutputStream(dir + query.searchString);
                fos.write(data);

                fos.close();
                in.close();

                System.out.println("Query " + query.id + " served, " + data.length + " bytes wrote");
            } catch (Exception e) {
                System.err.println("QuerySender Socket Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private class ProccessQuery extends Thread {

        Query query;

        public ProccessQuery(Query query) {
            this.query = query;
        }

        public void run() {
            if (System.currentTimeMillis() - query.timestamp >= query.timeToLive)
                return;
            if (servicedQueries.contains(query.id))
                return;

            servicedQueries.add(query.id);

            for (File file : files) {
                if (query.searchString.equals(file.getName())) {
                    try (Socket socket = new Socket(query.requestIP, query.requestPort)) {
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        out.writeInt(fileContent.length);
                        out.write(fileContent);
                        out.close();
                        socket.close();
                        System.out
                                .println("Serviced query id: " + query.id + ", " + fileContent.length + " bytes sent");
                    } catch (Exception e) {
                        System.err.println("ProccessQuery Socket Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return;
                }
            }
            // File not found
            try (DatagramSocket clientSocket = new DatagramSocket()) {
                byte[] sendData = query.toBytes();

                for (Peer peer : peers) {
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                            InetAddress.getByName(peer.ping.IP), peer.ping.port);
                    clientSocket.send(sendPacket);
                }
            } catch (Exception e) {
                System.err.println("ProccessQuery Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private class FileSystem extends Thread {

        private String folderLocation;

        public FileSystem(String folderLocation) {
            this.folderLocation = folderLocation;
        }

        public void run() {
            File f = new File(folderLocation);
            f.mkdirs();

            while (true) {
                updatePing();
                try {
                    TimeUnit.SECONDS.sleep(SLEEP_TIME_SECONDS);
                } catch (Exception e) {
                    System.err.println("FileSystem Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        public void updatePing(){
            findFiles(folderLocation, files);
            int totalSize = 0;
            for (File file : files) {
                totalSize += file.length();
            }

            myPing.sizeOfFiles = totalSize;
            myPing.numFiles = files.size();
        }

        private void findFiles(String directoryName, List<File> f) {
            File directory = new File(directoryName);

            File[] fList = directory.listFiles();
            if (fList != null) {
                for (File file : fList) {
                    if (file.isFile() && !files.contains(file)) {
                        f.add(file);
                    } else if (file.isDirectory()) {
                        findFiles(file.getAbsolutePath(), f);
                    }
                }
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