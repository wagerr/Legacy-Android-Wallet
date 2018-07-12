package network;

/**
 * Created by furszy on 6/13/17.
 */

public class PeerData {

    private String host;
    private int tcpPort;
    private int sslPort;
    private long prunningLimit;

    public PeerData(String host, int tcpPort, int sslPort) {
        this.host = host;
        this.tcpPort = tcpPort;
        this.sslPort = sslPort;
    }

    public String getHost() {
        return host;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public int getSslPort() {
        return sslPort;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PeerData)) return false;
        PeerData other = (PeerData) o;
        if (!this.host.equals(other.getHost())){
            return false;
        }
        if (this.tcpPort != other.tcpPort){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PeerData{" +
                "host='" + host + '\'' +
                ", tcpPort=" + tcpPort +
                ", sslPort=" + sslPort +
                ", prunningLimit=" + prunningLimit +
                '}';
    }
}
