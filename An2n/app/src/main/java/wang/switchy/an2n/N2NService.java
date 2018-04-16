package wang.switchy.an2n;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import wang.switchy.an2n.model.EdgeCmd;

import static android.os.Build.VERSION_CODES.N;

/**
 * Created by janiszhang on 2018/4/15.
 */

public class N2NService extends VpnService {

    private ParcelFileDescriptor mParcelFileDescriptor = null;
    private Thread mTrd = null;
    private PendingIntent mPendingIntent;

    private EdgeCmd cmd;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("zhangbz", "N2NService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("zhangbz", "N2NService onStartCommand");

        Builder b = new Builder();
        b.setMtu(1400 - 14);

        String ipAddress = intent.getStringExtra("ip_address");
        b.addAddress(ipAddress, 24);

        String[] split = ipAddress.split(".");
        String route = split[0] + "." + split[1] + "." + split[2] + ".0";
        b.addRoute(route, 24);

        mParcelFileDescriptor = b.setSession("N2N_V2S")/*.setConfigureIntent(pendingIntent)*/.establish();

        cmd = new EdgeCmd();
        cmd.ipAddr = intent.getStringExtra("ip_address");
        cmd.ipNetmask = intent.getStringExtra("net_mask");
        cmd.supernodes = new String[2];
        cmd.supernodes[0] = intent.getStringExtra("super_node");
        cmd.supernodes[1] = intent.getStringExtra("spare_super_node");
        cmd.community = intent.getStringExtra("community");
        cmd.encKey = intent.getStringExtra("encrypt");
        cmd.encKeyFile = null;
        cmd.macAddr = "00:11:22:33:44:55";
        cmd.mtu = 1400;
        cmd.localIP = "";
        cmd.holePunchInterval = 25;
        cmd.reResoveSupernodeIP = false;
        cmd.localPort = 0;
        cmd.allowRouting = false;
        cmd.dropMuticast = true;
        cmd.traceLevel = 2;
        cmd.vpnFd = mParcelFileDescriptor.getFd();

        try {
            startEdge(cmd);
        } catch (Exception e) {
            Log.e("zhangbz", e.getMessage());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("zhangbz", "N2NService onDestroy");
    }

    public native boolean startEdge(EdgeCmd cmd);
    public native void stopEdge();
}
