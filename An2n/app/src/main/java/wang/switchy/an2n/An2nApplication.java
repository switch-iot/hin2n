package wang.switchy.an2n;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

/**
 * Created by janiszhang on 2018/4/19.
 */

public class An2nApplication extends MultiDexApplication {

    public Context AppContext;

    @Override
    public void onCreate() {
        super.onCreate();

        AppContext = this;

//        UMConfigure.init(this, "5ad8aba3a40fa373830002f5", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, null);

        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
//        MobclickAgent.setSecret(this, );
    }
}
