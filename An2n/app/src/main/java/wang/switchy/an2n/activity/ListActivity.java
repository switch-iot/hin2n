package wang.switchy.an2n.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import wang.switchy.an2n.An2nApplication;
import wang.switchy.an2n.model.EdgeCmd;
import wang.switchy.an2n.service.N2NService;
import wang.switchy.an2n.R;
import wang.switchy.an2n.adapter.SettingItemAdapter;
import wang.switchy.an2n.entity.SettingItemEvtity;
import wang.switchy.an2n.model.N2NSettingInfo;
import wang.switchy.an2n.storage.db.base.N2NSettingModelDao;
import wang.switchy.an2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.an2n.template.BaseTemplate;
import wang.switchy.an2n.template.CommonTitleTemplate;
import wang.switchy.an2n.tool.N2nTools;

import static android.R.attr.id;
import static android.R.attr.name;
import static android.R.attr.password;
import static com.umeng.analytics.pro.j.a.p;


/**
 * Created by janiszhang on 2018/5/4.
 */

public class ListActivity extends BaseActivity {

    private SwipeMenuListView mSettingsListView;
    private SettingItemAdapter mSettingItemAdapter;
    private ArrayList<SettingItemEvtity> mSettingItemEvtities;

    private SharedPreferences mAn2nSp;
    private SharedPreferences.Editor mAn2nEdit;
    private TextView mMoreInfo;
    private N2NSettingModel mN2NSettingModel;

