package wang.switchy.hin2n.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.List;

import wang.switchy.hin2n.Hin2nApplication;
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
import wang.switchy.hin2n.storage.db.base.model.N2NSettingModel;

import static wang.switchy.hin2n.model.EdgeCmd.getRandomMac;
import static wang.switchy.hin2n.model.EdgeStatus.RunningStatus.CONNECTING;
import static wang.switchy.hin2n.model.EdgeStatus.RunningStatus.DISCONNECT;
import static wang.switchy.hin2n.model.EdgeStatus.RunningStatus.SUPERNODE_DISCONNECT;
import static wang.switchy.hin2n.tool.N2nTools.getIpAddrPrefixLength;
import static wang.switchy.hin2n.tool.N2nTools.getRoute;

/**
 * Created by janiszhang on 2018/4/15.
 */

public class N2NService extends VpnService {

    public static N2NService INSTANCE;

    private ParcelFileDescriptor mParcelFileDescriptor = null;
    private EdgeCmd cmd;

    private EdgeStatus.RunningStatus mLastStatus = DISCONNECT;
    private EdgeStatus.RunningStatus mCurrentStatus = DISCONNECT;

    private static final int sNotificationId = 1;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            EventBus.getDefault().post(new ErrorEvent());
            return super.onStartCommand(intent, flags, startId);
        }

        Bundle setting = intent.getBundleExtra("Setting");
        N2NSettingInfo n2nSettingInfo = setting.getParcelable("n2nSettingInfo");

        Builder builder = new Builder()
                .setMtu(n2nSettingInfo.getMtu())
                .addAddress(n2nSettingInfo.getIp(), getIpAddrPrefixLength(n2nSettingInfo.getNetmask()))
                .addRoute(getRoute(n2nSettingInfo.getIp(), getIpAddrPrefixLength(n2nSettingInfo.getNetmask())), getIpAddrPrefixLength(n2nSettingInfo.getNetmask()));

        if(!n2nSettingInfo.getGatewayIp().isEmpty())
            builder.addRoute("0.0.0.0", 1);

        String session = getResources().getStringArray(R.array.vpn_session_name)[n2nSettingInfo.getVersion()];
        try {
            mParcelFileDescriptor = builder.setSession(session).establish();
        } catch (IllegalArgumentException e) {
            Toast.makeText(INSTANCE, "Parameter is not accepted by the operating system.", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        } catch (IllegalStateException e) {
            Toast.makeText(INSTANCE, "Parameter cannot be applied by the operating system.", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }

        if (mParcelFileDescriptor == null) {
            EventBus.getDefault().post(new ErrorEvent());
            return super.onStartCommand(intent, flags, startId);
        }

        cmd = new EdgeCmd(n2nSettingInfo, mParcelFileDescriptor.detachFd(), getExternalFilesDir("log") + "/" + session + ".log");
        try {
            if (!startEdge(cmd)) {
                EventBus.getDefault().post(new ErrorEvent());
            }
        } catch (Exception e) {
            EventBus.getDefault().post(new ErrorEvent());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void stop() {
        stopEdge();
        mLastStatus = mCurrentStatus = DISCONNECT;
        showOrRemoveNotification(CMD_REMOVE_NOTIFICATION);

        try {
            if (mParcelFileDescriptor != null) {
                mParcelFileDescriptor.close();
                mParcelFileDescriptor = null;
            }
        } catch (IOException e) {
            EventBus.getDefault().post(new ErrorEvent());
            return;
        }

        EventBus.getDefault().post(new StopEvent());
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
        stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    public native boolean startEdge(EdgeCmd cmd);

    public native void stopEdge();

    public void reportEdgeStatus(EdgeStatus status) {
        mLastStatus = mCurrentStatus;
        mCurrentStatus = status.runningStatus;

        if (mLastStatus == mCurrentStatus) {
            return;
        }

        switch (status.runningStatus) {
            case CONNECTING:
            case CONNECTED:
                EventBus.getDefault().post(new StartEvent());
                if (mLastStatus == SUPERNODE_DISCONNECT) {
                    showOrRemoveNotification(CMD_UPDATE_NOTIFICATION);
                }
                break;
            case SUPERNODE_DISCONNECT:
                showOrRemoveNotification(CMD_ADD_NOTIFICATION);
                EventBus.getDefault().post(new SupernodeDisconnectEvent());
                break;
            case DISCONNECT:
                EventBus.getDefault().post(new StopEvent());
                if (mLastStatus == SUPERNODE_DISCONNECT) {
                    showOrRemoveNotification(CMD_REMOVE_NOTIFICATION);
                }
                break;
            case FAILED:
                EventBus.getDefault().post(new StopEvent());
                if (mLastStatus == SUPERNODE_DISCONNECT) {
                    showOrRemoveNotification(CMD_REMOVE_NOTIFICATION);
                }
                break;
            default:
                break;
        }
    }

    public EdgeStatus.RunningStatus getCurrentStatus() {
        return mCurrentStatus;
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

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id_default))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_state_supernode_diconnect))
                        .setColor(ContextCompat.getColor(this, R.color.colorSupernodeDisconnect))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notify_disconnect))
                        .setFullScreenIntent(null, false)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setAutoCancel(false)
                        .setContentIntent(mainPendingIntent);

                Notification notification = builder.build();
                notification.flags |= Notification.FLAG_NO_CLEAR;
                if (mNotificationManager == null) {
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                }
                mNotificationManager.notify(sNotificationId, notification);
                break;
            case CMD_UPDATE_NOTIFICATION:
                Intent mainIntent1 = new Intent(this, MainActivity.class);
                PendingIntent mainPendingIntent1 = PendingIntent.getActivity(this, 0, mainIntent1, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id_default))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notify_reconnected))
                        .setFullScreenIntent(null, false)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setAutoCancel(true)
                        .setContentIntent(mainPendingIntent1);

                Notification notification2 = builder2.build();
                if (mNotificationManager == null) {
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                }
                mNotificationManager.notify(sNotificationId, notification2);
                break;
            default:
                break;
        }
    }
}
