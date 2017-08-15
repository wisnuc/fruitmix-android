package com.winsun.fruitmix.wxapi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.winsun.fruitmix.LoginActivity;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.util.Util;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    public static final String TAG = WXEntryActivity.class.getSimpleName();

    private LoginUseCase loginUseCase;

    private ProgressDialog dialog;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        IWXAPI iwxapi = MiniProgram.registerToWX(this);

        iwxapi.handleIntent(getIntent(), this);

        MiniProgram.sendAuthRequest(iwxapi);

        loginUseCase = InjectLoginUseCase.provideLoginUseCase(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        dismissDialog();
        dialog = null;
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {

        int result = 0;

        switch (baseResp.errCode) {

            case BaseResp.ErrCode.ERR_OK:
                result = R.string.errcode_success;

                final SendAuth.Resp resp = (SendAuth.Resp) baseResp;

                final String code = resp.code;

                Toast.makeText(this, "baseresp.getType = " + baseResp.getType() + "onResp: state: " + resp.state + " code: " + code, Toast.LENGTH_LONG).show();

                Log.d(TAG, "onResp: wechat code: " + code);

                dialog = ProgressDialog.show(this, null, String.format(getString(R.string.operating_title), getString(R.string.login)), true, false);

                ThreadManager.getInstance().runOnCacheThread(new Runnable() {
                    @Override
                    public void run() {
                        loginInThread(code);
                    }
                });

                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;

                finish();

                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;

                finish();

                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                result = R.string.errcode_unsupported;

                finish();

                break;
            default:
                result = R.string.errcode_unknown;

                finish();

                break;

        }

        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

    }

    private void loginInThread(String code) {
        loginUseCase.loginWithWeChatCode(code, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                dismissDialog();

                startNavPagerActivity();

            }

            @Override
            public void onFail(OperationResult result) {

                dismissDialog();

                finish();

                Toast.makeText(WXEntryActivity.this, result.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startNavPagerActivity() {
        Intent jumpIntent = new Intent(mContext, NavPagerActivity.class);
        startActivity(jumpIntent);
        WXEntryActivity.this.setResult(RESULT_OK);
        finish();
    }

    private void dismissDialog() {
        if (dialog != null)
            dialog.dismiss();
    }
}
