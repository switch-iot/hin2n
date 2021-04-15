package wang.switchy.hin2n.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.VpnService;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import wang.switchy.hin2n.Hin2nApplication;
import wang.switchy.hin2n.R;
import wang.switchy.hin2n.adapter.SettingItemAdapter;
import wang.switchy.hin2n.entity.SettingItemEntity;
import wang.switchy.hin2n.event.ErrorEvent;
import wang.switchy.hin2n.event.StartEvent;
import wang.switchy.hin2n.event.StopEvent;
import wang.switchy.hin2n.model.EdgeStatus;
import wang.switchy.hin2n.model.N2NSettingInfo;
import wang.switchy.hin2n.service.N2NService;
import wang.switchy.hin2n.storage.db.base.N2NSettingModelDao;
import wang.switchy.hin2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.hin2n.template.BaseTemplate;
import wang.switchy.hin2n.template.CommonTitleTemplate;
import wang.switchy.hin2n.tool.N2nTools;
import wang.switchy.hin2n.tool.ThreadUtils;


/**
 * Created by janiszhang on 2018/5/4.
 */

public class ListActivity extends BaseActivity {

    private static final int REQUECT_CODE_VPN = 2;

    private SwipeMenuListView mSettingsListView;
    private SettingItemAdapter mSettingItemAdapter;
    private ArrayList<SettingItemEntity> mSettingItemEntities;

    private SharedPreferences mHin2nSp;
    private SharedPreferences.Editor mHin2nEdit;
    private N2NSettingModel mN2NSettingModel;
    private int mTargetSettingPosition;

