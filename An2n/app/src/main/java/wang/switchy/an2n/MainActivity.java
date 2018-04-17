package wang.switchy.an2n;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import wang.switchy.an2n.template.BaseTemplate;
import wang.switchy.an2n.template.MainTitleTemplate;

public class MainActivity extends BaseActivity {

    private TextInputLayout mIpAddressTIL;// TODO: 2018/4/17 ip地址的输入内容要检查格式
    private TextInputLayout mNetMaskTIL;
    private TextInputLayout mCommunityTIL;
    private TextInputLayout mEncryptTIL;
    private TextInputLayout mSuperNodeTIL;
//    private TextInputLayout mSpareSuperNodeTIL;
    private Button mActionBtn;

    // TODO: 2018/4/16 暂时先放这，回头再看看放哪更合适
    static {
        System.loadLibrary("edge_v2s");
        System.loadLibrary("n2n_v2s");
        System.loadLibrary("uip");
    }

    @Override
    protected BaseTemplate createTemplate() {
        return new MainTitleTemplate(this, "An2n");
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {

        Log.e("zhangbz", "MainActivity doOnCreate");
        mIpAddressTIL = (TextInputLayout) findViewById(R.id.til_ip_address);
        mNetMaskTIL = (TextInputLayout) findViewById(R.id.til_net_mask);
        mCommunityTIL = (TextInputLayout) findViewById(R.id.til_community);
        mEncryptTIL = (TextInputLayout) findViewById(R.id.til_encrypt);
        mSuperNodeTIL = (TextInputLayout) findViewById(R.id.til_super_node);
//        mSpareSuperNodeTIL = (TextInputLayout) findViewById(R.id.til_spare_super_node);

        mActionBtn = (Button) findViewById(R.id.btn_action);
        mActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(mIpAddressTIL.getEditText().getText())
                        || TextUtils.isEmpty(mCommunityTIL.getEditText().getText())
                        || TextUtils.isEmpty(mEncryptTIL.getEditText().getText())
                        || TextUtils.isEmpty(mSuperNodeTIL.getEditText().getText())
//                        || TextUtils.isEmpty(mSpareSuperNodeTIL.getEditText().getText())
                        ) {

//                    if (TextUtils.isEmpty(mSpareSuperNodeTIL.getEditText().getText())) {
//                        mSpareSuperNodeTIL.setError("Required");
//                        mSpareSuperNodeTIL.getEditText().requestFocus();
//                    } else {
//                        mSpareSuperNodeTIL.setErrorEnabled(false);
//                    }

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

                    return;
                }

                Intent vpnPrepareIntent = VpnService.prepare(MainActivity.this);
                if (vpnPrepareIntent != null) {
                    Log.e("zhangbz", "doOnCreate vpnPrepareIntent != null");
                    startActivityForResult(vpnPrepareIntent, 100);
                } else {
                    Log.e("zhangbz", "doOnCreate vpnPrepareIntent == null");
                    onActivityResult(100, 0, null);

                }

            }
        });


    }

    @Override
    protected int getContentLayout() {
        return wang.switchy.an2n.R.layout.activity_main;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("zhangbz", "onActivityResult requestCode = " + requestCode + "; resultCode = " + resultCode);
        if (requestCode == 100 && resultCode == 0) {
            Intent intent = new Intent(MainActivity.this, N2NService.class);

            intent.putExtra("ip_address", mIpAddressTIL.getEditText().getText().toString());
            intent.putExtra("net_mask", TextUtils.isEmpty(mNetMaskTIL.getEditText().getText()) ? "255.255.255.0" : mNetMaskTIL.getEditText().getText().toString());
            intent.putExtra("community", mCommunityTIL.getEditText().getText().toString());
            intent.putExtra("encrypt", mEncryptTIL.getEditText().getText().toString());
            intent.putExtra("super_node", mSuperNodeTIL.getEditText().getText().toString());
//                intent.putExtra("spare_super_node", mSpareSuperNodeTIL.getEditText().getText().toString());
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("zhangbz", "MainActivity onResume");

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
    }


}
