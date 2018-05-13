package wang.switchy.an2n.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import wang.switchy.an2n.An2nApplication;
import wang.switchy.an2n.N2NService;
import wang.switchy.an2n.R;
import wang.switchy.an2n.model.EdgeCmd;
import wang.switchy.an2n.model.N2NSettingInfo;
import wang.switchy.an2n.storage.db.base.N2NSettingModelDao;
import wang.switchy.an2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.an2n.template.BaseTemplate;
import wang.switchy.an2n.template.CommonTitleTemplate;

/**
 * Created by janiszhang on 2018/5/4.
 */

public class SettingDetailsActivity extends BaseActivity implements View.OnClickListener {

    public static int TYPE_SETTING_ADD = 0;
    public static int TYPE_SETTING_MODIFY = 1;
    private int type = TYPE_SETTING_ADD;

    private TextInputLayout mIpAddressTIL;// TODO: 2018/4/17 ip地址的输入内容要检查格式
    private TextInputLayout mNetMaskTIL;
    private TextInputLayout mCommunityTIL;
    private TextInputLayout mEncryptTIL;
    private TextInputLayout mSuperNodeTIL;
    private Button mSaveBtn;
    private SharedPreferences mAn2nSp;
    private SharedPreferences.Editor mAn2nEdit;
    private TextInputLayout mSettingName;
    private CheckBox mSaveAndSetCheckBox;

    private TextInputLayout mSuperNodeBackup;
    private TextInputLayout mMacAddr;
    private TextInputLayout mMtu;
    private TextInputLayout mLocalIP;
    private TextInputLayout mHolePunchInterval;
    private CheckBox mResoveSupernodeIPCheckBox;
    private TextInputLayout mLocalPort;
    private CheckBox mAllowRoutinCheckBox;
    private CheckBox mDropMuticastCheckBox;
    private TextInputLayout mTraceLevel;
    //    private TextInputLayout mVpnFd;
    private CheckBox mMoreSettingCheckBox;
    private RelativeLayout mMoreSettingView;
    private N2NSettingModel mN2NSettingModel;
    private Button mModifyBtn;
    private LinearLayout mButtons;
    private Button mDeleteBtn;
    private long mSaveId;


    @Override
    protected BaseTemplate createTemplate() {
        CommonTitleTemplate titleTemplate = new CommonTitleTemplate(mContext, "Add New Setting");
        titleTemplate.mLeftImg.setVisibility(View.VISIBLE);
        titleTemplate.mLeftImg.setImageResource(R.drawable.titlebar_icon_return_selector);
        titleTemplate.mLeftImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        return titleTemplate;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {

        mAn2nSp = getSharedPreferences("An2n", MODE_PRIVATE);
        mAn2nEdit = mAn2nSp.edit();

        mSettingName = (TextInputLayout) findViewById(R.id.til_setting_name);
        mIpAddressTIL = (TextInputLayout) findViewById(R.id.til_ip_address);
        mNetMaskTIL = (TextInputLayout) findViewById(R.id.til_net_mask);
        mCommunityTIL = (TextInputLayout) findViewById(R.id.til_community);
        mEncryptTIL = (TextInputLayout) findViewById(R.id.til_encrypt);
        mSuperNodeTIL = (TextInputLayout) findViewById(R.id.til_super_node);

        mMoreSettingView = (RelativeLayout) findViewById(R.id.rl_more_setting);

        mMoreSettingCheckBox = (CheckBox) findViewById(R.id.more_setting_check_box);
        mMoreSettingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mMoreSettingView.setVisibility(View.VISIBLE);
                } else {
                    mMoreSettingView.setVisibility(View.GONE);
                }
            }
        });

        mSuperNodeBackup = (TextInputLayout) findViewById(R.id.til_super_node_2);
        mMacAddr = (TextInputLayout) findViewById(R.id.til_mac_addr);
        mMtu = (TextInputLayout) findViewById(R.id.til_mtu);
        mLocalIP = (TextInputLayout) findViewById(R.id.til_local_ip);
        mHolePunchInterval = (TextInputLayout) findViewById(R.id.til_hole_punch_Interval);
        mResoveSupernodeIPCheckBox = (CheckBox) findViewById(R.id.resove_super_node_ip_check_box);
        mLocalPort = (TextInputLayout) findViewById(R.id.til_local_port);
        mAllowRoutinCheckBox = (CheckBox) findViewById(R.id.allow_routing_check_box);
        mDropMuticastCheckBox = (CheckBox) findViewById(R.id.drop_muticast_check_box);
        mTraceLevel = (TextInputLayout) findViewById(R.id.til_trace_level);
