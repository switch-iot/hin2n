package wang.switchy.an2n.model;

/**
 * Created by janiszhang on 2018/4/16.
 */

public class EdgeCmd {
    public String ipAddr;
    public String ipNetmask;
    public String[] supernodes;
    public String community;
    public String encKey;
    public String encKeyFile;
    public String macAddr;
    public int mtu;
    public String localIP;
    public int holePunchInterval;
    public boolean reResoveSupernodeIP;
    public int localPort;
    public boolean allowRouting;
    public boolean dropMuticast;
    public int traceLevel;
    public int vpnFd;
}
