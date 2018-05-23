package wang.switchy.an2n.service;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.InetAddress;

import wang.switchy.an2n.event.ErrorEvent;
import wang.switchy.an2n.event.StartEvent;
import wang.switchy.an2n.event.StopEvent;
import wang.switchy.an2n.model.EdgeCmd;
import wang.switchy.an2n.model.EdgeStatus;
import wang.switchy.an2n.model.N2NSettingInfo;

import static wang.switchy.an2n.model.EdgeCmd.getRandomMac;

/**
 * Created by janiszhang on 2018/4/15.
 */

// TODO: 2018/4/18 service 提高到前台

public class N2NService extends VpnService {

    public static N2NService INSTANCE;
//    public static boolean sIsRunning = false;

    private ParcelFileDescriptor mParcelFileDescriptor = null;
    private EdgeCmd cmd;
    private boolean mStartResult;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        Log.e("zhangbz", "N2NService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("zhangbz", "N2NService onStartCommand");

//        intent.getBundleExtra("");
        if (intent != null) {
            Log.e("zhangbz", "intent != null");
        } else {
            Log.e("zhangbz", "intent == null");
        }

        Bundle setting = intent.getBundleExtra("Setting");
//        N2NSettingModel n2nSettingInfo = setting.getParcelable("n2nSettingInfo");
        N2NSettingInfo n2nSettingInfo = setting.getParcelable("n2nSettingInfo");

        Builder b = new Builder();
        Log.e("zhangbz", "n2nSettingInfo = " + n2nSettingInfo.toString());

        Log.e("zhangbz", "mtu = " + n2nSettingInfo.getMtu());
        b.setMtu(n2nSettingInfo.getMtu());

        String ipAddress = n2nSettingInfo.getIp();
        Log.e("zhangbz", "ipAddress = " + ipAddress + "; getIpAddrPrefixLength(n2nSettingInfo.getNetmask()) = " + getIpAddrPrefixLength(n2nSettingInfo.getNetmask()));

        b.addAddress(ipAddress, getIpAddrPrefixLength(n2nSettingInfo.getNetmask()));

//        String[] split = ipAddress.split("\\.");//参数不能直接写成"."
//        String route = split[0] + "." + split[1] + "." + split[2] + ".0";
        String route = getRoute(ipAddress, getIpAddrPrefixLength(n2nSettingInfo.getNetmask()));
        Log.e("zhangbz", "route = " + route);
        b.addRoute(route, getIpAddrPrefixLength(n2nSettingInfo.getNetmask()));

        try {
            mParcelFileDescriptor = b.setSession("N2N_V2S")/*.setConfigureIntent(pendingIntent)*/.establish();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(INSTANCE, "Parameter is not accepted by the operating system.", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(INSTANCE, "Parameter cannot be applied by the operating system.", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }

//        测试代码
//        Builder b = new Builder();
//        b.setMtu(1400);
//        String ipAddress = "192.168.111.2";
//        b.addAddress(ipAddress, 24);
//        b.addRoute("192.168.111.0", 24);
//        mParcelFileDescriptor = b.setSession("N2N_V2S")/*.setConfigureIntent(pendingIntent)*/.establish();

        if (mParcelFileDescriptor != null) {
            Log.e("zhangbz", "mParcelFileDescriptor != null");

        } else {
            Log.e("zhangbz", "mParcelFileDescriptor == null");
            Toast.makeText(INSTANCE, "~error~", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }


        cmd = new EdgeCmd();
        cmd.ipAddr = n2nSettingInfo.getIp();
        cmd.ipNetmask = n2nSettingInfo.getNetmask();
        cmd.supernodes = new String[2];
        cmd.supernodes[0] = n2nSettingInfo.getSuperNode();
        cmd.supernodes[1] = n2nSettingInfo.getSuperNodeBackup();//intent.getStringExtra("spare_super_node");
        cmd.community = n2nSettingInfo.getCommunity();
        cmd.encKey = n2nSettingInfo.getPassword();
        cmd.encKeyFile = null;
        cmd.macAddr = n2nSettingInfo.getMacAddr();//getRandomMac();
        cmd.mtu = n2nSettingInfo.getMtu();
        cmd.localIP = n2nSettingInfo.getLocalIP();
        cmd.holePunchInterval = n2nSettingInfo.getHolePunchInterval();
        cmd.reResoveSupernodeIP = n2nSettingInfo.isResoveSupernodeIP();
        cmd.localPort = n2nSettingInfo.getLocalPort();
        cmd.allowRouting = n2nSettingInfo.isAllowRouting();
        cmd.dropMuticast = n2nSettingInfo.isDropMuticast();
        cmd.traceLevel = n2nSettingInfo.getTraceLevel();//2;
        cmd.vpnFd = mParcelFileDescriptor.detachFd();//????????????

        try {
            // TODO: 2018/4/17 需要判断返回值
            Log.e("zhangbz", "定位！");
            mStartResult = startEdge(cmd);

            Log.e("zhangbz", "mStartResult = " + mStartResult);

            if (mStartResult) {
////                sIsRunning = true;
//                EventBus.getDefault().post(new StartEvent());
            } else {
                EventBus.getDefault().post(new ErrorEvent());
            }

        } catch (Exception e) {
            Log.e("zhangbz", e.getMessage());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void stop() {
        Log.e("zhangbz", "call stop");
        stopEdge();

        /**
         * 这个我好像已经不需要处理了
         */
        try {
            if (mParcelFileDescriptor != null) {
                mParcelFileDescriptor.close();
                mParcelFileDescriptor = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        sIsRunning = false;
        EventBus.getDefault().post(new StopEvent());
//        stopSelf();
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
//        Toast.makeText(INSTANCE, "N2NService onRevoke", Toast.LENGTH_SHORT).show();
        Log.e("zhangbz", "N2NService onRevoke");
        stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("zhangbz", "N2NService onDestroy");
//        stopEdge();
    }

    public native boolean startEdge(EdgeCmd cmd);

    public native void stopEdge();

    public native EdgeStatus getEdgeStatus();

    public void reportEdgeStatus(EdgeStatus status) {
        Log.e("zhangbz", "N2NService reportEdgeStatus");
        if (status != null) {
            if (status.isRunning) {
                EventBus.getDefault().post(new StartEvent());
            } else {
                EventBus.getDefault().post(new StopEvent());

            }
        } else {
            //nothing to do
        }
    }

    private int getIpAddrPrefixLength(String netmask) {
        try {
            byte[] byteAddr = InetAddress.getByName(netmask).getAddress();
            int prefixLength = 0;
            for (int i = 0; i < byteAddr.length; i++) {
                for (int j = 0; j < 8; j++) {
                    if ((byteAddr[i] << j & 0xFF) != 0) {
                        prefixLength++;
                    } else {
                        return prefixLength;
                    }
                }
            }
            return prefixLength;
        } catch (Exception e) {
            return -1;
        }
    }

    private String getRoute(String ipAddr, int prefixLength) {
        byte[] arr = {(byte) 0x00, (byte) 0x80, (byte) 0xC0, (byte) 0xE0, (byte) 0xF0, (byte) 0xF8, (byte) 0xFC, (byte) 0xFE, (byte) 0xFF};

        if (prefixLength > 32 || prefixLength < 0) {
            return "";
        }
        try {
            byte[] byteAddr = InetAddress.getByName(ipAddr).getAddress();
            int idx = 0;
            while (prefixLength >= 8) {
                idx++;
                prefixLength -= 8;
            }
            if (idx < byteAddr.length) {
                byteAddr[idx++] &= arr[prefixLength];
            }
            for (; idx < byteAddr.length; idx++) {
                byteAddr[idx] = (byte) 0x00;
            }
            return InetAddress.getByAddress(byteAddr).getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

}
