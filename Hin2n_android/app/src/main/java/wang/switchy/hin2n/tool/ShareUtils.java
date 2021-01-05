package wang.switchy.hin2n.tool;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import wang.switchy.hin2n.R;
import wang.switchy.hin2n.activity.WebViewActivity;

public class ShareUtils {
    /****************
     * 发起添加群流程。群号：手机版n2n(hin2n)交流群(769731491) 的 key 为： 5QSK63d7uDivxPW2oCpWHyi7FmE4sAzo
     * 调用 joinQQGroup(5QSK63d7uDivxPW2oCpWHyi7FmE4sAzo) 即可发起手Q客户端申请加群 手机版n2n(hin2n)交流群(769731491)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public static boolean joinQQGroup(Activity activity,String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            activity.startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    public static void joinQQGroup(Activity activity){
        boolean b = joinQQGroup(activity,"5QSK63d7uDivxPW2oCpWHyi7FmE4sAzo");
        if (!b) {
            Intent intent = new Intent(activity, WebViewActivity.class);
            intent.putExtra(WebViewActivity.WEB_VIEW_TYPE, WebViewActivity.TYPE_WEB_VIEW_CONTACT);
            activity.startActivity(intent);
        }
    }



    public static void doOnClickShareItem(final Activity activity) {
        UMWeb umWeb = new UMWeb("https://github.com/switch-iot/hin2n/blob/master/README.md");
        umWeb.setTitle("Hin2n");
        umWeb.setThumb(new UMImage(activity, R.mipmap.ic_launcher));
        umWeb.setDescription("N2N is a VPN project that supports p2p.");

        new ShareAction(activity)
                .withMedia(umWeb)
                .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE/**,SHARE_MEDIA.SINA*/)
                .setCallback(new UMShareListener() {
                    /**
                     * @descrption 分享开始的回调
                     * @param platform 平台类型
                     */
                    @Override
                    public void onStart(SHARE_MEDIA platform) {

                    }

                    /**
                     * @descrption 分享成功的回调
                     * @param platform 平台类型
                     */
                    @Override
                    public void onResult(SHARE_MEDIA platform) {
//                        Toast.makeText(MainActivity.this, "成功了", Toast.LENGTH_LONG).show();

                        Log.e("zhangbzshare", "onResult");
                    }

                    /**
                     * @descrption 分享失败的回调
                     * @param platform 平台类型
                     * @param t 错误原因
                     */
                    @Override
                    public void onError(SHARE_MEDIA platform, Throwable t) {
//                        Toast.makeText(MainActivity.this, "失败" + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("zhangbzshare", "onError : " + t.getMessage());

                        Intent intent = new Intent(activity, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.WEB_VIEW_TYPE, WebViewActivity.TYPE_WEB_VIEW_SHARE);
                        activity.startActivity(intent);

                    }

                    /**
                     * @descrption 分享取消的回调
                     * @param platform 平台类型
                     */
                    @Override
                    public void onCancel(SHARE_MEDIA platform) {
//                        Toast.makeText(MainActivity.this, "取消了", Toast.LENGTH_LONG).show();
                        Log.e("zhangbzshare", "onCancel");
                    }
                }).open();
    }
}