//        mVpnFd = (TextInputLayout) findViewById(R.id.til_vpn_fd);

        mSaveAndSetCheckBox = (CheckBox) findViewById(R.id.check_box);

        mSaveBtn = (Button) findViewById(R.id.btn_save);
        mSaveBtn.setOnClickListener(this);

        mButtons = (LinearLayout) findViewById(R.id.ll_buttons);
        mModifyBtn = (Button) findViewById(R.id.btn_modify);
        mModifyBtn.setOnClickListener(this);
        mDeleteBtn = (Button) findViewById(R.id.btn_delete);
        mDeleteBtn.setOnClickListener(this);

        Intent intent = getIntent();
        type = intent.getIntExtra("type", 0);

        if (type == TYPE_SETTING_ADD) {
            //新增配置，需要设置默认值的设置默认值
            mMtu.getEditText().setText("1400");
            mHolePunchInterval.getEditText().setText("25");
            mDropMuticastCheckBox.setChecked(true);
            mTraceLevel.getEditText().setText("1");

            mSaveBtn.setVisibility(View.VISIBLE);
            mButtons.setVisibility(View.GONE);
        } else if (type == TYPE_SETTING_MODIFY) {
            // TODO: 2018/5/11 从数据库读取存储

            mSaveId = intent.getLongExtra("saveId", 0);
            mN2NSettingModel = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().load(mSaveId);

            mSettingName.getEditText().setText(mN2NSettingModel.getName());
            mIpAddressTIL.getEditText().setText(mN2NSettingModel.getIp());
            mNetMaskTIL.getEditText().setText(mN2NSettingModel.getNetmask());
            mCommunityTIL.getEditText().setText(mN2NSettingModel.getCommunity());
            mEncryptTIL.getEditText().setText(mN2NSettingModel.getPassword());
            mSuperNodeTIL.getEditText().setText(mN2NSettingModel.getSuperNode());

            mSuperNodeBackup.getEditText().setText(mN2NSettingModel.getSuperNodeBackup());
            mMacAddr.getEditText().setText(mN2NSettingModel.getMacAddr());
            mMtu.getEditText().setText(String.valueOf(mN2NSettingModel.getMtu()));
            mLocalIP.getEditText().setText(mN2NSettingModel.getLocalIP());
            mHolePunchInterval.getEditText().setText(String.valueOf(mN2NSettingModel.getHolePunchInterval()));
            mResoveSupernodeIPCheckBox.setChecked(mN2NSettingModel.getResoveSupernodeIP());
            mLocalPort.getEditText().setText(String.valueOf(mN2NSettingModel.getLocalPort()));
            mAllowRoutinCheckBox.setChecked(mN2NSettingModel.getAllowRouting());
            mDropMuticastCheckBox.setChecked(mN2NSettingModel.getDropMuticast());
            mTraceLevel.getEditText().setText(String.valueOf(mN2NSettingModel.getTraceLevel()));

            if (mN2NSettingModel.getMoreSettings()) {
                mMoreSettingCheckBox.setSelected(true);
            } else {
                mMoreSettingCheckBox.setChecked(false);
            }

            mButtons.setVisibility(View.VISIBLE);
            mSaveBtn.setVisibility(View.GONE);
        } else {
            Log.e("zhangbz", "error!!");
        }

    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_add_item;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("zhangbz", "onActivityResult requestCode = " + requestCode + "; resultCode = " + resultCode);
        if (requestCode == 100 && resultCode == -1) {//RESULT_OK

            Intent intent = new Intent(SettingDetailsActivity.this, N2NService.class);

            Bundle bundle = new Bundle();
            N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mN2NSettingModel);

            bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
            intent.putExtra("Setting", bundle);

            startService(intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:

                checkValues();

                if (mSaveAndSetCheckBox.isChecked()) {
                    Log.e("zhangbz", "AddItemActivity 定位1");
                    Long currentSettingId = mAn2nSp.getLong("current_setting_id", -1);

                    if (currentSettingId != -1) {
                        Log.e("zhangbz", "AddItemActivity 定位2");

                        N2NSettingModel currentSettingItem = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().load((long) currentSettingId);
                        if (currentSettingItem != null) {
                            currentSettingItem.setIsSelcected(false);
                            An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().update(currentSettingItem);
                        }

                    }

                    Log.e("zhangbz", "AddItemActivity 定位3");

                    N2NSettingModelDao n2NSettingModelDao = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                    long id;
                    if (mMoreSettingCheckBox.isChecked()) {
                        mN2NSettingModel = new N2NSettingModel(null, mSettingName.getEditText().getText().toString(), mIpAddressTIL.getEditText().getText().toString(),
                                TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
                                mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                                mSuperNodeTIL.getEditText().getText().toString(), true, mSuperNodeBackup.getEditText().getText().toString(),
                                TextUtils.isEmpty(mMacAddr.getEditText().getText().toString()) ? EdgeCmd.getRandomMac() : mMacAddr.getEditText().getText().toString(),
                                TextUtils.isEmpty(mMtu.getEditText().getText().toString()) ? 1400 : Integer.valueOf(mMtu.getEditText().getText().toString()) , mLocalIP.getEditText().getText().toString(),
                                TextUtils.isEmpty(mHolePunchInterval.getEditText().getText().toString()) ? 25 : Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()),
                                mResoveSupernodeIPCheckBox.isChecked(), TextUtils.isEmpty(mLocalPort.getEditText().getText().toString()) ? 0 : Integer.valueOf(mLocalPort.getEditText().getText().toString()),
                                mAllowRoutinCheckBox.isChecked(), mDropMuticastCheckBox.isChecked(), TextUtils.isEmpty(mTraceLevel.getEditText().getText().toString()) ? Integer.valueOf(mTraceLevel.getEditText().getText().toString()) : 1, true);
                        id = n2NSettingModelDao.insert(mN2NSettingModel);
                    } else {
                        Log.e("zhangbz", "AddItemActivity 定位4");

                        mN2NSettingModel = new N2NSettingModel(null, mSettingName.getEditText().getText().toString(), mIpAddressTIL.getEditText().getText().toString(),
                                TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
                                mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                                mSuperNodeTIL.getEditText().getText().toString(), false, "", EdgeCmd.getRandomMac(), 1400, "", 25, false, 0, false, true, 1, true);
                        id = n2NSettingModelDao.insert(mN2NSettingModel);
                    }

                    Log.e("zhangbz", "AddItemActivity 定位5");


                    mAn2nEdit.putLong("current_setting_id", id);
                    mAn2nEdit.commit();


                    // TODO: 2018/5/6 start

                    if (N2NService.INSTANCE != null && N2NService.INSTANCE.getEdgeStatus().isRunning) {
                        Log.e("zhangbz", "~定位~1");
                        N2NService.INSTANCE.stop();
                    }

                    Intent vpnPrepareIntent = VpnService.prepare(SettingDetailsActivity.this);
                    if (vpnPrepareIntent != null) {
                        Log.e("zhangbz", "doOnCreate vpnPrepareIntent != null");
                        startActivityForResult(vpnPrepareIntent, 100);
                    } else {
                        Log.e("zhangbz", "doOnCreate vpnPrepareIntent == null");
                        onActivityResult(100, -1, null);

                    }

                    Log.e("zhangbz", "AddItemActivity 定位6");

                } else {
                    Log.e("0511", "定位1");
                    N2NSettingModelDao n2NSettingModelDao = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                    Long id;
                    if (mMoreSettingCheckBox.isChecked()) {
                        Log.e("0511", "定位2");

                        mN2NSettingModel = new N2NSettingModel(null, mSettingName.getEditText().getText().toString(), mIpAddressTIL.getEditText().getText().toString(),
                                TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
                                mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                                mSuperNodeTIL.getEditText().getText().toString(), true, mSuperNodeBackup.getEditText().getText().toString(),
                                TextUtils.isEmpty(mMacAddr.getEditText().getText().toString()) ? EdgeCmd.getRandomMac() : mMacAddr.getEditText().getText().toString(),
                                TextUtils.isEmpty(mMtu.getEditText().getText().toString()) ? 1400 : Integer.valueOf(mMtu.getEditText().getText().toString()), mLocalIP.getEditText().getText().toString(),
                                TextUtils.isEmpty(mHolePunchInterval.getEditText().getText().toString()) ? 25 : Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()),
                                mResoveSupernodeIPCheckBox.isChecked(), TextUtils.isEmpty(mLocalPort.getEditText().getText().toString()) ? 0 : Integer.valueOf(mLocalPort.getEditText().getText().toString()),
                                mAllowRoutinCheckBox.isChecked(), mDropMuticastCheckBox.isChecked(), TextUtils.isEmpty(mTraceLevel.getEditText().getText().toString()) ? Integer.valueOf(mTraceLevel.getEditText().getText().toString()) : 1, false);
                        id = n2NSettingModelDao.insert(mN2NSettingModel);
                    } else {
                        Log.e("0511", "定位2");

                        mN2NSettingModel = new N2NSettingModel(null, mSettingName.getEditText().getText().toString(), mIpAddressTIL.getEditText().getText().toString(),
                                TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
                                mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                                mSuperNodeTIL.getEditText().getText().toString(), false, "", EdgeCmd.getRandomMac(), 1400, "", 25, false, 0, false, true, 1, false);

                        id = n2NSettingModelDao.insert(mN2NSettingModel);
                        Log.e("0511", "定位3");

                    }

                }
                break;

            case R.id.btn_modify:
                checkValues();

                if (mSaveAndSetCheckBox.isChecked()) {
                    Log.e("zhangbz", "AddItemActivity 定位1");
                    Long currentSettingId = mAn2nSp.getLong("current_setting_id", -1);

                    if (currentSettingId != -1) {
                        Log.e("zhangbz", "AddItemActivity 定位2");

                        N2NSettingModel currentSettingItem = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().load((long) currentSettingId);
                        if (currentSettingItem != null) {
                            currentSettingItem.setIsSelcected(false);
                            An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().update(currentSettingItem);
                        }
                    }

                    Log.e("zhangbz", "AddItemActivity 定位3");

                    N2NSettingModelDao n2NSettingModelDao = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                    long id;
                    if (mMoreSettingCheckBox.isChecked()) {
                        mN2NSettingModel = new N2NSettingModel(mSaveId, mSettingName.getEditText().getText().toString(), mIpAddressTIL.getEditText().getText().toString(),
                                TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
                                mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                                mSuperNodeTIL.getEditText().getText().toString(), true, mSuperNodeBackup.getEditText().getText().toString(),
                                TextUtils.isEmpty(mMacAddr.getEditText().getText().toString()) ? EdgeCmd.getRandomMac() : mMacAddr.getEditText().getText().toString(),
                                TextUtils.isEmpty(mMtu.getEditText().getText().toString()) ? 1400 : Integer.valueOf(mMtu.getEditText().getText().toString()) , mLocalIP.getEditText().getText().toString(),
                                TextUtils.isEmpty(mHolePunchInterval.getEditText().getText().toString()) ? 25 : Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()),
                                mResoveSupernodeIPCheckBox.isChecked(), TextUtils.isEmpty(mLocalPort.getEditText().getText().toString()) ? 0 : Integer.valueOf(mLocalPort.getEditText().getText().toString()),
                                mAllowRoutinCheckBox.isChecked(), mDropMuticastCheckBox.isChecked(), TextUtils.isEmpty(mTraceLevel.getEditText().getText().toString()) ? Integer.valueOf(mTraceLevel.getEditText().getText().toString()) : 1, true);
//                        id = n2NSettingModelDao.insert(mN2NSettingModel);
                        n2NSettingModelDao.update(mN2NSettingModel);
                    } else {
                        Log.e("zhangbz", "AddItemActivity 定位4");

                        mN2NSettingModel = new N2NSettingModel(mSaveId, mSettingName.getEditText().getText().toString(), mIpAddressTIL.getEditText().getText().toString(),
                                TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
                                mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                                mSuperNodeTIL.getEditText().getText().toString(), false, "", EdgeCmd.getRandomMac(), 1400, "", 25, false, 0, false, true, 1, true);
//                        id = n2NSettingModelDao.insert(mN2NSettingModel);
                        n2NSettingModelDao.update(mN2NSettingModel);

                    }

                    Log.e("zhangbz", "AddItemActivity 定位5");


                    mAn2nEdit.putLong("current_setting_id", mSaveId);
                    mAn2nEdit.commit();

                    // TODO: 2018/5/6 start

                    if (N2NService.INSTANCE != null && N2NService.INSTANCE.getEdgeStatus().isRunning) {
                        Log.e("zhangbz", "~定位~1");
                        N2NService.INSTANCE.stop();
                    }

                    Intent vpnPrepareIntent = VpnService.prepare(SettingDetailsActivity.this);
                    if (vpnPrepareIntent != null) {
                        Log.e("zhangbz", "doOnCreate vpnPrepareIntent != null");
                        startActivityForResult(vpnPrepareIntent, 100);
                    } else {
                        Log.e("zhangbz", "doOnCreate vpnPrepareIntent == null");
                        onActivityResult(100, -1, null);

                    }

                    Log.e("zhangbz", "AddItemActivity 定位6");

                } else {
                    Log.e("0511", "定位1");
                    N2NSettingModelDao n2NSettingModelDao = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                    Long id;
                    if (mMoreSettingCheckBox.isChecked()) {
                        Log.e("0511", "定位2");

                        mN2NSettingModel = new N2NSettingModel(mSaveId, mSettingName.getEditText().getText().toString(), mIpAddressTIL.getEditText().getText().toString(),
                                TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
                                mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                                mSuperNodeTIL.getEditText().getText().toString(), true, mSuperNodeBackup.getEditText().getText().toString(),
                                TextUtils.isEmpty(mMacAddr.getEditText().getText().toString()) ? EdgeCmd.getRandomMac() : mMacAddr.getEditText().getText().toString(),
                                TextUtils.isEmpty(mMtu.getEditText().getText().toString()) ? 1400 : Integer.valueOf(mMtu.getEditText().getText().toString()), mLocalIP.getEditText().getText().toString(),
                                TextUtils.isEmpty(mHolePunchInterval.getEditText().getText().toString()) ? 25 : Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()),
                                mResoveSupernodeIPCheckBox.isChecked(), TextUtils.isEmpty(mLocalPort.getEditText().getText().toString()) ? 0 : Integer.valueOf(mLocalPort.getEditText().getText().toString()),
                                mAllowRoutinCheckBox.isChecked(), mDropMuticastCheckBox.isChecked(), TextUtils.isEmpty(mTraceLevel.getEditText().getText().toString()) ? Integer.valueOf(mTraceLevel.getEditText().getText().toString()) : 1, false);
                        n2NSettingModelDao.update(mN2NSettingModel);
                    } else {
                        Log.e("0511", "定位2");

                        mN2NSettingModel = new N2NSettingModel(mSaveId, mSettingName.getEditText().getText().toString(), mIpAddressTIL.getEditText().getText().toString(),
                                TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
                                mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                                mSuperNodeTIL.getEditText().getText().toString(), false, "", EdgeCmd.getRandomMac(), 1400, "", 25, false, 0, false, true, 1, false);

                        n2NSettingModelDao.update(mN2NSettingModel);
                        Log.e("0511", "定位3");

                    }

                }

                Toast.makeText(mContext, "Update Succeed", Toast.LENGTH_SHORT).show();

                break;

            case R.id.btn_delete:
                N2NSettingModelDao n2NSettingModelDao = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                n2NSettingModelDao.deleteByKey(mSaveId);

                Toast.makeText(mContext, "Delete Succeed", Toast.LENGTH_SHORT).show();
                break;
            default:

                break;
        }
    }

    private void checkValues() {
        /**
         * 基础配置判空
         *
         * 判空的状态恢复有问题，后续有时间再改吧
         */
        if (TextUtils.isEmpty(mSettingName.getEditText().getText())
                || TextUtils.isEmpty(mIpAddressTIL.getEditText().getText())
                || TextUtils.isEmpty(mCommunityTIL.getEditText().getText())
                || TextUtils.isEmpty(mEncryptTIL.getEditText().getText())
                || TextUtils.isEmpty(mSuperNodeTIL.getEditText().getText())) {

            if (TextUtils.isEmpty(mSuperNodeTIL.getEditText().getText())) {
                mSuperNodeTIL.setError("Required");
                mSuperNodeTIL.getEditText().requestFocus();
            } else {
                mSuperNodeTIL.setErrorEnabled(false);
            }

            if (TextUtils.isEmpty(mEncryptTIL.getEditText().getText())) {
                mEncryptTIL.setError("Required");
                mEncryptTIL.getEditText().requestFocus();
            } else {
                mEncryptTIL.setErrorEnabled(false);
            }

            if (TextUtils.isEmpty(mCommunityTIL.getEditText().getText())) {
                mCommunityTIL.setError("Required");
                mCommunityTIL.getEditText().requestFocus();
            } else {
                mCommunityTIL.setErrorEnabled(false);
            }

            if (TextUtils.isEmpty(mIpAddressTIL.getEditText().getText())) {
                mIpAddressTIL.setError("Required");
                mIpAddressTIL.getEditText().requestFocus();
            } else {
                mIpAddressTIL.setErrorEnabled(false);
            }

            if (TextUtils.isEmpty(mSettingName.getEditText().getText())) {
                mSettingName.setError("Required");
                mSettingName.getEditText().requestFocus();
            } else {
                mSettingName.setErrorEnabled(false);
            }

            return;
        }

        /**
         * 高级配置判空
         */
//                if (mMoreSettingCheckBox.isChecked()) {
//                    if (TextUtils.isEmpty(mSuperNodeBackup.getEditText().getText())
//                            || TextUtils.isEmpty(mMacAddr.getEditText().getText())
//                            || TextUtils.isEmpty(mMtu.getEditText().getText())
//                            || TextUtils.isEmpty(mLocalIP.getEditText().getText())
//                            || TextUtils.isEmpty(mHolePunchInterval.getEditText().getText())
//                            || TextUtils.isEmpty(mLocalPort.getEditText().getText())
//                            || TextUtils.isEmpty(mTraceLevel.getEditText().getText())
////                            || TextUtils.isEmpty(mVpnFd.getEditText().getText())
//                            ) {
//
//                        if (TextUtils.isEmpty(mSuperNodeBackup.getEditText().getText())) {
//                            mSuperNodeBackup.setError("Required");
//                            mSuperNodeBackup.getEditText().requestFocus();
//                        } else {
//                            mSuperNodeBackup.setErrorEnabled(false);
//                        }
//
//                        if (TextUtils.isEmpty(mMacAddr.getEditText().getText())) {
//                            mMacAddr.setError("Required");
//                            mMacAddr.getEditText().requestFocus();
//                        } else {
//                            mMacAddr.setErrorEnabled(false);
//                        }
//
//                        if (TextUtils.isEmpty(mMtu.getEditText().getText())) {
//                            mMtu.setError("Required");
//                            mMtu.getEditText().requestFocus();
//                        } else {
//                            mMtu.setErrorEnabled(false);
//                        }
//
//                        if (TextUtils.isEmpty(mLocalIP.getEditText().getText())) {
//                            mLocalIP.setError("Required");
//                            mLocalIP.getEditText().requestFocus();
//                        } else {
//                            mLocalIP.setErrorEnabled(false);
//                        }
//
//                        if (TextUtils.isEmpty(mHolePunchInterval.getEditText().getText())) {
//                            mHolePunchInterval.setError("Required");
//                            mHolePunchInterval.getEditText().requestFocus();
//                        } else {
//                            mHolePunchInterval.setErrorEnabled(false);
//                        }
//
//                        if (TextUtils.isEmpty(mLocalPort.getEditText().getText())) {
//                            mLocalPort.setError("Required");
//                            mLocalPort.getEditText().requestFocus();
//                        } else {
//                            mLocalPort.setErrorEnabled(false);
//                        }
//
//                        if (TextUtils.isEmpty(mTraceLevel.getEditText().getText())) {
//                            mTraceLevel.setError("Required");
//                            mTraceLevel.getEditText().requestFocus();
//                        } else {
//                            mTraceLevel.setErrorEnabled(false);
//                        }
//
////                        if (TextUtils.isEmpty(mVpnFd.getEditText().getText())) {
////                            mVpnFd.setError("Required");
////                            mVpnFd.getEditText().requestFocus();
////                        } else {
////                            mVpnFd.setErrorEnabled(false);
////                        }
//
//                        return;
//                    }
//
//                }


        /**
         * 基础配置参数检查
         */
        Log.e("zhangbz", "ipAddress = " + mIpAddressTIL.getEditText().getText().toString());
        if (!EdgeCmd.checkIPV4(mIpAddressTIL.getEditText().getText().toString())) {

            mIpAddressTIL.setError("IP Address Error!");
            mIpAddressTIL.getEditText().requestFocus();
            return;

        } else {
            mIpAddressTIL.setErrorEnabled(false);
        }

        Log.e("zhangbzln", "定位1");
//                Log.e("zhangbzln", "定位1 , " + EdgeCmd.checkIPV4Mask(TextUtils.isEmpty(mNetMaskTIL.getEditText().getText().toString()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString()));

        if (!EdgeCmd.checkIPV4Mask(TextUtils.isEmpty(mNetMaskTIL.getEditText().getText().toString()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString())) {
            mNetMaskTIL.setError("NetMask Error!");
            mNetMaskTIL.getEditText().requestFocus();
            Log.e("zhangbzln", "定位1~1");

            return;

        } else {
            Log.e("zhangbzln", "定位1~2");

            mNetMaskTIL.setErrorEnabled(false);

        }

        Log.e("zhangbzln", "定位2");

        if (!EdgeCmd.checkCommunity(mCommunityTIL.getEditText().getText().toString())) {
            mCommunityTIL.setError("Community Error!");
            mCommunityTIL.getEditText().requestFocus();
            return;

        } else {
            mCommunityTIL.setErrorEnabled(false);

        }

        Log.e("zhangbzln", "定位3");

        if (!EdgeCmd.checkEncKey(mEncryptTIL.getEditText().getText().toString())) {
            mEncryptTIL.setError("Password Error!");
            mEncryptTIL.getEditText().requestFocus();
            return;

        } else {
            mEncryptTIL.setErrorEnabled(false);

        }

        Log.e("zhangbzln", "定位4");

        if (!EdgeCmd.checkSupernode(mSuperNodeTIL.getEditText().getText().toString())) {
            mSuperNodeTIL.setError("Supernode Error!");
            mSuperNodeTIL.getEditText().requestFocus();
            return;

        } else {
            mSuperNodeTIL.setErrorEnabled(false);

        }

        /**
         * 高级配置参数检查
         */

        if (mMoreSettingCheckBox.isChecked()) {
            Log.e("0511", "mSuperNodeBackup.getEditText().getText().toString() = " + mSuperNodeBackup.getEditText().getText().toString() + " " + (mSuperNodeBackup.getEditText().getText().toString() != ""));
//                    if (mSuperNodeBackup.getEditText().getText().toString() != "") {
//                        if (!EdgeCmd.checkSupernode(mSuperNodeBackup.getEditText().getText().toString())) {
//                            mSuperNodeBackup.setError("Supernode Back Error!");
//                            mSuperNodeBackup.getEditText().requestFocus();
//                        } else {
//                            mSuperNodeBackup.setErrorEnabled(false);
//                        }
//                    } else {
//                        mSuperNodeBackup.setErrorEnabled(false);
//
//                    }

            Log.e("0511", "TextUtils.isEmpty(mSuperNodeBackup.getEditText().getText().toString()) = " + TextUtils.isEmpty(mSuperNodeBackup.getEditText().getText().toString()));
            if (!TextUtils.isEmpty(mSuperNodeBackup.getEditText().getText().toString()) && !EdgeCmd.checkSupernode(mSuperNodeBackup.getEditText().getText().toString())) {
                mSuperNodeBackup.setError("Supernode Back Error!");
                mSuperNodeBackup.getEditText().requestFocus();
                return;
            } else {
                mSuperNodeBackup.setErrorEnabled(false);

            }

//                    if (mMacAddr.getEditText().getText().toString()  != "") {
//                        if (!EdgeCmd.checkMacAddr(mMacAddr.getEditText().getText().toString())) {
//                            mMacAddr.setError("Mac Address Error!");
//                            mMacAddr.getEditText().requestFocus();
//                            return;
//
//                        } else {
//                            mMacAddr.setErrorEnabled(false);
//                        }
//                    } else {
//                        mMacAddr.setErrorEnabled(false);
//
//                    }

            if (!TextUtils.isEmpty(mMacAddr.getEditText().getText().toString()) && !EdgeCmd.checkMacAddr(mMacAddr.getEditText().getText().toString())) {
                mMacAddr.setError("Mac Address Error!");
                mMacAddr.getEditText().requestFocus();
                return;

            } else {
                mMacAddr.setErrorEnabled(false);

            }

//                    if (mMtu.getEditText().getText().toString() != "") {
//                        if (!EdgeCmd.checkInt(Integer.valueOf(mMtu.getEditText().getText().toString()), 64, 65535)) {
//                            mMtu.setError("Mtu Error!");
//                            mMtu.getEditText().requestFocus();
//                            return;
//
//                        } else {
//                            mMtu.setErrorEnabled(false);
//
//                        }
//                    } else {
//                        mMtu.setErrorEnabled(false);
//
//                    }
            if (!TextUtils.isEmpty(mMtu.getEditText().getText().toString()) && !EdgeCmd.checkInt(Integer.valueOf(mMtu.getEditText().getText().toString()), 64, 65535)) {
                mMtu.setError("Mtu Error!");
                mMtu.getEditText().requestFocus();
                return;

            } else {
                mMtu.setErrorEnabled(false);

            }

            if (!TextUtils.isEmpty(mLocalIP.getEditText().getText().toString()) && !EdgeCmd.checkIPV4(mLocalIP.getEditText().getText().toString())) {
                mLocalIP.setError("Local IP Error!");
                mLocalIP.getEditText().requestFocus();
                return;

            } else {
                mLocalIP.setErrorEnabled(false);

            }
            if (!TextUtils.isEmpty(mHolePunchInterval.getEditText().getText().toString()) && !EdgeCmd.checkInt(Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()), 10, 120)) {
                mHolePunchInterval.setError("Hole Punch Interval Error!");
                mHolePunchInterval.getEditText().requestFocus();
                return;
            } else {
                mHolePunchInterval.setErrorEnabled(false);

            }

            if (!TextUtils.isEmpty(mLocalPort.getEditText().getText().toString()) && !EdgeCmd.checkInt(Integer.valueOf(mLocalPort.getEditText().getText().toString()), 0, 65535)) {
                mLocalPort.setError("Local Port Error!");
                mLocalPort.getEditText().requestFocus();
                return;

            } else {
                mLocalPort.setErrorEnabled(false);

            }

            if (!TextUtils.isEmpty(mTraceLevel.getEditText().getText().toString()) && !EdgeCmd.checkInt(Integer.valueOf(mTraceLevel.getEditText().getText().toString()), 0, 4)) {
                mTraceLevel.setError("Trace Level Error!");
                mTraceLevel.getEditText().requestFocus();
                return;

            } else {
                mTraceLevel.setErrorEnabled(false);

            }

//                    if (!EdgeCmd.checkInt(Integer.valueOf(mVpnFd.getEditText().getText().toString()), 0, 65535)) {
//                        mVpnFd.setError("VpnFd Error!");
//                        mVpnFd.getEditText().requestFocus();
//                        return;
//
//                    } else {
//                        mVpnFd.setErrorEnabled(false);
//
//                    }
        }
    }
}
