package com.winsun.fruitmix.wxapi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.logged.in.user.LoggedInWeChatUser;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationMoreThanOneStation;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.token.InjectTokenRemoteDataSource;
import com.winsun.fruitmix.token.TokenDataSource;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;
import com.winsun.fruitmix.usecase.InjectGetAllBindingLocalUserUseCase;

import java.util.List;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    public static final String TAG = WXEntryActivity.class.getSimpleName();

    private LoginUseCase loginUseCase;

    private TokenDataSource tokenDataSource;

    private ProgressDialog dialog;

    private Context mContext;

    private ThreadManager threadManager;

    public interface WXEntryLoginCallback {

        void loginSucceed();

        void loginFail();

    }

    public interface WXEntryGetTokenCallback {

        void succeed(String token);

        void fail();

    }

    private static WXEntryGetTokenCallback wxEntryGetTokenCallback;

    public static void setWxEntryGetTokenCallback(WXEntryGetTokenCallback wxEntryGetTokenCallback) {
        WXEntryActivity.wxEntryGetTokenCallback = wxEntryGetTokenCallback;
    }

    private static WXEntryLoginCallback wxEntryLoginCallback;

    public static void setWxEntryLoginCallback(WXEntryLoginCallback wxEntryLoginCallback) {
        WXEntryActivity.wxEntryLoginCallback = wxEntryLoginCallback;
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

                if (wxEntryLoginCallback != null) {

                    showLoadingDialog();

                    loginInThread(code);

                } else if (wxEntryGetTokenCallback != null) {

                    showGetTokneDialog();

                    getTokenInThread(code);

                }

                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;

                if (wxEntryLoginCallback != null)
                    finishWhenLoginFail();
                else if (wxEntryGetTokenCallback != null)
                    finishWhenGetTokenFail();

                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;

                if (wxEntryLoginCallback != null)
                    finishWhenLoginFail();
                else if (wxEntryGetTokenCallback != null)
                    finishWhenGetTokenFail();

                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                result = R.string.errcode_unsupported;

                if (wxEntryLoginCallback != null)
                    finishWhenLoginFail();
                else if (wxEntryGetTokenCallback != null)
                    finishWhenGetTokenFail();

                break;
            default:
                result = R.string.errcode_unknown;

                if (wxEntryLoginCallback != null)
                    finishWhenLoginFail();
                else if (wxEntryGetTokenCallback != null)
                    finishWhenGetTokenFail();

                break;

        }

        Log.d(TAG, "onResp: " + result);

    }

    private void showGetTokneDialog() {
        dialog = ProgressDialog.show(this, null, String.format(getString(R.string.operating_title), getString(R.string.get_wechat_user_info)), true, false);
    }

    private void getTokenInThread(String code) {

        tokenDataSource = InjectTokenRemoteDataSource.provideTokenDataSource(this);

        tokenDataSource.getToken(code, new BaseLoadDataCallback<WeChatTokenUserWrapper>() {
            @Override
            public void onSucceed(List<WeChatTokenUserWrapper> data, OperationResult operationResult) {

                dismissDialog();

                finishWhenGetTokenSucceed(data.get(0).getToken());

            }

            @Override
            public void onFail(OperationResult operationResult) {

                dismissDialog();

                finishWhenGetTokenFail();

                Toast.makeText(WXEntryActivity.this, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void finishWhenGetTokenSucceed(String token) {

        finish();

        if (wxEntryGetTokenCallback != null) {

            wxEntryGetTokenCallback.succeed(token);
            wxEntryGetTokenCallback = null;

        }

    }

    private void finishWhenGetTokenFail() {

        finish();

        if (wxEntryGetTokenCallback != null) {

            wxEntryGetTokenCallback.fail();
            wxEntryGetTokenCallback = null;

        }

    }

    private void showLoadingDialog() {
        dialog = ProgressDialog.show(this, null, String.format(getString(R.string.operating_title), getString(R.string.login)), true, false);
    }

    private void loginInThread(String code) {
        loginUseCase.loginWithWeChatCode(code, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                if (result.getOperationResultType() == OperationResultType.MORE_THAN_ONE_STATION) {

                    OperationMoreThanOneStation operationMoreThanOneStation = (OperationMoreThanOneStation) result;

                    final WeChatTokenUserWrapper weChatTokenUserWrapper = operationMoreThanOneStation.getWeChatTokenUserWrapper();

                    InjectGetAllBindingLocalUserUseCase.provideInstance(WXEntryActivity.this).
                            getAllBindingLocalUser(weChatTokenUserWrapper.getGuid(), weChatTokenUserWrapper.getToken(), new BaseLoadDataCallback<LoggedInWeChatUser>() {
                                @Override
                                public void onSucceed(List<LoggedInWeChatUser> data, OperationResult operationResult) {

                                    if (data.isEmpty()) {

                                        handleLoginFail(new OperationFail(R.string.no_binding_user_in_nas));

                                    } else {

                                        dismissDialog();

                                        showChooseStationDialog(weChatTokenUserWrapper, data);

                                    }

                                }

                                @Override
                                public void onFail(OperationResult operationResult) {

                                    handleLoginFail(operationResult);

                                }
                            });

                } else {

                    handleLoginSucceed();

                }

            }

            @Override
            public void onFail(final OperationResult result) {

                handleLoginFail(result);

            }
        });
    }

    private class SelectItem {

        private int selectItemPosition;

        int getSelectItemPosition() {
            return selectItemPosition;
        }

        void setSelectItemPosition(int selectItemPosition) {
            this.selectItemPosition = selectItemPosition;
        }
    }

    private void showChooseStationDialog(final WeChatTokenUserWrapper weChatTokenUserWrapper, final List<LoggedInWeChatUser> loggedInWeChatUsers) {

        String[] items = new String[loggedInWeChatUsers.size()];

        for (int i = 0; i < loggedInWeChatUsers.size(); i++) {

            LoggedInWeChatUser loggedInWeChatUser = loggedInWeChatUsers.get(i);

            String item = loggedInWeChatUser.getUser().getUserName() + "\n" +
                    String.format(getString(R.string.on_equipment), loggedInWeChatUser.getEquipmentName());

            items[i] = item;

        }

        final SelectItem selectItem = new SelectItem();
        selectItem.setSelectItemPosition(0);

        AlertDialog dialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(WXEntryActivity.this).setTitle(getString(R.string.select_one_winsuc))
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        selectItem.setSelectItemPosition(which);

                    }
                }).setPositiveButton(getString(R.string.login), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        showLoadingDialog();

                        final String stationID = loggedInWeChatUsers.get(selectItem.getSelectItemPosition()).getStationID();

                        loginUseCase.getUsersAfterChooseStationID(weChatTokenUserWrapper, stationID,
                                new BaseOperateDataCallback<Boolean>() {
                                    @Override
                                    public void onSucceed(Boolean data, OperationResult result) {

                                        loginUseCase.handleLoginWithWeChatCodeSucceed(weChatTokenUserWrapper.getGuid(), weChatTokenUserWrapper.getToken(), stationID);

                                        handleLoginSucceed();

                                    }

                                    @Override
                                    public void onFail(OperationResult result) {

                                        handleLoginFail(result);

                                    }
                                });

                    }
                }).setCancelable(false);

        dialog = builder.create();

        dialog.show();

    }

    private void handleLoginSucceed() {
        dismissDialog();

        startNavPagerActivity();

        Toast.makeText(mContext, String.format(getString(R.string.success), getString(R.string.login)), Toast.LENGTH_SHORT).show();
    }

    private void handleLoginFail(OperationResult result) {
        dismissDialog();

        finishWhenLoginFail();

        Toast.makeText(WXEntryActivity.this, result.getResultMessage(mContext), Toast.LENGTH_SHORT).show();
    }

    private void startNavPagerActivity() {
        Intent jumpIntent = new Intent(mContext, NavPagerActivity.class);
        startActivity(jumpIntent);

        finish();

        if (wxEntryLoginCallback != null) {

            wxEntryLoginCallback.loginSucceed();
            wxEntryLoginCallback = null;

        }


    }

    private void finishWhenLoginFail() {

        finish();

        if (wxEntryLoginCallback != null) {

            wxEntryLoginCallback.loginFail();
            wxEntryLoginCallback = null;

        }


    }

    private void dismissDialog() {
        if (dialog != null)
            dialog.dismiss();
    }
}
