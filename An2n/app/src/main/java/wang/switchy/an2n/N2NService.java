package wang.switchy.an2n;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import wang.switchy.an2n.model.EdgeCmd;

/**
 * Created by janiszhang on 2018/4/15.
 */

// TODO: 2018/4/18 service 提高到前台

public class N2NService extends VpnService {

    public static N2NService INSTANCE;

    private ParcelFileDescriptor mParcelFileDescriptor = null;
    private Thread mTrd = null;
    private PendingIntent mPendingIntent;

    private EdgeCmd cmd;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        Log.e("zhangbz", "N2NService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("zhangbz", "N2NService onStartCommand");

        Builder b = new Builder();
        b.setMtu(1400 - 14);

        String ipAddress = "192.168.234.2";//intent.getStringExtra("ip_address");
        b.addAddress(ipAddress, 24);

        String[] split = ipAddress.split("\\.");//参数不能直接写成"."
        String route = split[0] + "." + split[1] + "." + split[2] + ".0";
        b.addRoute(route, 24);

        mParcelFileDescriptor = b.setSession("N2N_V2S")/*.setConfigureIntent(pendingIntent)*/.establish();

        cmd = new EdgeCmd();
        cmd.ipAddr = "192.168.234.2";//intent.getStringExtra("ip_address");
        cmd.ipNetmask = intent.getStringExtra("net_mask");
        cmd.supernodes = new String[2];
        cmd.supernodes[0] = "switchy.wang:9000";//intent.getStringExtra("super_node");
        cmd.supernodes[1] = "";//intent.getStringExtra("spare_super_node");
        cmd.community = "switch-comm";//intent.getStringExtra("community");
        cmd.encKey = "0cce3058e9e376f72adda9dc3d45d0fd";//intent.getStringExtra("encrypt");
        cmd.encKeyFile = null;
        cmd.macAddr = "00:11:22:33:44:55";
        cmd.mtu = 1400;
        cmd.localIP = "";
        cmd.holePunchInterval = 25;
        cmd.reResoveSupernodeIP = false;
        cmd.localPort = 0;
        cmd.allowRouting = false;
        cmd.dropMuticast = true;
        cmd.traceLevel = 4;//2;
        cmd.vpnFd = mParcelFileDescriptor.getFd();

        try {
            // TODO: 2018/4/17 需要判断返回值
            startEdge(cmd);
        } catch (Exception e) {
            Log.e("zhangbz", e.getMessage());
        }


//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.e("zhangbz", "call stopEdge");
//                stopEdge();
//
//                try {
//                    if (mParcelFileDescriptor != null) {
//                        mParcelFileDescriptor.close();
//                        mParcelFileDescriptor = null;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 1000 * 15);


        return super.onStartCommand(intent, flags, startId);
    }

    public void stop() {
        Log.e("zhangbz", "call stop");
        stopEdge();

        try {
            if (mParcelFileDescriptor != null) {
                mParcelFileDescriptor.close();
                mParcelFileDescriptor = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        stopSelf();
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
//        Toast.makeText(INSTANCE, "N2NService onRevoke", Toast.LENGTH_SHORT).show();
        Log.e("zhangbz", "N2NService onRevoke");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("zhangbz", "N2NService onDestroy");
//        stopEdge();
    }

    public native boolean startEdge(EdgeCmd cmd);

    public native void stopEdge();


}
