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
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;
import com.winsun.fruitmix.usecase.InjectGetAllBindingLocalUserUseCase;

import java.util.List;

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

                showLoadingDialog();

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

        Log.d(TAG, "onResp: " + result);

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

                    final List<Station> stations = operationMoreThanOneStation.getStations();

                    InjectGetAllBindingLocalUserUseCase.provideInstance(WXEntryActivity.this).
                            getAllBindingLocalUser(weChatTokenUserWrapper.getGuid(), weChatTokenUserWrapper.getToken(), new BaseLoadDataCallback<LoggedInWeChatUser>() {
                                @Override
                                public void onSucceed(List<LoggedInWeChatUser> data, OperationResult operationResult) {

                                    if (data.isEmpty()) {

                                        handleLoginFail(new OperationFail("logged in wechat user is empty"));

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
                    "在" + loggedInWeChatUser.getEquipmentName() + "上";

            items[i] = item;

        }

        final SelectItem selectItem = new SelectItem();
        selectItem.setSelectItemPosition(0);

        AlertDialog dialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(WXEntryActivity.this).setTitle("选择一台wisnuc")
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        selectItem.setSelectItemPosition(which);

                    }
                }).setPositiveButton(getString(R.string.login), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        showLoadingDialog();

                        loginUseCase.getUsersAfterChooseStationID(weChatTokenUserWrapper, loggedInWeChatUsers.get(selectItem.getSelectItemPosition()).getStationID(),
                                new BaseOperateDataCallback<Boolean>() {
                                    @Override
                                    public void onSucceed(Boolean data, OperationResult result) {

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

        Toast.makeText(mContext, "登录成功", Toast.LENGTH_SHORT).show();
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
