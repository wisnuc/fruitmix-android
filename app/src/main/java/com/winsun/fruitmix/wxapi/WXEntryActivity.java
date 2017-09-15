package com.winsun.fruitmix.wxapi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    public static final String TAG = WXEntryActivity.class.getSimpleName();

    private LoginUseCase loginUseCase;

    private ProgressDialog dialog;

    private Context mContext;

    private ThreadManager threadManager;

    public interface WXEntryCallback {

        void loginSucceed();

        void loginFail();

    }

    private static WXEntryCallback wxEntryCallback;

    public static void setWxEntryCallback(WXEntryCallback wxEntryCallback) {
        WXEntryActivity.wxEntryCallback = wxEntryCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        loginUseCase = InjectLoginUseCase.provideLoginUseCase(this);

        threadManager = ThreadManagerImpl.getInstance();

        Log.d(TAG, "onCreate: ");

        IWXAPI iwxapi = MiniProgram.registerToWX(this);

        iwxapi.handleIntent(getIntent(), this);

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

//                Toast.makeText(this, "baseresp.getType = " + baseResp.getType() + "onResp: state: " + resp.state + " code: " + code, Toast.LENGTH_LONG).show();

                Log.d(TAG, "onResp: wechat code: " + code);

                dialog = ProgressDialog.show(this, null, String.format(getString(R.string.operating_title), getString(R.string.login)), true, false);

                loginInThread(code);

                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;

                finishWhenLoginFail();

                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;

                finishWhenLoginFail();

                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                result = R.string.errcode_unsupported;

                finishWhenLoginFail();

                break;
            default:
                result = R.string.errcode_unknown;

                finishWhenLoginFail();

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

                Toast.makeText(mContext, "登录成功", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFail(final OperationResult result) {

                dismissDialog();

                finishWhenLoginFail();

                Toast.makeText(WXEntryActivity.this, result.getResultMessage(mContext), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void startNavPagerActivity() {
        Intent jumpIntent = new Intent(mContext, NavPagerActivity.class);
        startActivity(jumpIntent);

        finish();

        if (wxEntryCallback != null)
            wxEntryCallback.loginSucceed();

    }

    private void finishWhenLoginFail() {

        finish();

        if (wxEntryCallback != null)
            wxEntryCallback.loginFail();

    }

    private void dismissDialog() {
        if (dialog != null)
            dialog.dismiss();
    }
}
