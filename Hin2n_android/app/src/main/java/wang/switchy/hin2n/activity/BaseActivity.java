package wang.switchy.hin2n.activity;

import android.app.Activity;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;

import wang.switchy.hin2n.template.BaseTemplate;

/**
 * Created by janiszhang on 2018/4/13.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected Activity mContext;

    /**
     * 页面的内容视图（除标题栏）
     */
    protected View mContentView;

    /**
     * 当前页面的模版
     */
    protected BaseTemplate mTemplate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        mContext = this;
        mTemplate = createTemplate();
        if (mTemplate != null) {
            if (getContentLayout() != 0) {
                mContentView = LayoutInflater.from(mContext).inflate(getContentLayout(), null);
                mTemplate.setContentView(mContentView);
            }
            setContentView(mTemplate.getPageView());
        }

        doOnCreate(savedInstanceState);
    }

    protected abstract BaseTemplate createTemplate();

    protected abstract void doOnCreate(Bundle savedInstanceState);

    protected abstract int getContentLayout();


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
