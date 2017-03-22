package com.winsun.fruitmix.presenter;

import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.business.LoadTokenParam;
import com.winsun.fruitmix.business.callback.LoadDeviceIdOperationCallback;
import com.winsun.fruitmix.business.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.business.callback.UserOperationCallback;
import com.winsun.fruitmix.contract.LoginContract;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.Util;

import java.util.List;

/**
 * Created by Administrator on 2017/2/9.
 */

public class LoginPresenterImpl implements LoginContract.LoginPresenter {

    public static final String TAG = LoginPresenterImpl.class.getSimpleName();

    private LoginContract.LoginView mView;
    private DataRepository mRepository;

    private String mEquipmentGroupName;
    private String mEquipmentChildName;
    private int mUserDefaultBgColor;
    private String mUserUUid;
    private String mGateway;

    public LoginPresenterImpl(DataRepository repository, String equipmentGroupName, String equipmentChildName, int userDefaultBgColor, String gateway, String userUUID) {
        mRepository = repository;

        mEquipmentGroupName = equipmentGroupName;
        mEquipmentChildName = equipmentChildName;
        mUserDefaultBgColor = userDefaultBgColor;
        mGateway = gateway;
        mUserUUid = userUUID;
    }

    @Override
    public void login(String userPassword) {

        mView.hideSoftInput();

        if (!mView.isNetworkAlive()) {
            mView.showNoNetwork();
            return;
        }

        mView.showDialog();

        LoadTokenParam param = new LoadTokenParam(mGateway, mUserUUid, userPassword);
        mRepository.loadRemoteTokenWhenLoginInThread(param, new LoadTokenOperationCallback.LoadTokenCallback() {
            @Override
            public void onLoadSucceed(OperationResult result, String token) {

                mRepository.loadRemoteDeviceIDInThread(new LoadDeviceIdOperationCallback.LoadDeviceIDCallback() {
                    @Override
                    public void onLoadSucceed(OperationResult result, String deviceID) {

                        mRepository.loadUsersInThread(new UserOperationCallback.LoadUsersCallback() {
                            @Override
                            public void onLoadSucceed(OperationResult operationResult, List<User> users) {
                                handleLoginSucceed();
                            }

                            @Override
                            public void onLoadFail(OperationResult operationResult) {
                                if (mView == null) return;

                                mView.dismissDialog();

                                mView.handleLoginFail(operationResult);
                            }

                        });

                    }

                    @Override
                    public void onLoadFail(OperationResult result) {

                        if (mView == null) return;

                        mView.dismissDialog();

                        mView.handleLoginFail(result);
                    }
                });

            }

            @Override
            public void onLoadFail(OperationResult result) {

                if (mView == null) return;

                mView.dismissDialog();

                mView.handleLoginFail(result);
            }
        });
    }

    private void handleLoginSucceed() {
        if (mView == null) return;

        mView.dismissDialog();

        List<LoggedInUser> loggedInUsers = mRepository.loadLoggedInUserInMemory();

        Log.i(TAG, "LocalLoggedInUsers size: " + loggedInUsers.size());

        if (loggedInUsers.isEmpty()) {

            mRepository.saveCurrentUploadDeviceID();

            mRepository.saveAutoUploadOrNot(true);

            mRepository.saveLoggedInUser(mEquipmentGroupName);

            loadData();

            mView.handleLoginSucceed();

        } else {

            mRepository.saveLoggedInUser(mEquipmentGroupName);

            mView.showAlertDialog(R.string.need_auto_upload, R.string.ok, R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    mRepository.saveCurrentUploadDeviceID();

                    mRepository.saveAutoUploadOrNot(true);

                    loadData();

                    mView.handleLoginSucceed();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mRepository.saveAutoUploadOrNot(false);

                    loadData();

                    mView.handleLoginSucceed();
                }
            });

        }
    }


    private void loadData() {
        mRepository.loadMediasInThread(null);
        mRepository.loadMediaSharesInThread(null);
    }

    @Override
    public void onFocusChange(boolean hasFocus) {

        if (hasFocus) {
            mView.hidePwdEditHint();
        } else {
            mView.showPwdEditHint();
        }

    }

    @Override
    public void attachView(LoginContract.LoginView view) {
        mView = view;
    }

    @Override
    public void detachView() {

        mView.dismissDialog();

        mView = null;
    }

    @Override
    public void showEquipmentAndUser() {
        mView.setEquipmentGroupNameText(mEquipmentGroupName);
        mView.setEquipmentChildNameText(mEquipmentChildName);

        mView.setUserDefaultPortraitText(Util.getUserNameFirstLetter(mEquipmentChildName));
        mView.setUserDefaultPortraitBgColor(mUserDefaultBgColor);
    }

    @Override
    public void handleBackEvent() {
        mView.finishActivity();
    }

    @Override
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
