package wang.switchy.hin2n.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.tencent.bugly.beta.Beta;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;
import com.zhy.m.permission.ShowRequestPermissionRationale;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.pedant.SweetAlert.SweetAlertDialog;
import wang.switchy.hin2n.Hin2nApplication;
import wang.switchy.hin2n.R;
import wang.switchy.hin2n.event.ConnectingEvent;
import wang.switchy.hin2n.event.ErrorEvent;
import wang.switchy.hin2n.event.StartEvent;
import wang.switchy.hin2n.event.StopEvent;
import wang.switchy.hin2n.event.SupernodeDisconnectEvent;
import wang.switchy.hin2n.model.N2NSettingInfo;
import wang.switchy.hin2n.service.N2NService;
import wang.switchy.hin2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.hin2n.template.BaseTemplate;
import wang.switchy.hin2n.template.CommonTitleTemplate;
import wang.switchy.hin2n.tool.N2nTools;

public class MainActivity extends BaseActivity {

    private N2NSettingModel mCurrentSettingInfo;
    private RelativeLayout mCurrentSettingItem;
    private TextView mCurrentSettingName;
    private ImageView mConnectBtn;
    //    private AVLoadingIndicatorView mLoadingView;
    private TextView mSupernodeDisconnectNote;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private LinearLayout mLeftMenu;

    private static final int REQUECT_CODE_SDCARD = 2;

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


//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) titleTemplate.mLeftImg.getLayoutParams();
//        layoutParams.leftMargin = N2nTools.dp2px(this, 10);
//        titleTemplate.mLeftImg.setLayoutParams(layoutParams);

        titleTemplate.mLeftImg.setImageResource(R.mipmap.ic_menu);
        titleTemplate.mLeftImg.setVisibility(View.VISIBLE);
        titleTemplate.mLeftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawerLayout.isDrawerOpen(mLeftMenu)) {
                    mDrawerLayout.closeDrawer(mLeftMenu);
                } else {
                    mDrawerLayout.openDrawer(mLeftMenu);
                }
            }
        });

        return titleTemplate;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.mipmap.ic_launcher, R.string.open, R.string.close) {
            //菜单打开
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            // 菜单关闭
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

        mLeftMenu = (LinearLayout) findViewById(R.id.ll_menu_left);

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

        initLeftMenu();

        if (!MPermissions.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUECT_CODE_SDCARD)) {
            MPermissions.requestPermissions(MainActivity.this, REQUECT_CODE_SDCARD, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

    }

    private void initLeftMenu() {
        TextView appVersion = (TextView) findViewById(R.id.tv_app_version);
        appVersion.setText(N2nTools.getVersionName(this));

        RelativeLayout shareItem = (RelativeLayout) findViewById(R.id.rl_share);
        shareItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.d("shareItem onClick~");

                if (Build.VERSION.SDK_INT >= 23) {
                    String[] permissionList = new String[]{
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.CALL_PHONE,
//                            Manifest.permission.READ_LOGS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.SET_DEBUG_APP,
//                            Manifest.permission.SYSTEM_ALERT_WINDOW,
//                            Manifest.permission.GET_ACCOUNTS,
//                            Manifest.permission.WRITE_APN_SETTINGS
                    };
//                    String[] DeniedPermissions = new String[]{};
//                    for (int i = 0; i < permissionList.length; i++) {
//                        if (ContextCompat.checkSelfPermission(MainActivity.this,
//                                permissionList[i])
//                                != PackageManager.PERMISSION_GRANTED) {
//                            DeniedPermissions
//                        }
//                    }
                    ActivityCompat.requestPermissions(MainActivity.this, permissionList, 123);


                } else {
                    doOnClickShareItem();

                }

            }
        });

//        shareItem.setVisibility(View.GONE);

        RelativeLayout feedbackItem = (RelativeLayout) findViewById(R.id.rl_feedback);
        feedbackItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = joinQQGroup("5QSK63d7uDivxPW2oCpWHyi7FmE4sAzo");
                if (!b) {
                    Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.WEB_VIEW_TYPE, WebViewActivity.TYPE_WEB_VIEW_FEEDBACK);
                    startActivity(intent);
                }
            }
        });

        RelativeLayout checkUpdateItem = (RelativeLayout) findViewById(R.id.rl_check_update);
        checkUpdateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Beta.checkUpgrade();
            }
        });

        RelativeLayout aboutItem = (RelativeLayout) findViewById(R.id.rl_about);
        aboutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.WEB_VIEW_TYPE, WebViewActivity.TYPE_WEB_VIEW_ABOUT);
                startActivity(intent);
            }
        });
    }

    private void doOnClickShareItem() {

        UMWeb umWeb = new UMWeb("https://github.com/switch-iot/n2n_vLTS/blob/master/README.md");
        umWeb.setTitle("Hin2n");
        umWeb.setThumb(new UMImage(this, R.drawable.ic_launcher));
        umWeb.setDescription("N2N is a VPN project that supports p2p.");

        new ShareAction(MainActivity.this)
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

                        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.WEB_VIEW_TYPE, WebViewActivity.TYPE_WEB_VIEW_SHARE);
                        startActivity(intent);

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


    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

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

    /****************
     * 发起添加群流程。群号：手机版n2n(hin2n)交流群(769731491) 的 key 为： 5QSK63d7uDivxPW2oCpWHyi7FmE4sAzo
     * 调用 joinQQGroup(5QSK63d7uDivxPW2oCpWHyi7FmE4sAzo) 即可发起手Q客户端申请加群 手机版n2n(hin2n)交流群(769731491)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }


    /**
     * check permission
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123) {
            for (int i = 0; i < grantResults.length; i++) {
//                Log.e("zhangbzshare", "permission[" + i + "] = " + permissions[i] + ",grantResult[" + i + "] = " + grantResults[i]);
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            doOnClickShareItem();
        }

    }


    @PermissionGrant(REQUECT_CODE_SDCARD)
    public void requestSdcardSuccess() {
        Toast.makeText(this, "GRANT ACCESS SDCARD!", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUECT_CODE_SDCARD)
    public void requestSdcardFailed() {
        Toast.makeText(this, "DENY ACCESS SDCARD!", Toast.LENGTH_SHORT).show();
//        finish();
    }

    @ShowRequestPermissionRationale(REQUECT_CODE_SDCARD)
    public void ShowRequestPermissionRationale() {
        Toast.makeText(this, "ShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
        Logger.d("ShowRequestPermissionRationale");

//        mConnectBtn.setImageResource(R.mipmap.ic_state_supernode_diconnect);
//        mSupernodeDisconnectNote.setVisibility(View.VISIBLE);

        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
        sweetAlertDialog
                .setTitleText("I need permission!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        MPermissions.requestPermissions(MainActivity.this, REQUECT_CODE_SDCARD, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    }
                })
                .show();


    }

}
