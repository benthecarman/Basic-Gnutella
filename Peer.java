public class Peer {
    Ping ping;
    long lastMessage;

    public Peer(Ping ping, long lastMessage) {
        this.ping = ping;
        this.lastMessage = lastMessage;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj.getClass() != this.getClass())
            return false;

        Peer p = (Peer) obj;

        return p.ping.equals(this.ping);
    }

    public String toString()
    {
        return "Ping: " + this.ping.toString() + "\nLast Message: " + this.lastMessage;
    }
}