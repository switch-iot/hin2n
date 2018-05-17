package wang.switchy.an2n.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wang.switchy.an2n.An2nApplication;
import wang.switchy.an2n.service.N2NService;
import wang.switchy.an2n.R;
import wang.switchy.an2n.adapter.SettingItemAdapter;
import wang.switchy.an2n.entity.SettingItemEvtity;
import wang.switchy.an2n.model.N2NSettingInfo;
import wang.switchy.an2n.storage.db.base.N2NSettingModelDao;
import wang.switchy.an2n.storage.db.base.model.N2NSettingModel;
import wang.switchy.an2n.template.BaseTemplate;
import wang.switchy.an2n.template.CommonTitleTemplate;


/**
 * Created by janiszhang on 2018/5/4.
 */

public class ListActivity extends BaseActivity {

    private ListView mSettingsListView;
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

        mSettingsListView = (ListView) findViewById(R.id.lv_setting_item);

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

        mSettingsListView.setAdapter(mSettingItemAdapter);

//        mMoreInfo = (TextView) findViewById(R.id.tv_more);
//        mMoreInfo.setClickable(true);
//        mMoreInfo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(mContext, "mMoreInfo onClick", Toast.LENGTH_SHORT).show();
//            }
//        });

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
                public void onClick() {
                    // TODO: 2018/5/10
                    Log.e("zhangbz", "settingItemEvtity onClick~");

                    Intent intent = new Intent(ListActivity.this, SettingDetailsActivity.class);
                    intent.putExtra("type", SettingDetailsActivity.TYPE_SETTING_MODIFY);
                    intent.putExtra("saveId", settingItemEvtity.getSaveId());

                    startActivity(intent);
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
