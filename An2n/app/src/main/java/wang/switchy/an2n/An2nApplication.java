package wang.switchy.an2n;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.multidex.MultiDexApplication;

import wang.switchy.an2n.storage.db.base.DaoMaster;
import wang.switchy.an2n.storage.db.base.DaoSession;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;


/**
 * Created by janiszhang on 2018/4/19.
 */

public class An2nApplication extends MultiDexApplication {

    public Context AppContext;

    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    //静态单例
    public static An2nApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        AppContext = this;

        setDatabase();

//        UMConfigure.init(this, "5ad8aba3a40fa373830002f5", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, null);

        // TODO: 2018/5/10 不知道问什么有问题了。。
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);

//        MobclickAgent.setSecret(this, );


//        startService(new Intent(this, N2NService.class));
    }

    public static An2nApplication getInstance(){
        return instance;
    }

    /**
     * 设置greenDao
     */
    private void setDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        mHelper = new DaoMaster.DevOpenHelper(this, "N2N-db", null);
        db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }
}

