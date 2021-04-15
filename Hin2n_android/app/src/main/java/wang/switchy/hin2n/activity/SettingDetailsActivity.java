package wang.switchy.hin2n.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import wang.switchy.hin2n.Hin2nApplication;
import wang.switchy.hin2n.R;
import wang.switchy.hin2n.event.ErrorEvent;
import wang.switchy.hin2n.event.StartEvent;
import wang.switchy.hin2n.event.StopEvent;
import wang.switchy.hin2n.model.EdgeCmd;
import wang.switchy.hin2n.model.EdgeStatus;
import wang.switchy.hin2n.model.N2NSettingInfo;
import wang.switchy.hin2n.service.N2NService;
import wang.switchy.hin2n.storage.db.base.N2NSettingModelDao;
import wang.switchy.hin2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.hin2n.template.BaseTemplate;
import wang.switchy.hin2n.template.CommonTitleTemplate;


/**
 * Created by janiszhang on 2018/5/4.
 */

public class SettingDetailsActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUECT_CODE_VPN = 2;

    public static int TYPE_SETTING_ADD = 0;
    public static int TYPE_SETTING_MODIFY = 1;
    private int type = TYPE_SETTING_ADD;

    private TextInputLayout mIpAddressTIL;
    private TextInputLayout mNetMaskTIL;
    private TextInputLayout mCommunityTIL;
    private TextInputLayout mEncryptTIL;

    private TextInputLayout mDevDescTIL;

    private TextInputLayout mSuperNodeTIL;
    private Button mSaveBtn;
    private SharedPreferences mHin2nSp;
    private SharedPreferences.Editor mHin2nEdit;
    private TextInputLayout mSettingName;

    private TextInputLayout mSuperNodeBackup;
    private TextInputLayout mMacAddr;
    private TextInputLayout mMtu;
    private TextInputLayout mLocalIP;
    private TextInputLayout mHolePunchInterval;
    private RelativeLayout mResolveSnLayout;
    private CheckBox mResoveSupernodeIPCheckBox;
    private TextInputLayout mLocalPort;
    private CheckBox mAllowRoutinCheckBox;
    private CheckBox mAcceptMuticastCheckBox;
    private RelativeLayout mAcceptMuticastView;
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
    private RadioGroup mVersionGroup;
    private CheckBox mUseHttpTunnelCheckBox;
    private RadioButton mVersionV1;
    private RadioButton mVersionV2;
    private RadioButton mVersionV2s;
    private RadioButton mVersionV3;
    private TextInputLayout mGatewayIp;
    private TextInputLayout mDnsServer;
    private LinearLayout mEncryptionBox;
    private Spinner mEncryptionMode;

    @Override
    protected BaseTemplate createTemplate() {
        CommonTitleTemplate titleTemplate = new CommonTitleTemplate(mContext, getString(R.string.title_add_setting));
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

        mHin2nSp = getSharedPreferences("Hin2n", MODE_PRIVATE);
        mHin2nEdit = mHin2nSp.edit();

        mSettingName = (TextInputLayout) findViewById(R.id.til_setting_name);

        mVersionGroup = (RadioGroup) findViewById(R.id.rg_version);
        mVersionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                updateVersionGroupCheck(checkedId);
            }
        });
        mVersionV1 = (RadioButton) findViewById(R.id.rb_v1);
        mVersionV2 = (RadioButton) findViewById(R.id.rb_v2);
        mVersionV2s = (RadioButton) findViewById(R.id.rb_v2s);
        mVersionV3 = (RadioButton) findViewById(R.id.rb_v3);

        mIpAddressTIL = (TextInputLayout) findViewById(R.id.til_ip_address);
        mNetMaskTIL = (TextInputLayout) findViewById(R.id.til_net_mask);
        mCommunityTIL = (TextInputLayout) findViewById(R.id.til_community);
        mEncryptTIL = (TextInputLayout) findViewById(R.id.til_encrypt);
        mEncryptTIL.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());//隐藏
        mSuperNodeTIL = (TextInputLayout) findViewById(R.id.til_super_node);

        mDevDescTIL = (TextInputLayout) findViewById(R.id.til_dev_desc);

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
                    mLocalIP.setEnabled(false);
                } else {
                    mLocalIP.setEnabled(true);
                }
            }
        });
        mHolePunchInterval = (TextInputLayout) findViewById(R.id.til_hole_punch_Interval);
        mResolveSnLayout = (RelativeLayout)  findViewById(R.id.rl_resolve_super_node_ip_check_box);
        mResoveSupernodeIPCheckBox = (CheckBox) findViewById(R.id.resove_super_node_ip_check_box);
        mLocalPort = (TextInputLayout) findViewById(R.id.til_local_port);
        mAllowRoutinCheckBox = (CheckBox) findViewById(R.id.allow_routing_check_box);
        mAcceptMuticastView = (RelativeLayout) findViewById(R.id.rl_drop_muticast);
        mAcceptMuticastCheckBox = (CheckBox) findViewById(R.id.accept_muticast_check_box);
        mUseHttpTunnelCheckBox = (CheckBox) findViewById(R.id.use_http_tunnel_check_box);
        mGatewayIp = (TextInputLayout) findViewById(R.id.til_gateway_ip);
        mDnsServer = (TextInputLayout) findViewById(R.id.til_dns_server_ip);
        mEncryptionBox = (LinearLayout) findViewById(R.id.ll_n2n_encryption);
        mEncryptionMode = (Spinner) findViewById(R.id.til_encryption_mode);

        ArrayAdapter<CharSequence> encAdapter = ArrayAdapter.createFromResource(this, R.array.encryption_modes,
                android.R.layout.simple_spinner_item);
        encAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEncryptionMode.setAdapter(encAdapter);

        mTraceLevelSpinner = (Spinner) findViewById(R.id.spinner_trace_level);

        mTraceLevelList = new ArrayList<>();
        mTraceLevelList.add(getString(R.string.trace_level_error));
        mTraceLevelList.add(getString(R.string.trace_level_warn));
        mTraceLevelList.add(getString(R.string.trace_level_normal));
        mTraceLevelList.add(getString(R.string.trace_level_info));
        mTraceLevelList.add(getString(R.string.trace_level_debug));

        final ArrayAdapter<String> traceLevelAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTraceLevelList);
        traceLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTraceLevelSpinner.setAdapter(traceLevelAdapter);

        mTraceLevelSpinner.setSelection(Integer.valueOf(getString(R.string.item_default_tracelevel)) - 1);

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
            ((CommonTitleTemplate) mTemplate).setTitleText(R.string.title_add_setting);
            mSettingName.getEditText().setText(R.string.item_default_name);
            mVersionV2.setChecked(true);
            mSuperNodeTIL.getEditText().setText(R.string.item_default_supernode_v1);
            mCommunityTIL.getEditText().setText(R.string.item_default_community);
            mEncryptTIL.getEditText().setText(R.string.item_default_password);
            mIpAddressTIL.getEditText().setText(R.string.item_default_ip);
            mSuperNodeBackup.getEditText().setText(R.string.item_default_supernode_backup);
            mMtu.getEditText().setText(R.string.item_default_mtu);
            mHolePunchInterval.getEditText().setText(R.string.item_default_holepunchinterval);
            mLocalIP.getEditText().setText(R.string.item_default_localip);
            mLocalIpCheckBox.setChecked(Boolean.valueOf(getString(R.string.item_default_localip_checkbox)));
            mLocalPort.getEditText().setText(R.string.item_default_localport);
            mNetMaskTIL.getEditText().setText(R.string.item_default_netmask);
            mMacAddr.getEditText().setText(EdgeCmd.getRandomMac());
            mResoveSupernodeIPCheckBox.setChecked(Boolean.valueOf(getString(R.string.item_default_resovesupernodeip)));
            mAllowRoutinCheckBox.setChecked(Boolean.valueOf(getString(R.string.item_default_allowrouting)));
            mAcceptMuticastCheckBox.setChecked(!Boolean.valueOf(getString(R.string.item_default_dropmuticast)));
            mUseHttpTunnelCheckBox.setChecked(Boolean.valueOf(getString(R.string.item_default_usehttptunnel)));
            mTraceLevelSpinner.setSelection(Integer.valueOf(getString(R.string.item_default_tracelevel)) - 1);
            mMoreSettingCheckBox.setChecked(false);
            mGatewayIp.getEditText().setText(R.string.item_default_gateway_ip);
            mDnsServer.getEditText().setText("");
            mEncryptionMode.setSelection(encAdapter.getPosition("Twofish"));

            mDevDescTIL.getEditText().setText("");

            mSaveBtn.setVisibility(View.VISIBLE);
            mButtons.setVisibility(View.GONE);
        } else if (type == TYPE_SETTING_MODIFY) {
            // 从数据库读取存储
            ((CommonTitleTemplate) mTemplate).setTitleText(R.string.title_update_setting);
            mSaveId = intent.getLongExtra("saveId", 0);
            mN2NSettingModel = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().load(mSaveId);
            mSettingName.getEditText().setText(mN2NSettingModel.getName());
            switch (mN2NSettingModel.getVersion()) {
                case 0:
                    mVersionV1.setChecked(true);
                    break;
                case 1:
                    mVersionV2.setChecked(true);
                    break;
                case 2:
                    mVersionV2s.setChecked(true);
                    break;
                case 3:
                    mVersionV3.setChecked(true);
                default:
                    break;
            }
            mIpAddressTIL.getEditText().setText(mN2NSettingModel.getIp());
            mNetMaskTIL.getEditText().setText(mN2NSettingModel.getNetmask());
            mCommunityTIL.getEditText().setText(mN2NSettingModel.getCommunity());
            mEncryptTIL.getEditText().setText(mN2NSettingModel.getPassword());
            mDevDescTIL.getEditText().setText(mN2NSettingModel.getDevDesc());
            mSuperNodeTIL.getEditText().setText(mN2NSettingModel.getSuperNode());
            mGatewayIp.getEditText().setText(mN2NSettingModel.getGatewayIp());
            mDnsServer.getEditText().setText(mN2NSettingModel.getDnsServer());
            mEncryptionMode.setSelection(encAdapter.getPosition(mN2NSettingModel.getEncryptionMode()));

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
            mAcceptMuticastCheckBox.setChecked(!mN2NSettingModel.getDropMuticast());
            mUseHttpTunnelCheckBox.setChecked(mN2NSettingModel.getUseHttpTunnel());
            mTraceLevelSpinner.setSelection(Integer.valueOf(mN2NSettingModel.getTraceLevel()));
            mMoreSettingCheckBox.setChecked(false);

            mButtons.setVisibility(View.VISIBLE);
            mSaveBtn.setVisibility(View.GONE);
        }

        updateVersionGroupCheck(mVersionGroup.getCheckedRadioButtonId());
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_add_item;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUECT_CODE_VPN && resultCode == RESULT_OK) {
            Intent intent = new Intent(SettingDetailsActivity.this, N2NService.class);
            Bundle bundle = new Bundle();
            N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mN2NSettingModel);
            bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
            intent.putExtra("Setting", bundle);

            startService(intent);
        }
    }

    private void updateVersionGroupCheck(int checkedId) {
        switch (checkedId) {
            case R.id.rb_v1:
                mUseHttpTunnelCheckBox.setVisibility(View.GONE);
                mDevDescTIL.setVisibility(View.GONE);
                mSuperNodeBackup.setVisibility(View.GONE);
                mAcceptMuticastView.setVisibility(View.GONE);
                mHolePunchInterval.setVisibility(View.GONE);
                mLocalIP.setVisibility(View.GONE);
                mLocalIpCheckBox.setVisibility(View.GONE);
                mGatewayIp.setVisibility(View.GONE);
                mDnsServer.setVisibility(View.GONE);
                mResolveSnLayout.setVisibility(View.VISIBLE);
                mEncryptionBox.setVisibility(View.GONE);
                if (isDefaultSupernode(mSuperNodeTIL.getEditText().getText().toString())) {
                    mSuperNodeTIL.getEditText().setText(R.string.item_default_supernode_v1);
                }
                break;
            case R.id.rb_v2:
                mUseHttpTunnelCheckBox.setVisibility(View.GONE);
                mDevDescTIL.setVisibility(View.GONE);
                mSuperNodeBackup.setVisibility(View.VISIBLE);
                mAcceptMuticastView.setVisibility(View.VISIBLE);
                mHolePunchInterval.setVisibility(View.GONE);
                mLocalIP.setVisibility(View.GONE);
                mLocalIpCheckBox.setVisibility(View.GONE);
                mGatewayIp.setVisibility(View.VISIBLE);
                mDnsServer.setVisibility(View.VISIBLE);
                mResolveSnLayout.setVisibility(View.GONE);
                mEncryptionBox.setVisibility(View.VISIBLE);
                if (isDefaultSupernode(mSuperNodeTIL.getEditText().getText().toString())) {
                    mSuperNodeTIL.getEditText().setText(R.string.item_default_supernode_v2);
                }
                break;
            case R.id.rb_v2s:
                mUseHttpTunnelCheckBox.setVisibility(View.GONE);
                mDevDescTIL.setVisibility(View.GONE);
                mSuperNodeBackup.setVisibility(View.VISIBLE);
                mAcceptMuticastView.setVisibility(View.VISIBLE);
                mHolePunchInterval.setVisibility(View.VISIBLE);
                mLocalIP.setVisibility(View.VISIBLE);
                mLocalIpCheckBox.setVisibility(View.VISIBLE);
                mGatewayIp.setVisibility(View.GONE);
                mDnsServer.setVisibility(View.GONE);
                mResolveSnLayout.setVisibility(View.VISIBLE);
                mEncryptionBox.setVisibility(View.GONE);
                if (isDefaultSupernode(mSuperNodeTIL.getEditText().getText().toString())) {
                    mSuperNodeTIL.getEditText().setText(R.string.item_default_supernode_v2s);
                }
                break;
            case R.id.rb_v3:
                mUseHttpTunnelCheckBox.setVisibility(View.GONE);
                mDevDescTIL.setVisibility(View.VISIBLE);
                mSuperNodeBackup.setVisibility(View.VISIBLE);
                mAcceptMuticastView.setVisibility(View.VISIBLE);
                mHolePunchInterval.setVisibility(View.GONE);
                mLocalIP.setVisibility(View.GONE);
                mLocalIpCheckBox.setVisibility(View.GONE);
                mGatewayIp.setVisibility(View.VISIBLE);
                mDnsServer.setVisibility(View.VISIBLE);
                mResolveSnLayout.setVisibility(View.GONE);
                mEncryptionBox.setVisibility(View.VISIBLE);
                if (isDefaultSupernode(mSuperNodeTIL.getEditText().getText().toString())) {
                    mSuperNodeTIL.getEditText().setText(R.string.item_default_supernode_v2);
                }
                break;
            default:
                break;
        }
    }

    private Boolean isDefaultSupernode(String supernode) {
        if (supernode == null || supernode.isEmpty() ||
                supernode.equals(getString(R.string.item_default_supernode_v1)) ||
                supernode.equals(getString(R.string.item_default_supernode_v2)) ||
                supernode.equals(getString(R.string.item_default_supernode_v2s))) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                if (!checkValues()) {
                    return;
                }

                N2NSettingModelDao n2NSettingModelDao = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                String settingName = mSettingName.getEditText().getText().toString();
                String setingNameTmp = settingName;//原始字符串
                int i = 0;
                while (n2NSettingModelDao.queryBuilder().where(N2NSettingModelDao.Properties.Name.eq(settingName)).unique() != null) {
                    i++;
                    settingName = setingNameTmp + "(" + i + ")";
                }

                boolean hasSelected = false;
                if (n2NSettingModelDao.queryBuilder().where(N2NSettingModelDao.Properties.IsSelcected.eq(true)).unique() != null) {
                    hasSelected = true;
                }

                mN2NSettingModel = new N2NSettingModel(null, getN2nVersion(), settingName,
                        mIpAddressTIL.getEditText().getText().toString(), mNetMaskTIL.getEditText().getText().toString(),
                        mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                        mDevDescTIL.getEditText().getText().toString(),
                        mSuperNodeTIL.getEditText().getText().toString(), mMoreSettingCheckBox.isChecked(),
                        mSuperNodeBackup.getEditText().getText().toString(), mMacAddr.getEditText().getText().toString(),
                        Integer.valueOf(mMtu.getEditText().getText().toString()), mLocalIpCheckBox.isChecked() ? "auto" : mLocalIP.getEditText().getText().toString(),
                        Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()), mResoveSupernodeIPCheckBox.isChecked(),
                        Integer.valueOf(mLocalPort.getEditText().getText().toString()), mAllowRoutinCheckBox.isChecked(),
                        !mAcceptMuticastCheckBox.isChecked(), mUseHttpTunnelCheckBox.isChecked(),
                        mTraceLevelSpinner.getSelectedItemPosition(), !hasSelected,
                        mGatewayIp.getEditText().getText().toString(),
                        mDnsServer.getEditText().getText().toString(),
                        mEncryptionMode.getSelectedItem().toString());
                n2NSettingModelDao.insert(mN2NSettingModel);

                if (!hasSelected) {
                    mN2NSettingModel = n2NSettingModelDao.queryBuilder().where(N2NSettingModelDao.Properties.IsSelcected.eq(true)).unique();
                    mHin2nEdit.putLong("current_setting_id", mN2NSettingModel.getId());
                    mHin2nEdit.commit();
                }

                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText(getString(R.string.dialog_add_succeed))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                finish();
                                sweetAlertDialog.dismiss();
                            }
                        }).show();
                break;
            case R.id.btn_modify:
                if (!checkValues()) {
                    return;
                }

                N2NSettingModelDao n2NSettingModelDao1 = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
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

                mN2NSettingModel = new N2NSettingModel(mSaveId, getN2nVersion(), settingName1,
                        mIpAddressTIL.getEditText().getText().toString(), mNetMaskTIL.getEditText().getText().toString(),
                        mCommunityTIL.getEditText().getText().toString(), mEncryptTIL.getEditText().getText().toString(),
                        mDevDescTIL.getEditText().getText().toString(),
                        mSuperNodeTIL.getEditText().getText().toString(), mMoreSettingCheckBox.isChecked(),
                        mSuperNodeBackup.getEditText().getText().toString(), mMacAddr.getEditText().getText().toString(),
                        Integer.valueOf(mMtu.getEditText().getText().toString()), mLocalIpCheckBox.isChecked() ? "auto" : mLocalIP.getEditText().getText().toString(),
                        Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()), mResoveSupernodeIPCheckBox.isChecked(),
                        Integer.valueOf(mLocalPort.getEditText().getText().toString()), mAllowRoutinCheckBox.isChecked(),
                        !mAcceptMuticastCheckBox.isChecked(), mUseHttpTunnelCheckBox.isChecked(),
                        mTraceLevelSpinner.getSelectedItemPosition(), mN2NSettingModel.getIsSelcected(),
                        mGatewayIp.getEditText().getText().toString(),
                        mDnsServer.getEditText().getText().toString(),
                        mEncryptionMode.getSelectedItem().toString());
                n2NSettingModelDao1.update(mN2NSettingModel);

                if (N2NService.INSTANCE != null &&
                        N2NService.INSTANCE.getCurrentStatus() != EdgeStatus.RunningStatus.DISCONNECT &&
                        N2NService.INSTANCE.getCurrentStatus() != EdgeStatus.RunningStatus.FAILED) {
                    Long currentSettingId = mHin2nSp.getLong("current_setting_id", -1);
                    if (currentSettingId == mSaveId) {
                        new SweetAlertDialog(SettingDetailsActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(getString(R.string.dialog_update_request))
                                .setCancelText(getString(R.string.dialog_no))
                                .setConfirmText(getString(R.string.dialog_yes))
                                .showCancelButton(true)
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.cancel();
                                    }
                                })
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        N2NService.INSTANCE.stop(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent vpnPrepareIntent = VpnService.prepare(SettingDetailsActivity.this);
                                                if (vpnPrepareIntent != null) {
                                                    startActivityForResult(vpnPrepareIntent, REQUECT_CODE_VPN);
                                                } else {
                                                    onActivityResult(REQUECT_CODE_VPN, RESULT_OK, null);
                                                }
                                            }
                                        });

                                        sweetAlertDialog.dismiss();
                                    }
                                })
                                .show();
                    } else {
                        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText(getString(R.string.dialog_save_succeed))
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        finish();
                                        sweetAlertDialog.dismiss();
                                    }
                                }).show();
                    }
                } else {
                    new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(getString(R.string.dialog_save_succeed))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    finish();
                                    sweetAlertDialog.dismiss();
                                }
                            }).show();
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
                || TextUtils.isEmpty(mSuperNodeTIL.getEditText().getText())) {
            Boolean bReq = false;
            if (TextUtils.isEmpty(mSettingName.getEditText().getText())) {
                mSettingName.setError(mSettingName.getHint() + " is required");
                if (!bReq) {
                    mSettingName.getEditText().requestFocus();
                    bReq = true;
                }
            } else {
                mSettingName.setErrorEnabled(false);
            }

            if (TextUtils.isEmpty(mSuperNodeTIL.getEditText().getText())) {
                mSuperNodeTIL.setError(mSuperNodeTIL.getHint() + " is required");
                if (!bReq) {
                    mSuperNodeTIL.getEditText().requestFocus();
                    bReq = true;
                }
            } else {
                mSuperNodeTIL.setErrorEnabled(false);
            }

            if (TextUtils.isEmpty(mCommunityTIL.getEditText().getText())) {
                mCommunityTIL.setError(mCommunityTIL.getHint() + " is required");
                if (!bReq) {
                    mCommunityTIL.getEditText().requestFocus();
                    bReq = true;
                }
            } else {
                mCommunityTIL.setErrorEnabled(false);
            }

            if (TextUtils.isEmpty(mIpAddressTIL.getEditText().getText())) {
                mIpAddressTIL.setError(mIpAddressTIL.getHint() + " is required");
                if (!bReq) {
                    mIpAddressTIL.getEditText().requestFocus();
                    bReq = true;
                }
            } else {
                mIpAddressTIL.setErrorEnabled(false);
            }

            return false;
        }

        /**
         * 基础配置参数检查
         */
        if (!EdgeCmd.checkSupernode(mSuperNodeTIL.getEditText().getText().toString())) {
            mSuperNodeTIL.setError(mSuperNodeTIL.getHint() + " format is incorrect");
            mSuperNodeTIL.getEditText().requestFocus();
            return false;
        } else {
            mSuperNodeTIL.setErrorEnabled(false);
        }
        if (!EdgeCmd.checkCommunity(mCommunityTIL.getEditText().getText().toString())) {
            mCommunityTIL.setError(mCommunityTIL.getHint() + " format is incorrect");
            mCommunityTIL.getEditText().requestFocus();
            return false;
        } else {
            mCommunityTIL.setErrorEnabled(false);
        }
        if (!EdgeCmd.checkEncKey(mEncryptTIL.getEditText().getText().toString())) {
            mEncryptTIL.setError(mEncryptTIL.getHint() + " format is incorrect");
            mEncryptTIL.getEditText().requestFocus();
            return false;
        } else {
            mEncryptTIL.setErrorEnabled(false);
        }
        if (!EdgeCmd.checkIPV4(mIpAddressTIL.getEditText().getText().toString())) {
            mIpAddressTIL.setError(mIpAddressTIL.getHint() + " format is incorrect");
            mIpAddressTIL.getEditText().requestFocus();
            return false;
        } else {
            mIpAddressTIL.setErrorEnabled(false);
        }
        // netmask => v1, v2, v2s
        if (!EdgeCmd.checkIPV4Mask(TextUtils.isEmpty(mNetMaskTIL.getEditText().getText().toString()) ?
                        "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString())) {
            mNetMaskTIL.setError(mNetMaskTIL.getHint() + " format is incorrect");
            mNetMaskTIL.getEditText().requestFocus();
            return false;
        } else {
            mNetMaskTIL.setErrorEnabled(false);
        }
        if((!mGatewayIp.getEditText().getText().toString().isEmpty()) &&
            (!EdgeCmd.checkIPV4(mGatewayIp.getEditText().getText().toString()))) {
          mGatewayIp.setError(mGatewayIp.getHint() + " format is incorrect");
          mGatewayIp.getEditText().requestFocus();
          return false;
        } else {
          mGatewayIp.setErrorEnabled(false);
        }

        if((!mDnsServer.getEditText().getText().toString().isEmpty()) &&
                (!EdgeCmd.checkIPV4(mDnsServer.getEditText().getText().toString()))) {
            mDnsServer.setError(mDnsServer.getHint() + " format is incorrect");
            mDnsServer.getEditText().requestFocus();
            return false;
        } else {
            mDnsServer.setErrorEnabled(false);
        }

        /**
         * 高级配置参数检查
         */
        int ver = getN2nVersion();
        // backup supernode => v2, v2s
        if ((ver == 1 || ver == 2) && !TextUtils.isEmpty(mSuperNodeBackup.getEditText().getText().toString()) && !EdgeCmd.checkSupernode(mSuperNodeBackup.getEditText().getText().toString())) {
            mSuperNodeBackup.setError(mSuperNodeBackup.getHint() + " format is incorrect");
            mSuperNodeBackup.getEditText().requestFocus();
            mMoreSettingCheckBox.setChecked(true);
            mMoreSettingView.setVisibility(View.VISIBLE);
            return false;
        } else {
            mSuperNodeBackup.setErrorEnabled(false);
        }
        // mtu => v1, v2, v2s
        if (!TextUtils.isEmpty(mMtu.getEditText().getText().toString()) && !EdgeCmd.checkMtu(Integer.valueOf(mMtu.getEditText().getText().toString()))) {
            mMtu.setError(mMtu.getHint() + " format is incorrect");
            mMtu.getEditText().requestFocus();
            mMoreSettingCheckBox.setChecked(true);
            mMoreSettingView.setVisibility(View.VISIBLE);
            return false;
        } else {
            mMtu.setErrorEnabled(false);
        }
        // holePunchInterval => v2s
        if (ver == 2 && !TextUtils.isEmpty(mHolePunchInterval.getEditText().getText().toString()) && !EdgeCmd.checkInt(Integer.valueOf(mHolePunchInterval.getEditText().getText().toString()), 10, 120)) {
            mHolePunchInterval.setError(mHolePunchInterval.getHint() + " format is incorrect");
            mHolePunchInterval.getEditText().requestFocus();
            mMoreSettingCheckBox.setChecked(true);
            mMoreSettingView.setVisibility(View.VISIBLE);
            return false;
        } else {
            mHolePunchInterval.setErrorEnabled(false);
        }
        // localIP => v2s
        if (ver == 2 && !mLocalIpCheckBox.isChecked()) {
            if (!TextUtils.isEmpty(mLocalIP.getEditText().getText().toString()) && !EdgeCmd.checkIPV4(mLocalIP.getEditText().getText().toString())) {
                mLocalIP.setError(mLocalIP.getHint() + " format is incorrect");
                mLocalIP.getEditText().requestFocus();
                mMoreSettingCheckBox.setChecked(true);
                mMoreSettingView.setVisibility(View.VISIBLE);
                return false;
            } else {
                mLocalIP.setErrorEnabled(false);
            }
        }
        // localPort => v1, v2, v2s
        if (!TextUtils.isEmpty(mLocalPort.getEditText().getText().toString()) && !EdgeCmd.checkInt(Integer.valueOf(mLocalPort.getEditText().getText().toString()), 0, 65535)) {
            mLocalPort.setError(mLocalPort.getHint() + " format is incorrect");
            mLocalPort.getEditText().requestFocus();
            mMoreSettingCheckBox.setChecked(true);
            mMoreSettingView.setVisibility(View.VISIBLE);
            return false;
        } else {
            mLocalPort.setErrorEnabled(false);
        }
        // macAddr => v1, v2, v2s
        if (!TextUtils.isEmpty(mMacAddr.getEditText().getText().toString()) && !EdgeCmd.checkMacAddr(mMacAddr.getEditText().getText().toString())) {
            mMacAddr.setError(mMacAddr.getHint() + " format is incorrect");
            mMacAddr.getEditText().requestFocus();
            mMoreSettingCheckBox.setChecked(true);
            mMoreSettingView.setVisibility(View.VISIBLE);
            return false;
        } else {
            mMacAddr.setErrorEnabled(false);
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private int getN2nVersion() {
        switch (mVersionGroup.getCheckedRadioButtonId()) {
            case R.id.rb_v1:
                return 0;
            case R.id.rb_v2:
                return 1;
            case R.id.rb_v2s:
                return 2;
            case R.id.rb_v3:
                return 3;
            default:
                return -1;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStartEvent(StartEvent event) {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(getString(R.string.dialog_update_succeed))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        finish();
                    }
                }).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStopEvent(StopEvent event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText("Something went wrong!")
                .show();
    }
}
