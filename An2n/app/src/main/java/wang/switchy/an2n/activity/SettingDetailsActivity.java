package wang.switchy.an2n.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.SpanWatcher;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import wang.switchy.an2n.An2nApplication;
import wang.switchy.an2n.event.ErrorEvent;
import wang.switchy.an2n.event.StartEvent;
import wang.switchy.an2n.event.StopEvent;
import wang.switchy.an2n.service.N2NService;
import wang.switchy.an2n.R;
import wang.switchy.an2n.model.EdgeCmd;
import wang.switchy.an2n.model.N2NSettingInfo;
import wang.switchy.an2n.storage.db.base.N2NSettingModelDao;
import wang.switchy.an2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.an2n.template.BaseTemplate;
import wang.switchy.an2n.template.CommonTitleTemplate;

import static android.R.attr.id;

/**
 * Created by janiszhang on 2018/5/4.
 */

public class SettingDetailsActivity extends BaseActivity implements View.OnClickListener {

    public static int TYPE_SETTING_ADD = 0;
    public static int TYPE_SETTING_MODIFY = 1;
    private int type = TYPE_SETTING_ADD;

    private TextInputLayout mIpAddressTIL;
    private TextInputLayout mNetMaskTIL;
    private TextInputLayout mCommunityTIL;
    private TextInputLayout mEncryptTIL;
    private TextInputLayout mSuperNodeTIL;
    private Button mSaveBtn;
    private SharedPreferences mAn2nSp;
    private SharedPreferences.Editor mAn2nEdit;
    private TextInputLayout mSettingName;

    private TextInputLayout mSuperNodeBackup;
    private TextInputLayout mMacAddr;
    private TextInputLayout mMtu;
    private TextInputLayout mLocalIP;
    private TextInputLayout mHolePunchInterval;
    private CheckBox mResoveSupernodeIPCheckBox;
    private TextInputLayout mLocalPort;
    private CheckBox mAllowRoutinCheckBox;
    private CheckBox mDropMuticastCheckBox;
    private Spinner mTraceLevelSpinner;
    private CheckBox mMoreSettingCheckBox;
    private RelativeLayout mMoreSettingView;
    private N2NSettingModel mN2NSettingModel;
    private Button mModifyBtn;
    private LinearLayout mButtons;
    private Button mDeleteBtn;
    private long mSaveId;
    private ArrayList<String> mTraceLevelList;
    private CheckBox mLocalIpCheckBox;

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

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mAn2nSp = getSharedPreferences("An2n", MODE_PRIVATE);
        mAn2nEdit = mAn2nSp.edit();

