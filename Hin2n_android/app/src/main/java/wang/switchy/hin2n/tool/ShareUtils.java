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
    private static final String QQ_GROUP_KEY = "_pG_kr_Or-KpDVCYhG2JhrvyA9wAXRr-";
    private static final String GITHUB_LINK = "https://github.com/switch-iot/hin2n/blob/master/README.md";
    private static final String APP_TITLE = "Hin2n";
    private static final String APP_DESCRIPTION = "N2N is a VPN project that supports p2p.";

    public static boolean joinQQGroup(Activity activity, String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        try {
            activity.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void joinQQGroup(Activity activity) {
        boolean success = joinQQGroup(activity, QQ_GROUP_KEY);
        if (!success) {
            Intent intent = new Intent(activity, WebViewActivity.class);
            intent.putExtra(WebViewActivity.WEB_VIEW_TYPE, WebViewActivity.TYPE_WEB_VIEW_CONTACT);
            activity.startActivity(intent);
        }
    }

    public static void doOnClickShareItem(final Activity activity) {
        UMWeb umWeb = new UMWeb(GITHUB_LINK);
        umWeb.setTitle(APP_TITLE);
        umWeb.setThumb(new UMImage(activity, R.mipmap.ic_launcher));
        umWeb.setDescription(APP_DESCRIPTION);

        new ShareAction(activity)
                .withMedia(umWeb)
                .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
                .setCallback(new UMShareListener() {
                    @Override
                    public void onStart(SHARE_MEDIA platform) {}

                    @Override
                    public void onResult(SHARE_MEDIA platform) {
                        Log.e("zhangbzshare", "onResult");
                    }

                    @Override
                    public void onError(SHARE_MEDIA platform, Throwable t) {
                        Log.e("zhangbzshare", "onError : " + t.getMessage());
                        Intent intent = new Intent(activity, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.WEB_VIEW_TYPE, WebViewActivity.TYPE_WEB_VIEW_SHARE);
                        activity.startActivity(intent);
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA platform) {
                        Log.e("zhangbzshare", "onCancel");
                    }
                }).open();
    }
}
