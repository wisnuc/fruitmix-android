package com.winsun.fruitmix.person.info;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.data.TokenDataSource;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.wxapi.MiniProgram;
import com.winsun.fruitmix.wxapi.WXEntryActivity;

import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 2017/12/10.
 */

public class BindWeChatUserPresenter implements WXEntryActivity.WXEntryGetWeChatCodeCallback,ActiveView{

    public static final String TAG = BindWeChatUserPresenter.class.getSimpleName();

    private UserDataRepository userDataRepository;

    SystemSettingDataSource systemSettingDataSource;

    PersonInfoView personInfoView;

    private PersonInfoDataSource personInfoDataSource;

    private String ticketID;

    private WeChatTokenUserWrapper mWeChatTokenUserWrapper;

    private TokenDataSource mTokenDataSource;

    public BindWeChatUserPresenter(UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource,
                                   PersonInfoView personInfoView, PersonInfoDataSource personInfoDataSource,TokenDataSource tokenDataSource) {
        this.userDataRepository = userDataRepository;
        this.systemSettingDataSource = systemSettingDataSource;
        this.personInfoView = personInfoView;
        this.personInfoDataSource = personInfoDataSource;
        mTokenDataSource = tokenDataSource;
    }

    public void bindWeChatUser() {

        personInfoView.showProgressDialog(personInfoView.getString(R.string.operating_title, personInfoView.getString(R.string.initiate_binding_request)));

        personInfoDataSource.createBindWeChatUserTicket(new BaseOperateDataCallback<String>() {
            @Override
            public void onSucceed(String data, OperationResult result) {

                personInfoView.dismissDialog();

                ticketID = data;

                WXEntryActivity.setWxEntryGetWeChatCodeCallback(BindWeChatUserPresenter.this);

                IWXAPI iwxapi = MiniProgram.registerToWX(personInfoView.getContext());

                MiniProgram.sendAuthRequest(iwxapi);

            }

            @Override
            public void onFail(OperationResult result) {

                personInfoView.dismissDialog();

                personInfoView.showToast(result.getResultMessage(personInfoView.getContext()));

            }
        });


    }

    @Override
    public void succeed(String code) {

        personInfoView.showProgressDialog(personInfoView.getString(R.string.operating_title,personInfoView.getString(R.string.get_wechat_user_info)));

        mTokenDataSource.getCloudToken(code, new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<WeChatTokenUserWrapper>() {
            @Override
            public void onSucceed(List<WeChatTokenUserWrapper> data, OperationResult operationResult) {

                personInfoView.dismissDialog();

                handleGetTokenUserWrapperSucceed(data.get(0));

            }

            @Override
            public void onFail(OperationResult operationResult) {

                personInfoView.dismissDialog();

                personInfoView.showToast(operationResult.getResultMessage(personInfoView.getContext()));

            }
        }, this));

    }

    private void handleGetTokenUserWrapperSucceed(final WeChatTokenUserWrapper weChatTokenUserWrapper) {

        personInfoView.showProgressDialog(personInfoView.getString(R.string.operating_title, personInfoView.getString(R.string.send_wechat_user_info)));

        mWeChatTokenUserWrapper = weChatTokenUserWrapper;

        personInfoDataSource.fillBindWeChatUserTicket(ticketID, weChatTokenUserWrapper.getToken(), new BaseOperateDataCallback<String>() {
            @Override
            public void onSucceed(String data, OperationResult result) {

                personInfoView.dismissDialog();

                showConfirmBindDialog(ticketID, data, weChatTokenUserWrapper.getNickName());

            }

            @Override
            public void onFail(OperationResult result) {

                personInfoView.dismissDialog();

                personInfoView.showToast(result.getResultMessage(personInfoView.getContext()));

            }
        });

    }

    @Override
    public void fail(int resID) {

        Log.d(TAG, "fail: get wechat code");

        personInfoView.showToast(personInfoView.getString(resID));

    }

    private void showConfirmBindDialog(final String ticketID, final String guid, String wechatUserNickName) {

        new AlertDialog.Builder(personInfoView.getContext()).setMessage(personInfoView.getString(R.string.bind_wechat_user) + " " + wechatUserNickName + " ?")
                .setPositiveButton(personInfoView.getString(R.string.bind), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        personInfoView.showProgressDialog(personInfoView.getString(R.string.operating_title, personInfoView.getString(R.string.bind_wechat_user)));

                        personInfoDataSource.confirmBindWeChatUserTicket(ticketID, guid, true, new BaseOperateDataCallback<String>() {
                            @Override
                            public void onSucceed(String data, OperationResult result) {

                                personInfoView.dismissDialog();

                                personInfoView.showToast(personInfoView.getString(R.string.success, personInfoView.getString(R.string.bind_wechat_user)));

                                handleBindSucceed();

                            }

                            @Override
                            public void onFail(OperationResult result) {

                                personInfoView.dismissDialog();

                                if (result instanceof OperationNetworkException) {

                                    HttpErrorBodyParser parser = new HttpErrorBodyParser();

                                    try {
                                        String messageInBody = parser.parse(((OperationNetworkException) result).getHttpResponseData());

                                        personInfoView.showToast(messageInBody);

                                    } catch (JSONException e) {
                                        e.printStackTrace();

                                        personInfoView.showToast(result.getResultMessage(personInfoView.getContext()));
                                    }

                                } else {
                                    personInfoView.showToast(result.getResultMessage(personInfoView.getContext()));
                                }

                            }
                        });

                    }
                }).setNegativeButton(personInfoView.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                personInfoDataSource.confirmBindWeChatUserTicket(ticketID, guid, false, new BaseOperateDataCallbackImpl<String>());

            }
        }).setCancelable(false).create().show();

    }

    private void handleBindSucceed() {

        User user = getCurrentUser();
        user.setAvatar(mWeChatTokenUserWrapper.getAvatarUrl());
        user.setAssociatedWeChatGUID(mWeChatTokenUserWrapper.getGuid());

        systemSettingDataSource.setCurrentWAToken(mWeChatTokenUserWrapper.getToken());

        userDataRepository.updateUser(user);

        personInfoView.handleBindSucceed();

    }


    public User getCurrentUser() {
        return userDataRepository.getUserByUUID(systemSettingDataSource.getCurrentLoginUserUUID());
    }

    @Override
    public boolean isActive() {
        return personInfoView != null;
    }
}