        mSettingName = (TextInputLayout) findViewById(R.id.til_setting_name);
        mIpAddressTIL = (TextInputLayout) findViewById(R.id.til_ip_address);
        mNetMaskTIL = (TextInputLayout) findViewById(R.id.til_net_mask);
        mCommunityTIL = (TextInputLayout) findViewById(R.id.til_community);
        mEncryptTIL = (TextInputLayout) findViewById(R.id.til_encrypt);
        mEncryptTIL.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());//隐藏
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
        mLocalIpCheckBox = (CheckBox) findViewById(R.id.check_box_local_ip);
        mLocalIpCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mLocalIP.getEditText().setText("");
                    mLocalIP.setEnabled(false);
                } else {
                    mLocalIP.setEnabled(true);
                }
            }
        });
        mHolePunchInterval = (TextInputLayout) findViewById(R.id.til_hole_punch_Interval);
        mResoveSupernodeIPCheckBox = (CheckBox) findViewById(R.id.resove_super_node_ip_check_box);
        mLocalPort = (TextInputLayout) findViewById(R.id.til_local_port);
        mAllowRoutinCheckBox = (CheckBox) findViewById(R.id.allow_routing_check_box);
        mDropMuticastCheckBox = (CheckBox) findViewById(R.id.drop_muticast_check_box);

        mTraceLevelSpinner = (Spinner) findViewById(R.id.spinner_trace_level);

        mTraceLevelList = new ArrayList<>();
        mTraceLevelList.add("ERROR");
        mTraceLevelList.add("WARNING");
        mTraceLevelList.add("NORMAL");
        mTraceLevelList.add("INFO");
        mTraceLevelList.add("DEBUG");

        final ArrayAdapter<String> traceLevelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTraceLevelList);
        traceLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTraceLevelSpinner.setAdapter(traceLevelAdapter);

        mTraceLevelSpinner.setSelection(1);

        mTraceLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {// parent： 为控件Spinner view：显示文字的TextView position：下拉选项的位置从0开始
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSaveBtn = (Button) findViewById(R.id.btn_save);
        mSaveBtn.setOnClickListener(this);

        mButtons = (LinearLayout) findViewById(R.id.ll_buttons);
        mModifyBtn = (Button) findViewById(R.id.btn_modify);
        mModifyBtn.setOnClickListener(this);

        Intent intent = getIntent();
        type = intent.getIntExtra("type", 0);

        if (type == TYPE_SETTING_ADD) {
            //新增配置，需要设置默认值的设置默认值
            mMtu.getEditText().setText("1400");
            mHolePunchInterval.getEditText().setText("25");
            mTraceLevelSpinner.setSelection(1);

            mSaveBtn.setVisibility(View.VISIBLE);
            mButtons.setVisibility(View.GONE);
        } else if (type == TYPE_SETTING_MODIFY) {
            // 从数据库读取存储

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

            if (mN2NSettingModel.getLocalIP().equals("auto")) {
                mLocalIP.setEnabled(false);
                mLocalIpCheckBox.setChecked(true);
            } else {
                mLocalIP.getEditText().setText(mN2NSettingModel.getLocalIP());
                mLocalIpCheckBox.setChecked(false);
            }
            mHolePunchInterval.getEditText().setText(String.valueOf(mN2NSettingModel.getHolePunchInterval()));
            mResoveSupernodeIPCheckBox.setChecked(mN2NSettingModel.getResoveSupernodeIP());
            mLocalPort.getEditText().setText(String.valueOf(mN2NSettingModel.getLocalPort()));
            mAllowRoutinCheckBox.setChecked(mN2NSettingModel.getAllowRouting());
            mDropMuticastCheckBox.setChecked(mN2NSettingModel.getDropMuticast());

            mTraceLevelSpinner.setSelection(Integer.valueOf(mN2NSettingModel.getTraceLevel()));

            if (mN2NSettingModel.getMoreSettings()) {
                mMoreSettingCheckBox.setChecked(true);
                mMoreSettingView.setVisibility(View.VISIBLE);
            } else {
                mMoreSettingCheckBox.setChecked(false);
            }

            mButtons.setVisibility(View.VISIBLE);
            mSaveBtn.setVisibility(View.GONE);
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

                if (!checkValues()) {
                    return;
                }

                N2NSettingModelDao n2NSettingModelDao = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                String settingName = mSettingName.getEditText().getText().toString();
                String setingNameTmp = settingName;//原始字符串
                int i = 0;
                while (n2NSettingModelDao.queryBuilder().where(N2NSettingModelDao.Properties.Name.eq(settingName)).unique() != null) {
                    i++;
                    settingName = setingNameTmp + "(" + i + ")";

                }
                Long id;
//                if (mMoreSettingCheckBox.isChecked()) {
//                    Log.e("0511", "定位2");

                    mN2NSettingModel = new N2NSettingModel(null, settingName, mIpAddressTIL.getEditText().getText().toString(),
                            TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
                            mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                            mSuperNodeTIL.getEditText().getText().toString(), mMoreSettingCheckBox.isChecked(), mSuperNodeBackup.getEditText().getText().toString(),
                            TextUtils.isEmpty(mMacAddr.getEditText().getText().toString()) ? EdgeCmd.getRandomMac() : mMacAddr.getEditText().getText().toString(),
                            TextUtils.isEmpty(mMtu.getEditText().getText().toString()) ? 1400 : Integer.valueOf(mMtu.getEditText().getText().toString()), mLocalIpCheckBox.isChecked() ? "auto" : mLocalIP.getEditText().getText().toString(),
                            TextUtils.isEmpty(mHolePunchInterval.getEditText().getText().toString()) ? 25 : Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()),
                            mResoveSupernodeIPCheckBox.isChecked(), TextUtils.isEmpty(mLocalPort.getEditText().getText().toString()) ? 0 : Integer.valueOf(mLocalPort.getEditText().getText().toString()),
                            mAllowRoutinCheckBox.isChecked(), mDropMuticastCheckBox.isChecked(), mTraceLevelSpinner.getSelectedItemPosition()/*TextUtils.isEmpty(mTraceLevel.getEditText().getText().toString()) ?  1:Integer.valueOf(mTraceLevel.getEditText().getText().toString())*/, false);
                    id = n2NSettingModelDao.insert(mN2NSettingModel);

//                } else {
//                    Log.e("0511", "定位2");
//
//                    mN2NSettingModel = new N2NSettingModel(null, settingName, mIpAddressTIL.getEditText().getText().toString(),
//                            TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
//                            mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
//                            mSuperNodeTIL.getEditText().getText().toString(), false, "", EdgeCmd.getRandomMac(), 1400, "", 25, false, 0, false, false, 1, false);
//
//                    id = n2NSettingModelDao.insert(mN2NSettingModel);
//                    Log.e("0511", "定位3");
//
//                }


                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Add Succeed!")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                finish();
                            }
                        })
                        .show();


                break;

            case R.id.btn_modify:
                if (!checkValues()) {
                    return;
                }

                Log.e("0511", "定位1");
                N2NSettingModelDao n2NSettingModelDao1 = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                String settingName1 = mSettingName.getEditText().getText().toString();
                String setingNameTmp1 = settingName1;//原始字符串
                int i1 = 0;
                N2NSettingModel n2NSettingModelTmp = n2NSettingModelDao1.queryBuilder().where(N2NSettingModelDao.Properties.Name.eq(settingName1)).unique();

                while (n2NSettingModelTmp != null) {
                    if (n2NSettingModelTmp.getId() == mSaveId) {
                        break;
                    }

                    i1++;
                    settingName1 = setingNameTmp1 + "(" + i1 + ")";

                    n2NSettingModelTmp = n2NSettingModelDao1.queryBuilder().where(N2NSettingModelDao.Properties.Name.eq(settingName1)).unique();
                }

