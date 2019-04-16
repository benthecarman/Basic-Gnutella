public class Peer
{
    Pong pong;
    long lastMessage;

    public Peer(Pong pong, long lastMessage)
    {
        this.pong = pong;
        this.lastMessage = lastMessage;
    }
}