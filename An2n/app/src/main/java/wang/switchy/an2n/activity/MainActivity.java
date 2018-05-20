package wang.switchy.an2n.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import wang.switchy.an2n.An2nApplication;
import wang.switchy.an2n.service.N2NService;
import wang.switchy.an2n.R;
import wang.switchy.an2n.event.ErrorEvent;
import wang.switchy.an2n.event.StartEvent;
import wang.switchy.an2n.event.StopEvent;
import wang.switchy.an2n.model.N2NSettingInfo;
import wang.switchy.an2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.an2n.template.BaseTemplate;
import wang.switchy.an2n.template.CommonTitleTemplate;


public class MainActivity extends BaseActivity {

    private Button mActionBtn;

    // TODO: 2018/4/16 暂时先放这，回头再看看放哪更合适
    static {
        System.loadLibrary("slog");
        System.loadLibrary("uip");
        System.loadLibrary("n2n_v2s");
        System.loadLibrary("edge_v2s");

    }

    private N2NSettingModel mCurrentSettingInfo;
    private RelativeLayout mCurrentSettingItem;
    private TextView mCurrentSettingName;

    @Override
    protected BaseTemplate createTemplate() {
        CommonTitleTemplate titleTemplate = new CommonTitleTemplate(this, "Hin2n");
        titleTemplate.mRightImg.setImageResource(R.mipmap.img_add);
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

//        EdgeStatus edgeStatus = new EdgeStatus();
//        N2NService.INSTANCE.getEdgeStatus();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        Log.e("zhangbz", "MainActivity doOnCreate");

        mCurrentSettingItem = (RelativeLayout) findViewById(R.id.rl_current_setting_item);
        mCurrentSettingItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(mContext, "~~onClick~~", Toast.LENGTH_SHORT).show();

                if (N2NService.INSTANCE != null && N2NService.INSTANCE.getEdgeStatus().isRunning) {
                    Toast.makeText(mContext, "~Running~", Toast.LENGTH_SHORT).show();

                } else {
                    startActivity(new Intent(MainActivity.this, ListActivity.class));

                }
            }
        });

        mCurrentSettingName = (TextView) findViewById(R.id.tv_current_setting_name);

        mActionBtn = (Button) findViewById(R.id.btn_action);

        if (N2NService.INSTANCE == null) {
            mActionBtn.setText("start");

        } else {
            if (N2NService.INSTANCE.getEdgeStatus().isRunning) {
                mActionBtn.setText("stop");
            } else {
                mActionBtn.setText("start");
            }
        }

        mActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("zhangbz", "~定位~");

                if (mCurrentSettingName.getText().equals("--null--")) {
                    Toast.makeText(mContext, "null setting", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.e("zhangbz", "~定位~0");

                if (N2NService.INSTANCE != null && N2NService.INSTANCE.getEdgeStatus().isRunning) {
                    Log.e("zhangbz", "~定位~1");
                    N2NService.INSTANCE.stop();
                } else {
                    Log.e("zhangbz", "~定位~2");

                    Intent vpnPrepareIntent = VpnService.prepare(MainActivity.this);
                    Log.e("zhangbz", "~定位~3");

                    if (vpnPrepareIntent != null) {
                        Log.e("zhangbz", "doOnCreate vpnPrepareIntent != null");
                        startActivityForResult(vpnPrepareIntent, 100);
                    } else {
                        Log.e("zhangbz", "doOnCreate vpnPrepareIntent == null");
                        onActivityResult(100, -1, null);

                    }

                }
            }
        });


    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("zhangbz", "onActivityResult requestCode = " + requestCode + "; resultCode = " + resultCode);
        if (requestCode == 100 && resultCode == -1) {//RESULT_OK

            Intent intent = new Intent(MainActivity.this, N2NService.class);


            Bundle bundle = new Bundle();
            N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mCurrentSettingInfo);
            bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
            intent.putExtra("Setting", bundle);

            Log.e("zhangbz", "n2NSettingInfo = " + n2NSettingInfo.toString());

            Log.e("zhangbz", "mtu = " + n2NSettingInfo.getMtu());

            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("zhangbz", "MainActivity onResume");

        SharedPreferences n2nSp = getSharedPreferences("An2n", MODE_PRIVATE);
        Long currentSettingId = n2nSp.getLong("current_setting_id", -1);

        if (currentSettingId != -1) {
            mCurrentSettingInfo = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().load((long) currentSettingId);
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
        mActionBtn.setText("stop");
    }
    
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStopEvent(StopEvent event) {
        mActionBtn.setText("start");
    }
    
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event) {
        mActionBtn.setText("start");
        Toast.makeText(mContext, "~_~Error~_~", Toast.LENGTH_SHORT).show();
    }

}
