package wang.switchy.hin2n.tool;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.TypedValue;

/**
 * Created by janiszhang on 2018/5/23.
 */

public class N2nTools {
    public static final String MetaUmengAppKey = "UMENG_APPKEY";
    public static final String MetaUmengChannel = "UMENG_CHANNEL";
    public static final String MetaBuglyAppId = "BUGLY_APPID";
    public static final String MetaShareWxAppId = "SHARE_WX_APPID";
    public static final String MetaShareWxAppSecret = "SHARE_WX_APPSECRET";


    public static int dp2px(Context context, int dp) {

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static String getMetaData(Context context, String key) {
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.getString(key, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
