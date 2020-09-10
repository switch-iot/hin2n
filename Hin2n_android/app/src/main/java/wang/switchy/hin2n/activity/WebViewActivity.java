package wang.switchy.hin2n.activity;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.orhanobut.logger.Logger;
import com.wang.avi.AVLoadingIndicatorView;

import wang.switchy.hin2n.R;
import wang.switchy.hin2n.template.BaseTemplate;
import wang.switchy.hin2n.template.CommonTitleTemplate;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.changeAction;

/**
 * Created by janiszhang on 2018/6/25.
 */

public class WebViewActivity extends BaseActivity {

    public static final String WEB_VIEW_TYPE = "web_view_type";

    public static final int TYPE_WEB_VIEW_ABOUT = 0;
    public static final int TYPE_WEB_VIEW_FEEDBACK = 1;
    public static final int TYPE_WEB_VIEW_SHARE = 2;
    public static final int TYPE_WEB_VIEW_CONTACT = 3;

    public static final String ABOUT_URL = "https://github.com/switch-iot/hin2n/blob/dev_android/README.md";
    public static final String SHARE_URL = "https://github.com/switch-iot/hin2n/wiki/Welcome-to-hin2n";
    public static final String CONTACT_URL = "https://github.com/switch-iot/hin2n/wiki/Feedback-&-Contact-Us";
    public static final String FEEDBACK_URL = "https://support.qq.com/products/38470";

    private WebView mWebView;
    private AVLoadingIndicatorView mLoadingView;
    private CommonTitleTemplate mCommonTitleTemplate;

    @Override
    protected BaseTemplate createTemplate() {
        mCommonTitleTemplate = new CommonTitleTemplate(mContext, "About");

        mCommonTitleTemplate.mLeftImg.setVisibility(View.VISIBLE);
        mCommonTitleTemplate.mLeftImg.setImageResource(R.drawable.titlebar_icon_return_selector);
        mCommonTitleTemplate.mLeftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    finish();
                }
            }
        });
        return mCommonTitleTemplate;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {

        mLoadingView = (AVLoadingIndicatorView) findViewById(R.id.loading_view);

        mWebView = (WebView) findViewById(R.id.web_view);

        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mLoadingView.setVisibility(View.VISIBLE);

                if (mWebView != null) {
                    mWebView.setVisibility(View.GONE);

                }

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                mLoadingView.setVisibility(View.GONE);
                if (mWebView != null) {
                    mWebView.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        int webViewType = getIntent().getIntExtra(WEB_VIEW_TYPE, -1);

        switch (webViewType) {
            case TYPE_WEB_VIEW_ABOUT:
                mCommonTitleTemplate.setTitleText("About");
                mWebView.loadUrl(ABOUT_URL);
                break;
            case TYPE_WEB_VIEW_FEEDBACK:
                mCommonTitleTemplate.setTitleText("Feedback");
                mWebView.loadUrl(FEEDBACK_URL);
                break;
            case TYPE_WEB_VIEW_SHARE:
                mCommonTitleTemplate.setTitleText("Share");
                mWebView.loadUrl(SHARE_URL);
                break;
            case TYPE_WEB_VIEW_CONTACT:
                mCommonTitleTemplate.setTitleText("Contact");
                mWebView.loadUrl(CONTACT_URL);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();

            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_about;
    }
}
