package wang.switchy.hin2n.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import wang.switchy.hin2n.Hin2nApplication;
import wang.switchy.hin2n.event.ConnectingEvent;
import wang.switchy.hin2n.event.SupernodeDisconnectEvent;
import wang.switchy.hin2n.service.N2NService;
import wang.switchy.hin2n.R;
import wang.switchy.hin2n.event.ErrorEvent;
import wang.switchy.hin2n.event.StartEvent;
import wang.switchy.hin2n.event.StopEvent;
import wang.switchy.hin2n.model.N2NSettingInfo;
import wang.switchy.hin2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.hin2n.template.BaseTemplate;
import wang.switchy.hin2n.template.CommonTitleTemplate;


public class MainActivity extends BaseActivity {

    private N2NSettingModel mCurrentSettingInfo;
    private RelativeLayout mCurrentSettingItem;
    private TextView mCurrentSettingName;
    private ImageView mConnectBtn;
//    private AVLoadingIndicatorView mLoadingView;
    private TextView mSupernodeDisconnectNote;

    @Override
    protected BaseTemplate createTemplate() {
        CommonTitleTemplate titleTemplate = new CommonTitleTemplate(this, "Hin2n");
        titleTemplate.mRightImg.setImageResource(R.mipmap.ic_add);
        titleTemplate.mRightImg.setVisibility(View.VISIBLE);
        titleTemplate.mRightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingDetailsActivity.class);
                intent.putExtra("type", SettingDetailsActivity.TYPE_SETTING_ADD);
                startActivity(intent);
            }
        });

        return titleTemplate;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

//        mLoadingView = (AVLoadingIndicatorView) findViewById(R.id.loading_view);

        mConnectBtn = (ImageView) findViewById(R.id.iv_connect_btn);

        if (N2NService.INSTANCE == null) {

            mConnectBtn.setImageResource(R.mipmap.ic_state_disconnect);
        } else {
            if (N2NService.INSTANCE.isRunning) {
                mConnectBtn.setImageResource(R.mipmap.ic_state_connect);

            } else {
                mConnectBtn.setImageResource(R.mipmap.ic_state_disconnect);

            }
        }

        mConnectBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mCurrentSettingName.getText().equals("--null--")) {
                    Toast.makeText(mContext, "null setting", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (N2NService.INSTANCE != null && N2NService.INSTANCE.isRunning) {
                    N2NService.INSTANCE.stop();
                } else {

                    Intent vpnPrepareIntent = VpnService.prepare(MainActivity.this);

                    if (vpnPrepareIntent != null) {
                        startActivityForResult(vpnPrepareIntent, 100);
                    } else {
                        onActivityResult(100, -1, null);

                    }

                }
            }
        });

        mSupernodeDisconnectNote = (TextView) findViewById(R.id.tv_supernode_disconnect_note);

        mCurrentSettingItem = (RelativeLayout) findViewById(R.id.rl_current_setting_item);
        mCurrentSettingItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ListActivity.class));

            }
        });

        mCurrentSettingName = (TextView) findViewById(R.id.tv_current_setting_name);


    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == -1) {//RESULT_OK

            Intent intent = new Intent(MainActivity.this, N2NService.class);


            Bundle bundle = new Bundle();
            N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mCurrentSettingInfo);
            bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
            intent.putExtra("Setting", bundle);

            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("zhangbz", "MainActivity onResume");

        SharedPreferences n2nSp = getSharedPreferences("Hin2n", MODE_PRIVATE);
        Long currentSettingId = n2nSp.getLong("current_setting_id", -1);

        if (currentSettingId != -1) {
            mCurrentSettingInfo = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().load((long) currentSettingId);
            if (mCurrentSettingInfo != null) {
                mCurrentSettingName.setText(mCurrentSettingInfo.getName());
            } else {
                mCurrentSettingName.setText("--null--");

            }


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("zhangbz", "MainActivity onPause");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("zhangbz", "MainActivity onDestroy");

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStartEvent(StartEvent event) {
//        mLoadingView.setVisibility(View.GONE);
        mConnectBtn.setVisibility(View.VISIBLE);
        mConnectBtn.setImageResource(R.mipmap.ic_state_connect);
        mSupernodeDisconnectNote.setVisibility(View.GONE);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStopEvent(StopEvent event) {
//        mLoadingView.setVisibility(View.GONE);
        mConnectBtn.setVisibility(View.VISIBLE);
        mConnectBtn.setImageResource(R.mipmap.ic_state_disconnect);
        mSupernodeDisconnectNote.setVisibility(View.GONE);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event) {
        mConnectBtn.setImageResource(R.mipmap.ic_state_disconnect);

        Toast.makeText(mContext, "~_~Error~_~", Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectingEvent(ConnectingEvent event) {
        mConnectBtn.setVisibility(View.GONE);
//        mLoadingView.setVisibility(View.VISIBLE);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSupernodeDisconnectEvent(SupernodeDisconnectEvent event) {
//        mLoadingView.setVisibility(View.GONE);
        mConnectBtn.setImageResource(R.mipmap.ic_state_supernode_diconnect);
        mSupernodeDisconnectNote.setVisibility(View.VISIBLE);
    }

}