    @Override
    protected BaseTemplate createTemplate() {
        CommonTitleTemplate titleTemplate = new CommonTitleTemplate(mContext, getString(R.string.title_setting_list));
        titleTemplate.mRightImg.setVisibility(View.VISIBLE);
        titleTemplate.mRightImg.setImageResource(R.mipmap.ic_add);
        titleTemplate.mRightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, SettingDetailsActivity.class);
                intent.putExtra("type", SettingDetailsActivity.TYPE_SETTING_ADD);
                startActivity(intent);
            }
        });

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

        mSettingsListView = (SwipeMenuListView) findViewById(R.id.lv_setting_item);

        mSettingItemEntities = new ArrayList<>();

        mSettingItemAdapter = new SettingItemAdapter(this, mSettingItemEntities);

        mSettingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final Long currentSettingId = mHin2nSp.getLong("current_setting_id", -1);

                SettingItemEntity settingItemEntity = mSettingItemEntities.get(position);
                Long saveId = settingItemEntity.getSaveId();
                if (currentSettingId.equals(saveId)) {
                    return;
                }

                if (N2NService.INSTANCE != null &&
                        N2NService.INSTANCE.getCurrentStatus() != EdgeStatus.RunningStatus.DISCONNECT &&
                        N2NService.INSTANCE.getCurrentStatus() != EdgeStatus.RunningStatus.FAILED) {
                    new SweetAlertDialog(ListActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(getString(R.string.dialog_reconnect))
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
                                            Intent vpnPrepareIntent = VpnService.prepare(ListActivity.this);
                                            if (vpnPrepareIntent != null) {
                                                startActivityForResult(vpnPrepareIntent, REQUECT_CODE_VPN);
                                            } else {
                                                onActivityResult(REQUECT_CODE_VPN, RESULT_OK, null);
                                            }
                                        }
                                    });

                                    mTargetSettingPosition = position;

                                    if (currentSettingId != -1) {
                                        ThreadUtils.cachedThreadExecutor(new Runnable() {
                                            @Override
                                            public void run() {
                                                N2NSettingModel currentSettingItem = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().load((long) currentSettingId);
                                                if (currentSettingItem != null) {
                                                    currentSettingItem.setIsSelcected(false);
                                                    Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().update(currentSettingItem);
                                                }
                                            }
                                        });
                                    }

                                    for (int i = 0; i < mSettingItemEntities.size(); i++) {
                                        mSettingItemEntities.get(i).setSelected(false);
                                    }
                                    ThreadUtils.cachedThreadExecutor(new Runnable() {
                                        @Override
                                        public void run() {
                                            N2NSettingModelDao n2NSettingModelDao = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                                            mN2NSettingModel = n2NSettingModelDao.load(mSettingItemEntities.get(position).getSaveId());
                                            mN2NSettingModel.setIsSelcected(true);
                                            n2NSettingModelDao.update(mN2NSettingModel);
                                            mHin2nEdit.putLong("current_setting_id", mN2NSettingModel.getId());
                                            mHin2nEdit.commit();
                                            mSettingItemEntities.get(position).setSelected(true);
                                            ThreadUtils.mainThreadExecutor(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mSettingItemAdapter.notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    });
                                    sweetAlertDialog.cancel();
                                }
                            })
                            .show();
                } else {
                    if (currentSettingId != -1) {
                        ThreadUtils.cachedThreadExecutor(new Runnable() {
                            @Override
                            public void run() {
                                N2NSettingModel currentSettingItem = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().load((long) currentSettingId);
                                if (currentSettingItem != null) {
                                    currentSettingItem.setIsSelcected(false);
                                    Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().update(currentSettingItem);
                                }
                            }
                        });
                    }

                    for (int i = 0; i < mSettingItemEntities.size(); i++) {
                        mSettingItemEntities.get(i).setSelected(false);
                    }
                    ThreadUtils.cachedThreadExecutor(new Runnable() {
                        @Override
                        public void run() {
                            N2NSettingModelDao n2NSettingModelDao = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                            mN2NSettingModel = n2NSettingModelDao.load(mSettingItemEntities.get(position).getSaveId());
                            mN2NSettingModel.setIsSelcected(true);

                            n2NSettingModelDao.update(mN2NSettingModel);

                            mHin2nEdit.putLong("current_setting_id", mN2NSettingModel.getId());
                            mHin2nEdit.commit();
                            mSettingItemEntities.get(position).setSelected(true);
                            ThreadUtils.mainThreadExecutor(new Runnable() {
                                @Override
                                public void run() {
                                    mSettingItemAdapter.notifyDataSetChanged();
                                }
                            });

                        }
                    });
                }

            }
        });

        /*****************侧滑菜单 begin********************/
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem copyItem = new SwipeMenuItem(getApplicationContext());
                copyItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                copyItem.setWidth(N2nTools.dp2px(ListActivity.this, 70));
                copyItem.setTitle("Copy");
                copyItem.setTitleSize(18);
                copyItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(copyItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(N2nTools.dp2px(ListActivity.this, 70));
                deleteItem.setTitle("Delete");
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };
        mSettingsListView.setMenuCreator(creator);

        mSettingsListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        mSettingsListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                final SettingItemEntity settingItemEntity = mSettingItemEntities.get(position);

                switch (index) {
                    case 0:
                        N2NSettingModelDao n2NSettingModelDao1 = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                        N2NSettingModel n2NSettingModelCopy = n2NSettingModelDao1.load(settingItemEntity.getSaveId());

                        //1.db update
                        String copyName = n2NSettingModelCopy.getName() + "-copy";
                        String copyNameTmp = copyName;

                        int i = 0;
                        while (n2NSettingModelDao1.queryBuilder().where(N2NSettingModelDao.Properties.Name.eq(copyName)).unique() != null) {
                            i++;
                            copyName = copyNameTmp + "(" + i + ")";

                        }

                        N2NSettingModel n2NSettingModel = new N2NSettingModel(null, n2NSettingModelCopy.getVersion(), copyName, n2NSettingModelCopy.getIp(), n2NSettingModelCopy.getNetmask(), n2NSettingModelCopy.getCommunity(),
                                n2NSettingModelCopy.getPassword(), n2NSettingModelCopy.getDevDesc(), n2NSettingModelCopy.getSuperNode(), n2NSettingModelCopy.getMoreSettings(), n2NSettingModelCopy.getSuperNodeBackup(),
                                n2NSettingModelCopy.getMacAddr(), n2NSettingModelCopy.getMtu(), n2NSettingModelCopy.getLocalIP(), n2NSettingModelCopy.getHolePunchInterval(),
                                n2NSettingModelCopy.getResoveSupernodeIP(), n2NSettingModelCopy.getLocalPort(), n2NSettingModelCopy.getAllowRouting(), n2NSettingModelCopy.getDropMuticast(),
                                n2NSettingModelCopy.isUseHttpTunnel(), n2NSettingModelCopy.getTraceLevel(), false, n2NSettingModelCopy.getGatewayIp(), n2NSettingModelCopy.getDnsServer(),
                                n2NSettingModelCopy.getEncryptionMode());
                        n2NSettingModelDao1.insert(n2NSettingModel);

                        //2.ui update
                        final SettingItemEntity settingItemEntity2 = new SettingItemEntity(n2NSettingModel.getName(),
                                n2NSettingModel.getId(), n2NSettingModel.getIsSelcected());

                        settingItemEntity2.setOnMoreBtnClickListener(new SettingItemEntity.OnMoreBtnClickListener() {
                            @Override
                            public void onClick(int positon) {
                                Intent intent = new Intent(ListActivity.this, SettingDetailsActivity.class);
                                intent.putExtra("type", SettingDetailsActivity.TYPE_SETTING_MODIFY);
                                intent.putExtra("saveId", settingItemEntity2.getSaveId());

                                startActivity(intent);
                            }
                        });
                        mSettingItemEntities.add(settingItemEntity2);
                        mSettingItemAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        final SettingItemEntity finalSettingItemEntity = settingItemEntity;
                        final Long currentSettingId = mHin2nSp.getLong("current_setting_id", -1);
                        new SweetAlertDialog(ListActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(getString(R.string.dialog_delete))
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
                                        N2NSettingModelDao n2NSettingModelDao = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                                        n2NSettingModelDao.deleteByKey(finalSettingItemEntity.getSaveId());

                                        mSettingItemEntities.remove(finalSettingItemEntity);
                                        mSettingItemAdapter.notifyDataSetChanged();

                                        if (N2NService.INSTANCE != null && currentSettingId == finalSettingItemEntity.getSaveId()) {
                                            N2NService.INSTANCE.stop(null);
                                        }

                                        sweetAlertDialog.cancel();
                                    }
                                })
                                .show();
                        break;
                    default:
                        break;
                }

                return false;
            }
        });

        /*****************侧滑菜单 end********************/

        mSettingsListView.setAdapter(mSettingItemAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThreadUtils.cachedThreadExecutor(new Runnable() {
            @Override
            public void run() {
                N2NSettingModelDao n2NSettingModelDao = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                List<N2NSettingModel> n2NSettingModels = n2NSettingModelDao.loadAll();

                N2NSettingModel n2NSettingModel;
                mSettingItemEntities.clear();
                for (int i = 0; i < n2NSettingModels.size(); i++) {
                    n2NSettingModel = n2NSettingModels.get(i);
                    final SettingItemEntity settingItemEntity = new SettingItemEntity(n2NSettingModel.getName(),
                            n2NSettingModel.getId(), n2NSettingModel.getIsSelcected());

                    settingItemEntity.setOnMoreBtnClickListener(new SettingItemEntity.OnMoreBtnClickListener() {

                        @Override
                        public void onClick(int positon) {
                            Intent intent = new Intent(ListActivity.this, SettingDetailsActivity.class);
                            intent.putExtra("type", SettingDetailsActivity.TYPE_SETTING_MODIFY);
                            intent.putExtra("saveId", settingItemEntity.getSaveId());

                            startActivity(intent);
                        }
                    });
                    mSettingItemEntities.add(settingItemEntity);

                    if (n2NSettingModel.getIsSelcected()) {
                        mHin2nEdit.putLong("current_setting_id", n2NSettingModel.getId());
                        mHin2nEdit.commit();
                    }
                }
                ThreadUtils.mainThreadExecutor(new Runnable() {
                    @Override
                    public void run() {
                        mSettingItemAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUECT_CODE_VPN && resultCode == RESULT_OK) {
            SettingItemEntity settingItemEntity = mSettingItemEntities.get(mTargetSettingPosition);

            N2NSettingModelDao n2NSettingModelDao1 = Hin2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
            N2NSettingModel n2NSettingModel = n2NSettingModelDao1.load(settingItemEntity.getSaveId());

            Intent intent = new Intent(ListActivity.this, N2NService.class);
            Bundle bundle = new Bundle();

            N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(n2NSettingModel);
            bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
            intent.putExtra("Setting", bundle);

            startService(intent);
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_setting_list;
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStopEvent(StopEvent event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onErrorEvent(ErrorEvent event) {
        Toast.makeText(mContext, "~_~Error~_~", Toast.LENGTH_SHORT).show();
    }
}