    @Override
    protected BaseTemplate createTemplate() {
        CommonTitleTemplate titleTemplate = new CommonTitleTemplate(mContext, "Setting List");
        titleTemplate.mRightImg.setVisibility(View.VISIBLE);
        titleTemplate.mRightImg.setImageResource(R.mipmap.img_add);
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
                // TODO: 2018/5/4
            }
        });

        return titleTemplate;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {

        mAn2nSp = getSharedPreferences("An2n", MODE_PRIVATE);
        mAn2nEdit = mAn2nSp.edit();

        mSettingsListView = (SwipeMenuListView) findViewById(R.id.lv_setting_item);

        mSettingItemEvtities = new ArrayList<>();

        mSettingItemAdapter = new SettingItemAdapter(this, mSettingItemEvtities);

        mSettingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

//                Toast.makeText(mContext, "mSettingsListView onItemClick", Toast.LENGTH_SHORT).show();

                Long currentSettingId = mAn2nSp.getLong("current_setting_id", -1);

                if (currentSettingId != -1) {
                    N2NSettingModel currentSettingItem = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().load((long) currentSettingId);
                    if (currentSettingItem != null) {
                        currentSettingItem.setIsSelcected(false);
                        An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao().update(currentSettingItem);
                    }
                }

                for (int i = 0; i < mSettingItemEvtities.size(); i++) {
                    mSettingItemEvtities.get(i).setSelected(false);
                }

                mSettingItemAdapter.notifyDataSetChanged();


                N2NSettingModelDao n2NSettingModelDao = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                mN2NSettingModel = n2NSettingModelDao.load(mSettingItemEvtities.get(position).getSaveId());
                mN2NSettingModel.setIsSelcected(true);

                n2NSettingModelDao.update(mN2NSettingModel);

                mAn2nEdit.putLong("current_setting_id", mN2NSettingModel.getId());
                mAn2nEdit.commit();
                mSettingItemEvtities.get(position).setSelected(true);
                mSettingItemAdapter.notifyDataSetChanged();

//                if (N2NService.INSTANCE != null && N2NService.INSTANCE.getEdgeStatus().isRunning) {
//                    Log.e("zhangbz", "~定位~1");
//                    N2NService.INSTANCE.stop();
//                }
//
//                Intent vpnPrepareIntent = VpnService.prepare(ListActivity.this);
//                if (vpnPrepareIntent != null) {
//                    Log.e("zhangbz", "doOnCreate vpnPrepareIntent != null");
//                    startActivityForResult(vpnPrepareIntent, 100);
//                } else {
//                    Log.e("zhangbz", "doOnCreate vpnPrepareIntent == null");
//                    onActivityResult(100, -1, null);
//
//                }
            }
        });

        /*****************侧滑菜单 begin********************/
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem modifyItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                modifyItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                modifyItem.setWidth(N2nTools.dp2px(ListActivity.this, 70));
                // set item title
                modifyItem.setTitle("Edit");
                // set item title fontsize
                modifyItem.setTitleSize(18);
                // set item title font color
                modifyItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(modifyItem);


                SwipeMenuItem copyItem = new SwipeMenuItem(getApplicationContext());
                copyItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                copyItem.setWidth(N2nTools.dp2px(ListActivity.this, 70));
                copyItem.setTitle("Copy");
                copyItem.setTitleSize(18);
                copyItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(copyItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(N2nTools.dp2px(ListActivity.this, 70));
                // set a icon
//                deleteItem.setIcon(R.mipmap.ic_launcher);
                deleteItem.setTitle("Delete");
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(deleteItem);

            }
        };

        // set creator
        mSettingsListView.setMenuCreator(creator);

        // Right
        mSettingsListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        mSettingsListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                Log.e("zhangbz", "onMenuItemClick index = " + index);

                SettingItemEvtity settingItemEvtity = mSettingItemEvtities.get(position);

                switch (index) {
                    case 0:
//                        Toast.makeText(mContext, "ToDo：Modify", Toast.LENGTH_SHORT).show();

                        settingItemEvtity = mSettingItemEvtities.get(position);
                        Intent intent = new Intent(ListActivity.this, SettingDetailsActivity.class);
                        intent.putExtra("type", SettingDetailsActivity.TYPE_SETTING_MODIFY);
                        intent.putExtra("saveId", settingItemEvtity.getSaveId());

                        startActivity(intent);
                        break;

                    case 1:
//                        Toast.makeText(mContext, "ToDo：Copy", Toast.LENGTH_SHORT).show();

                        N2NSettingModelDao n2NSettingModelDao1 = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                        N2NSettingModel n2NSettingModelCopy = n2NSettingModelDao1.load(settingItemEvtity.getSaveId());

                        //1.db update
                        String copyName = n2NSettingModelCopy.getName() + "-copy";
                        String copyNameTmp = copyName;

                        int i = 0;
                        while (n2NSettingModelDao1.queryBuilder().where(N2NSettingModelDao.Properties.Name.eq(copyName)).unique() != null) {
                            i++;
                            copyName = copyNameTmp + "(" + i + ")";

                        }

                        N2NSettingModel n2NSettingModel = new N2NSettingModel(null, copyName, n2NSettingModelCopy.getIp(), n2NSettingModelCopy.getNetmask(), n2NSettingModelCopy.getCommunity(),
                                n2NSettingModelCopy.getPassword(), n2NSettingModelCopy.getSuperNode(), n2NSettingModelCopy.getMoreSettings(), n2NSettingModelCopy.getSuperNodeBackup(),
                                n2NSettingModelCopy.getMacAddr(), n2NSettingModelCopy.getMtu(), n2NSettingModelCopy.getLocalIP(), n2NSettingModelCopy.getHolePunchInterval(),
                                n2NSettingModelCopy.getResoveSupernodeIP(), n2NSettingModelCopy.getLocalPort(), n2NSettingModelCopy.getAllowRouting(), n2NSettingModelCopy.getDropMuticast(),
                                n2NSettingModelCopy.getTraceLevel(), false);

                        n2NSettingModelDao1.insert(n2NSettingModel);

                        //2.ui update

                        SettingItemEvtity settingItemEvtity2 = new SettingItemEvtity(n2NSettingModel.getName(),
                                n2NSettingModel.getId(), n2NSettingModel.getIsSelcected());
                        mSettingItemEvtities.add(settingItemEvtity2);

                        mSettingItemAdapter.notifyDataSetChanged();

                        break;

                    case 2:
//                        Toast.makeText(mContext, "ToDo：Delete", Toast.LENGTH_SHORT).show();

                        final SettingItemEvtity finalSettingItemEvtity = settingItemEvtity;
                        new SweetAlertDialog(ListActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Are you sure?")
//                                .setContentText("Won't be able to recover this file!")
                                .setCancelText("No,cancel plx!")
                                .setConfirmText("Yes,delete it!")
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
                                        N2NSettingModelDao n2NSettingModelDao = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
                                        n2NSettingModelDao.deleteByKey(finalSettingItemEvtity.getSaveId());

                                        mSettingItemEvtities.remove(finalSettingItemEvtity);
                                        mSettingItemAdapter.notifyDataSetChanged();

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

//        mSettingsListView.smoothOpenMenu();
        /*****************侧滑菜单 end********************/

        mSettingsListView.setAdapter(mSettingItemAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        N2NSettingModelDao n2NSettingModelDao = An2nApplication.getInstance().getDaoSession().getN2NSettingModelDao();
        List<N2NSettingModel> n2NSettingModels = n2NSettingModelDao.loadAll();

        Log.e("zhangbz", "ListActivity onResume n2NSettingModels : " + n2NSettingModels.size());
        N2NSettingModel n2NSettingModel;
//        SettingItemEvtity settingItemEvtity;
        //需要判空吗？
        mSettingItemEvtities.clear();
        for (int i = 0; i < n2NSettingModels.size(); i++) {
            n2NSettingModel = n2NSettingModels.get(i);
            final SettingItemEvtity settingItemEvtity = new SettingItemEvtity(n2NSettingModel.getName(),
                    n2NSettingModel.getId(), n2NSettingModel.getIsSelcected());

//            final SettingItemEvtity finalSettingItemEvtity = settingItemEvtity;

            settingItemEvtity.setOnMoreBtnClickListener(new SettingItemEvtity.OnMoreBtnClickListener() {

                @Override
                public void onClick(int positon) {
//                    // TODO: 2018/5/10
//                    Log.e("zhangbz", "settingItemEvtity onClick~");
//
//                    Intent intent = new Intent(ListActivity.this, SettingDetailsActivity.class);
//                    intent.putExtra("type", SettingDetail.TYPE_SETTING_MODIFY);
//                    intent.putExtra("saveId", settingItemEvtity.getSaveId());
//
//                    startActivity(intent);
                   mSettingsListView.smoothOpenMenu(positon);
                }
            });
            mSettingItemEvtities.add(settingItemEvtity);
        }

        mSettingItemAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("zhangbz", "onActivityResult requestCode = " + requestCode + "; resultCode = " + resultCode);
        if (requestCode == 100 && resultCode == -1) {//RESULT_OK

            Intent intent = new Intent(ListActivity.this, N2NService.class);

            Bundle bundle = new Bundle();
//            N2NSettingModel n2NSettingModel = n2NSettingModelDao.load(mSettingItemEvtities.get(position).getSaveId());

            N2NSettingInfo n2NSettingInfo = new N2NSettingInfo(mN2NSettingModel);

            bundle.putParcelable("n2nSettingInfo", n2NSettingInfo);
            intent.putExtra("Setting", bundle);

            startService(intent);
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_setting_list;
    }
}
