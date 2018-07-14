package wang.switchy.hin2n.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;

import wang.switchy.hin2n.R;
import wang.switchy.hin2n.activity.MainActivity;
import wang.switchy.hin2n.event.ConnectingEvent;
import wang.switchy.hin2n.event.ErrorEvent;
import wang.switchy.hin2n.event.StartEvent;
import wang.switchy.hin2n.event.StopEvent;
import wang.switchy.hin2n.event.SupernodeDisconnectEvent;
import wang.switchy.hin2n.model.EdgeCmd;
import wang.switchy.hin2n.model.EdgeStatus;
import wang.switchy.hin2n.model.N2NSettingInfo;

import static wang.switchy.hin2n.model.EdgeCmd.getRandomMac;
import static wang.switchy.hin2n.model.EdgeStatus.RunningStatus.CONNECTING;
import static wang.switchy.hin2n.model.EdgeStatus.RunningStatus.DISCONNECT;
import static wang.switchy.hin2n.model.EdgeStatus.RunningStatus.SUPERNODE_DISCONNECT;

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
    public boolean isRunning;

    private EdgeStatus.RunningStatus mLastStatus = DISCONNECT;
    private EdgeStatus.RunningStatus mCurrentStatus = DISCONNECT;
    
    private static final int sNotificationId = 1;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        Log.e("zhangbz", "N2NService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("zhangbz", "N2NService onStartCommand");
        if (intent != null) {
            Log.e("zhangbz", "intent != null");
        } else {
            Log.e("zhangbz", "intent == null");
        }

        Bundle setting = intent.getBundleExtra("Setting");
        N2NSettingInfo n2nSettingInfo = setting.getParcelable("n2nSettingInfo");

        Builder b = new Builder();
        b.setMtu(n2nSettingInfo.getMtu());
        String ipAddress = n2nSettingInfo.getIp();
        b.addAddress(ipAddress, getIpAddrPrefixLength(n2nSettingInfo.getNetmask()));

        String route = getRoute(ipAddress, getIpAddrPrefixLength(n2nSettingInfo.getNetmask()));
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


        if (mParcelFileDescriptor != null) {
            Log.e("zhangbz", "mParcelFileDescriptor != null");

        } else {
            Log.e("zhangbz", "mParcelFileDescriptor == null");
            Toast.makeText(INSTANCE, "~error~", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }


        cmd = new EdgeCmd();
        cmd.edgeType = n2nSettingInfo.getVersion();
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
        cmd.dropMuticast = !n2nSettingInfo.isDropMuticast();
        cmd.httpTunnel = n2nSettingInfo.isUseHttpTunnel();
        cmd.traceLevel = n2nSettingInfo.getTraceLevel();//2;
        cmd.vpnFd = mParcelFileDescriptor.detachFd();//????????????

        try {

            mStartResult = startEdge(cmd);
            Log.e("zhangbz", "mStartResult = " + mStartResult);

            if (mStartResult) {

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

        EventBus.getDefault().post(new StopEvent());
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
        Log.e("zhangbz", "N2NService onRevoke");
        stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("zhangbz", "N2NService onDestroy");
    }

    public native boolean startEdge(EdgeCmd cmd);

    public native void stopEdge();

    /**
     * 暂且把SUPERNODE_DISCONNECT和DISCONNECT视为同一种，后续搞清楚再说
     * @param status
     */
    public void reportEdgeStatus(EdgeStatus status) {

        mLastStatus = mCurrentStatus;
        mCurrentStatus = CONNECTING;
        
        switch (status.runningStatus) {
            case CONNECTING:                     // Connecting to N2N network
               
//                Logger.d("reportEdgeStatus CONNECTING");
//                EventBus.getDefault().post(new ConnectingEvent());
                break;
            case CONNECTED:                      // Connect to N2N network successfully
                Logger.d("reportEdgeStatus CONNECTED");
                
                EventBus.getDefault().post(new StartEvent());
                isRunning = true;
                
                if (mLastStatus == SUPERNODE_DISCONNECT) {
                    showOrRemoveNotification(CMD_UPDATE_NOTIFICATION);
                }

                break;
            case SUPERNODE_DISCONNECT:          // Disconnect from the supernode
                Logger.d("reportEdgeStatus SUPERNODE_DISCONNECT");
//                isRunning = false;
                showOrRemoveNotification(CMD_ADD_NOTIFICATION);
                EventBus.getDefault().post(new SupernodeDisconnectEvent());
                break;
            case DISCONNECT:                     // Disconnect from N2N network
                Logger.d("reportEdgeStatus DISCONNECT");
                EventBus.getDefault().post(new StopEvent());
                if (mLastStatus == SUPERNODE_DISCONNECT) {
                    showOrRemoveNotification(CMD_REMOVE_NOTIFICATION);
                }
                isRunning = false;
                break;
            case FAILED:                          // Fail to connect to N2N network
                Logger.d("reportEdgeStatus FAILED");
                isRunning = false;
                EventBus.getDefault().post(new StopEvent());
                if (mLastStatus == SUPERNODE_DISCONNECT) {
                    showOrRemoveNotification(CMD_REMOVE_NOTIFICATION);
                }

                Toast.makeText(INSTANCE, "Fail to connect to N2N network.", Toast.LENGTH_SHORT).show();

                break;

            default:
                break;
        }

    }

    private static final int CMD_REMOVE_NOTIFICATION = 0;
    private static final int CMD_ADD_NOTIFICATION = 1;
    private static final int CMD_UPDATE_NOTIFICATION = 2;
    //supernode连接断开 supernode连接恢复 连接断开/失败--清除通知栏
    private void showOrRemoveNotification(int cmd) {

        switch (cmd) {
            case CMD_REMOVE_NOTIFICATION:

                if (mNotificationManager == null) {
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                }

                mNotificationManager.cancel(sNotificationId);
                break;

            case CMD_ADD_NOTIFICATION:

                Intent mainIntent = new Intent(this, MainActivity.class);
                PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Hin2n")
                        .setContentText("Disconnect from the supernode.")
                        .setFullScreenIntent(null, false)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setAutoCancel(true)
                        .setContentIntent(mainPendingIntent);

                Notification notification = builder.build();
                notification.flags |=Notification.FLAG_NO_CLEAR;

                if (mNotificationManager == null) {
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                }

                mNotificationManager.notify(sNotificationId, notification);
                break;

            case CMD_UPDATE_NOTIFICATION:
                NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Hin2n")
                        .setContentText("Connect to N2N network successfully.")
                        .setFullScreenIntent(null, false)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setAutoCancel(true);

                Notification notification2 = builder2.build();
//                notification2.flags |=Notification.FLAG_NO_CLEAR;


                if (mNotificationManager == null) {
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                }

                mNotificationManager.notify(sNotificationId, notification2);
                break;

            default:
                break;
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
