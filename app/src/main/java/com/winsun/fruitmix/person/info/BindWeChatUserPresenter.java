package com.winsun.fruitmix.person.info;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.HttpErrorBodyParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.token.WeChatTokenUserWrapper;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.wxapi.MiniProgram;
import com.winsun.fruitmix.wxapi.WXEntryActivity;

import org.json.JSONException;

/**
 * Created by Administrator on 2017/12/10.
 */

public class BindWeChatUserPresenter implements WXEntryActivity.WXEntryGetTokenCallback {

    public static final String TAG = BindWeChatUserPresenter.class.getSimpleName();

    private UserDataRepository userDataRepository;

    SystemSettingDataSource systemSettingDataSource;

    PersonInfoView personInfoView;

    private PersonInfoDataSource personInfoDataSource;

    private String ticketID;

    private WeChatTokenUserWrapper mWeChatTokenUserWrapper;

    public BindWeChatUserPresenter(UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource,
                                   PersonInfoView personInfoView, PersonInfoDataSource personInfoDataSource) {
        this.userDataRepository = userDataRepository;
        this.systemSettingDataSource = systemSettingDataSource;
        this.personInfoView = personInfoView;
        this.personInfoDataSource = personInfoDataSource;
    }

    public void bindWeChatUser() {

        personInfoView.showProgressDialog(personInfoView.getString(R.string.operating_title, personInfoView.getString(R.string.initiate_binding_request)));

        personInfoDataSource.createBindWeChatUserTicket(new BaseOperateDataCallback<String>() {
            @Override
            public void onSucceed(String data, OperationResult result) {

                personInfoView.dismissDialog();

                ticketID = data;

                WXEntryActivity.setWxEntryGetTokenCallback(BindWeChatUserPresenter.this);

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
    public void succeed(final WeChatTokenUserWrapper weChatTokenUserWrapper) {

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
    public void fail() {

        Log.d(TAG, "fail: get token");

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

        userDataRepository.updateUser(user);

        personInfoView.handleBindSucceed();

    }


    public User getCurrentUser() {
        return userDataRepository.getUserByUUID(systemSettingDataSource.getCurrentLoginUserUUID());
    }

}