//                if (mMoreSettingCheckBox.isChecked()) {
//                    Log.e("0511", "定位2");

                    mN2NSettingModel = new N2NSettingModel(mSaveId, settingName1, mIpAddressTIL.getEditText().getText().toString(),
                            TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
                            mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                            mSuperNodeTIL.getEditText().getText().toString(), mMoreSettingCheckBox.isChecked(), mSuperNodeBackup.getEditText().getText().toString(),
                            TextUtils.isEmpty(mMacAddr.getEditText().getText().toString()) ? EdgeCmd.getRandomMac() : mMacAddr.getEditText().getText().toString(),
                            TextUtils.isEmpty(mMtu.getEditText().getText().toString()) ? 1400 : Integer.valueOf(mMtu.getEditText().getText().toString()), mLocalIpCheckBox.isChecked() ? "auto" : mLocalIP.getEditText().getText().toString(),
                            TextUtils.isEmpty(mHolePunchInterval.getEditText().getText().toString()) ? 25 : Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()),
                            mResoveSupernodeIPCheckBox.isChecked(), TextUtils.isEmpty(mLocalPort.getEditText().getText().toString()) ? 0 : Integer.valueOf(mLocalPort.getEditText().getText().toString()),
                            mAllowRoutinCheckBox.isChecked(), mDropMuticastCheckBox.isChecked(), mTraceLevelSpinner.getSelectedItemPosition()/*TextUtils.isEmpty(mTraceLevel.getEditText().getText().toString()) ?  1 : Integer.valueOf(mTraceLevel.getEditText().getText().toString())*/, mN2NSettingModel.getIsSelcected());
                    n2NSettingModelDao1.update(mN2NSettingModel);
