package com.winsun.fruitmix.wxapi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.logged.in.user.LoggedInWeChatUser;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationMoreThanOneStation;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;

import java.util.List;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    public static final String TAG = WXEntryActivity.class.getSimpleName();

    public interface WXEntryGetWeChatCodeCallback {

        void succeed(String code);

        void fail(int resID);

    }

    private static WXEntryGetWeChatCodeCallback wxEntryGetWeChatCodeCallback;

    public static void setWxEntryGetWeChatCodeCallback(WXEntryGetWeChatCodeCallback wxEntryGetWeChatCodeCallback) {
        WXEntryActivity.wxEntryGetWeChatCodeCallback = wxEntryGetWeChatCodeCallback;
    }

    public interface WXEntrySendMiniProgramCallback {

        void succeed();

        void fail();

    }

    private static WXEntrySendMiniProgramCallback wxEntrySendMiniProgramCallback;

    public static void setWxEntrySendMiniProgramCallback(WXEntrySendMiniProgramCallback wxEntrySendMiniProgramCallback) {
        WXEntryActivity.wxEntrySendMiniProgramCallback = wxEntrySendMiniProgramCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: ");

        IWXAPI iwxapi = MiniProgram.registerToWX(this);

        iwxapi.handleIntent(getIntent(), this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

                if (baseResp instanceof SendAuth.Resp) {

                    final SendAuth.Resp resp = (SendAuth.Resp) baseResp;

                    final String code = resp.code;

//                Toast.makeText(this, "baseresp.getType = " + baseResp.getType() + "onResp: state: " + resp.state + " code: " + code, Toast.LENGTH_LONG).show();

                    Log.d(TAG, "onResp: wechat code: " + code + " baseresp.getType = " + baseResp.getType() + " state: " + resp.state);

                    if (wxEntryGetWeChatCodeCallback != null) {

                        finishWhenGetWeChatCodeSucceed(code);

                    } else
                        finish();

                } else if (baseResp instanceof SendMessageToWX.Resp) {

                    if (wxEntrySendMiniProgramCallback != null) {
                        wxEntrySendMiniProgramCallback.succeed();
                        wxEntrySendMiniProgramCallback = null;
                    }

                    finish();

                }

                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;

                if (wxEntryGetWeChatCodeCallback != null)
                    finishWhenGetWeChatCodeFail(result);
                else if (wxEntrySendMiniProgramCallback != null)
                    finishWhenSendMiniProgramFail();
                else
                    finish();

                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;

                if (wxEntryGetWeChatCodeCallback != null)
                    finishWhenGetWeChatCodeFail(result);
                else if (wxEntrySendMiniProgramCallback != null)
                    finishWhenSendMiniProgramFail();
                else
                    finish();

                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                result = R.string.errcode_unsupported;

                if (wxEntryGetWeChatCodeCallback != null)
                    finishWhenGetWeChatCodeFail(result);
                else if (wxEntrySendMiniProgramCallback != null)
                    finishWhenSendMiniProgramFail();
                else
                    finish();

                break;
            default:
                result = R.string.errcode_unknown;

                if (wxEntryGetWeChatCodeCallback != null)
                    finishWhenGetWeChatCodeFail(result);
                else if (wxEntrySendMiniProgramCallback != null)
                    finishWhenSendMiniProgramFail();
                else
                    finish();

                break;

        }

        Log.d(TAG, "onResp: " + getString(result));

    }

    private void finishWhenGetWeChatCodeSucceed(String code) {

        finish();

        wxEntryGetWeChatCodeCallback.succeed(code);
        wxEntryGetWeChatCodeCallback = null;


    }

    private void finishWhenGetWeChatCodeFail(int resID) {

        finish();

        wxEntryGetWeChatCodeCallback.fail(resID);

        wxEntryGetWeChatCodeCallback = null;

    }

    private void finishWhenSendMiniProgramFail() {

        finish();

        wxEntrySendMiniProgramCallback.fail();
        wxEntrySendMiniProgramCallback = null;

    }

}
