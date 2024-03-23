package wang.switchy.hin2n.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import wang.switchy.hin2n.Hin2nApplication;
import wang.switchy.hin2n.model.N2NSettingInfo;
import wang.switchy.hin2n.service.N2NService;
import wang.switchy.hin2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.hin2n.tool.ThreadUtils;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        ThreadUtils.mainThreadExecutor(new Runnable() {
            @Override
            public void run() {
                SharedPreferences n2nSp = context.getSharedPreferences("Hin2n", Context.MODE_PRIVATE);
                // If boot is not configured, exit
                if (!n2nSp.getBoolean("start_at_boot", false))
                    return;

                // Get the current Settings, if not, exit
                long currentSettingId = n2nSp.getLong("current_setting_id", -1);
                if (currentSettingId < 0)
                    return;

                Intent vpnPrepareIntent = VpnService.prepare(context);
                if (vpnPrepareIntent != null)
                    return;

                N2NSettingModel mCurrentSettingInfo = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().load((long) currentSettingId);
                if (mCurrentSettingInfo == null)
                    return;

                // Start N2N
                Intent i = new Intent(context, N2NService.class);
                Bundle bundle = new Bundle();
                N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mCurrentSettingInfo);
                bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
                i.putExtra("Setting", bundle);

                ContextCompat.startForegroundService(context,i);
            }
        });
    }
}