//                } else {
//                    Log.e("0511", "定位2");
//
//                    mN2NSettingModel = new N2NSettingModel(mSaveId, settingName1, mIpAddressTIL.getEditText().getText().toString(),
//                            TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString(),
//                            mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
//                            mSuperNodeTIL.getEditText().getText().toString(), false, mSuperNodeBackup.getEditText().getText().toString(), TextUtils.isEmpty(mMacAddr.getEditText().getText().toString()) ? EdgeCmd.getRandomMac() : mMacAddr.getEditText().getText().toString(),
//                            TextUtils.isEmpty(mMtu.getEditText().getText().toString()) ? 1400 : Integer.valueOf(mMtu.getEditText().getText().toString()), mLocalIpCheckBox.isChecked() ? "auto" : mLocalIP.getEditText().getText().toString(), 25, false, 0, false, false, 1, mN2NSettingModel.getIsSelcected());
//
//                    n2NSettingModelDao1.update(mN2NSettingModel);
//                    Log.e("0511", "定位3");
//
//                }

                if (N2NService.INSTANCE != null && N2NService.INSTANCE.getEdgeStatus().isRunning) {
                    Long currentSettingId = mAn2nSp.getLong("current_setting_id", -1);

                    if (currentSettingId == mSaveId) {
                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(SettingDetailsActivity.this, SweetAlertDialog.WARNING_TYPE);
                        sweetAlertDialog
                                .setTitleText("Update the setting ?")
//                                .setContentText("Won't be able to recover this file!")
                                .setCancelText("No")
                                .setConfirmText("Yes")
                                .showCancelButton(true)
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.cancel();
                                    }
                                })
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        N2NService.INSTANCE.stop();

                                        Intent vpnPrepareIntent = VpnService.prepare(SettingDetailsActivity.this);

                                        if (vpnPrepareIntent != null) {
                                            startActivityForResult(vpnPrepareIntent, 100);
                                        } else {
                                            onActivityResult(100, -1, null);

                                        }

                                        sweetAlertDialog.cancel();

