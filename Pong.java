public class Pong
{
    int port;
    String IP;
    int numFiles;
    long sizeOfFiles;

    public Pong (int port, String IP, int numFiles, long sizeOfFiles)
    {
        this.port = port;
        this.IP = IP;
        this.numFiles = numFiles;
        this.sizeOfFiles = sizeOfFiles;
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (obj.getClass() != this.getClass())
            return false;
        
        Pong p = (Pong) obj;

        return p.IP.equals(this.IP) && p.port == this.port;
    }
}