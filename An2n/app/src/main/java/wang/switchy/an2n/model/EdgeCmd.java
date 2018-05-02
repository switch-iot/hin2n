package wang.switchy.an2n.model;

import java.util.Vector;

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

    public boolean checkValues(Vector<String> invalids) {
        if (invalids == null) {
            invalids = new Vector<String>();
        }
        invalids.clear();
        if (!checkIPV4(ipAddr)) {
            invalids.add("ipAddr");
        }
        if (!checkIPV4(ipNetmask)) {
            invalids.add("ipAddr");
        }
        if (supernodes == null || !checkSupernode(supernodes[0]) ||
                (supernodes[1] != null && !supernodes[1].isEmpty() && !checkSupernode(supernodes[1]))) {
            invalids.add("(backup)supernode");
        }
        if (!checkCommunity(community)) {
            invalids.add("community");
        }
        if (!checkEncKey(encKey)) {
            invalids.add("encKey");
        }
        if (!checkEncKeyFile(encKeyFile)) {
            invalids.add("encKeyFile");
        }
        if (!checkMacAddr(macAddr)) {
            invalids.add("macAddr");
        }
        if (!checkInt(mtu, 64, 65535)) {
            invalids.add("mut");
        }
        if (localIP != null && !localIP.isEmpty() && !checkIPV4(localIP)) {
            invalids.add("localIP");
        }
        if (!checkInt(holePunchInterval, 10, 120)) {
            invalids.add("holePunchInterval");
        }
        if (!checkInt(localPort, 0, 65535)) {
            invalids.add("localPort");
        }
        if (!checkInt(traceLevel, 0, 4)) {
            invalids.add("traceLevel");
        }
        if (!checkInt(vpnFd, 0, 65535)) {
            invalids.add("traceLevel");
        }

        return invalids.size() == 0;
    }

    private boolean checkIPV4(String ip) {
        if (ip == null || ip.length() < 7 || ip.length() > 15) {
            return false;
        }
        String[] split = ip.split("\\.");
        if (split == null || split.length != 4) {
            return false;
        }
        try {
            for (int i = 0; i < split.length; ++i) {
                int n = Integer.parseInt(split[i]);
                if (n < 0 || n > 255 || String.valueOf(n) != split[i]) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private boolean checkSupernode(String supernode) {
        if (supernode == null || supernode.isEmpty() || supernode.length() > 47) {
            return false;
        }
        String[] split = supernode.split(":");
        if (split == null || split.length != 2 || split[0].isEmpty()) {
            return false;
        }
        int n = Integer.parseInt(split[1]);
        if (n < 0 || n > 65535 || String.valueOf(n) != split[1]) {
            return false;
        }

        return true;
    }

    private boolean checkCommunity(String community) {
        if (community == null || community.isEmpty() || community.length() > 15) {
            return false;
        }

        return true;
    }

    private boolean checkEncKey(String encKey) {
        return true;
    }

    private boolean checkEncKeyFile(String encKeyFile) {
        return true;
    }

    private boolean checkMacAddr(String mac) {
        String hex = "0123456789abcdef";
        if (mac == null || mac.length() != 17) {
            return false;
        }
        for (int i = 0; i < mac.length(); ++i) {
            char c = mac.charAt(i);
            if ((i + 1) % 3 == 0) {
                if (c != ':') {
                    return false;
                }
                continue;
            }
            if (!hex.contains(String.valueOf(c))) {
                return false;
            }
        }

        return true;
    }

    private boolean checkInt(int n, int min, int max) {
        if (mtu < min || mtu > max) {
            return false;
        }

        return true;
    }

}