//                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Save Succeed!")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        finish();
                                    }
                                })
                                .show();
                    }
                } else {
                    new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Save Succeed!")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    finish();
                                }
                            })
                            .show();
                }

                break;
            default:

                break;
        }
    }

    private boolean checkValues() {
        /**
         * 基础配置判空
         *
         * 判空的状态恢复有问题，后续有时间再改吧
         */
        if (TextUtils.isEmpty(mSettingName.getEditText().getText())
                || TextUtils.isEmpty(mIpAddressTIL.getEditText().getText())
                || TextUtils.isEmpty(mCommunityTIL.getEditText().getText())
//                || TextUtils.isEmpty(mEncryptTIL.getEditText().getText())//密码可以为空
                || TextUtils.isEmpty(mSuperNodeTIL.getEditText().getText())) {

            if (TextUtils.isEmpty(mSuperNodeTIL.getEditText().getText())) {
                mSuperNodeTIL.setError("Required");
                mSuperNodeTIL.getEditText().requestFocus();
            } else {
                mSuperNodeTIL.setErrorEnabled(false);
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

            return false;
        }

        /**
         * 基础配置参数检查
         */

        if (!EdgeCmd.checkSupernode(mSuperNodeTIL.getEditText().getText().toString())) {
            mSuperNodeTIL.setError("Supernode Error!");
            mSuperNodeTIL.getEditText().requestFocus();
            return false;

        } else {
            mSuperNodeTIL.setErrorEnabled(false);

        }

        if (!EdgeCmd.checkCommunity(mCommunityTIL.getEditText().getText().toString())) {
            mCommunityTIL.setError("Community Error!");
            mCommunityTIL.getEditText().requestFocus();
            return false;

        } else {
            mCommunityTIL.setErrorEnabled(false);

        }

        if (!EdgeCmd.checkEncKey(mEncryptTIL.getEditText().getText().toString())) {
            mEncryptTIL.setError("Password Error!");
            mEncryptTIL.getEditText().requestFocus();
            return false;

        } else {
            mEncryptTIL.setErrorEnabled(false);

        }

        if (!EdgeCmd.checkIPV4(mIpAddressTIL.getEditText().getText().toString())) {

            mIpAddressTIL.setError("IP Address Error!");
            mIpAddressTIL.getEditText().requestFocus();
            return false;

        } else {
            mIpAddressTIL.setErrorEnabled(false);
        }

        if (!EdgeCmd.checkIPV4Mask(TextUtils.isEmpty(mNetMaskTIL.getEditText().getText().toString()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString())) {
            mNetMaskTIL.setError("NetMask Error!");
            mNetMaskTIL.getEditText().requestFocus();
            Log.e("zhangbzln", "定位1~1");

            return false;

        } else {
            Log.e("zhangbzln", "定位1~2");

            mNetMaskTIL.setErrorEnabled(false);

        }

        /**
         * 高级配置参数检查
         */

//        if (mMoreSettingCheckBox.isChecked()) {

            Log.e("0511", "TextUtils.isEmpty(mSuperNodeBackup.getEditText().getText().toString()) = " + TextUtils.isEmpty(mSuperNodeBackup.getEditText().getText().toString()));
            if (!TextUtils.isEmpty(mSuperNodeBackup.getEditText().getText().toString()) && !EdgeCmd.checkSupernode(mSuperNodeBackup.getEditText().getText().toString())) {
                mSuperNodeBackup.setError("Supernode Back Error!");
                mSuperNodeBackup.getEditText().requestFocus();

                if (!mMoreSettingCheckBox.isChecked()) {
                    mMoreSettingCheckBox.setChecked(true);
                    mMoreSettingView.setVisibility(View.VISIBLE);
                }

                return false;
            } else {
                mSuperNodeBackup.setErrorEnabled(false);

            }

            if (!TextUtils.isEmpty(mMtu.getEditText().getText().toString()) && !EdgeCmd.checkInt(Integer.valueOf(mMtu.getEditText().getText().toString()), 46, 1500)) {
                mMtu.setError("Mtu Error!");
                mMtu.getEditText().requestFocus();

                if (!mMoreSettingCheckBox.isChecked()) {
                    mMoreSettingCheckBox.setChecked(true);
                    mMoreSettingView.setVisibility(View.VISIBLE);
                }

                return false;

            } else {
                mMtu.setErrorEnabled(false);

            }

            if (!TextUtils.isEmpty(mHolePunchInterval.getEditText().getText().toString()) && !EdgeCmd.checkInt(Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()), 10, 120)) {
                mHolePunchInterval.setError("Hole Punch Interval Error!");
                mHolePunchInterval.getEditText().requestFocus();

                if (!mMoreSettingCheckBox.isChecked()) {
                    mMoreSettingCheckBox.setChecked(true);
                    mMoreSettingView.setVisibility(View.VISIBLE);
                }

                return false;
            } else {
                mHolePunchInterval.setErrorEnabled(false);

            }

            if (!mLocalIpCheckBox.isChecked()) {
                if (!TextUtils.isEmpty(mLocalIP.getEditText().getText().toString()) && !EdgeCmd.checkIPV4(mLocalIP.getEditText().getText().toString())) {
                    mLocalIP.setError("Local IP Error!");
                    mLocalIP.getEditText().requestFocus();

                    if (!mMoreSettingCheckBox.isChecked()) {
                        mMoreSettingCheckBox.setChecked(true);
                        mMoreSettingView.setVisibility(View.VISIBLE);
                    }

                    return false;

                } else {
                    mLocalIP.setErrorEnabled(false);

                }
            }

            if (!TextUtils.isEmpty(mLocalPort.getEditText().getText().toString()) && !EdgeCmd.checkInt(Integer.valueOf(mLocalPort.getEditText().getText().toString()), 0, 65535)) {
                mLocalPort.setError("Local Port Error!");
                mLocalPort.getEditText().requestFocus();

                if (!mMoreSettingCheckBox.isChecked()) {
                    mMoreSettingCheckBox.setChecked(true);
                    mMoreSettingView.setVisibility(View.VISIBLE);
                }

                return false;

            } else {
                mLocalPort.setErrorEnabled(false);

            }

            if (!TextUtils.isEmpty(mMacAddr.getEditText().getText().toString()) && !EdgeCmd.checkMacAddr(mMacAddr.getEditText().getText().toString())) {
                mMacAddr.setError("Mac Address Error!");
                mMacAddr.getEditText().requestFocus();

                if (!mMoreSettingCheckBox.isChecked()) {
                    mMoreSettingCheckBox.setChecked(true);
                    mMoreSettingView.setVisibility(View.VISIBLE);
                }

                return false;

            } else {
                mMacAddr.setErrorEnabled(false);

            }
//        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStartEvent(StartEvent event) {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Update Succeed!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        finish();
                    }
                })
                .show();

        Log.e("zhangbz", "SetttingDetailsActivity onStartEvent");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStopEvent(StopEvent event) {
        Log.e("zhangbz", "SetttingDetailsActivity onStopEvent");

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText("Something went wrong!")
                .show();

        Log.e("zhangbz", "SetttingDetailsActivity onErrorEvent");
//        Toast.makeText(mContext, "~_~Error~_~", Toast.LENGTH_SHORT).show();
    }
}
