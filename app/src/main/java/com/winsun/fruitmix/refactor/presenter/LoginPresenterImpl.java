package com.winsun.fruitmix.refactor.presenter;

import android.content.Intent;

import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.business.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.refactor.contract.LoginContract;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/2/9.
 */

public class LoginPresenterImpl implements LoginContract.LoginPresenter {

    private LoginContract.LoginView mView;
    private DataRepository mRepository;

    private String mEquipmentGroupName;
    private String mEquipmentChildName;
    private int mUserDefaultBgColor;
    private String mUserUUid;
    private String mGateway;

    public LoginPresenterImpl(DataRepository repository, String equipmentGroupName, String equipmentChildName, int userDefaultBgColor, String gateway, String uesrUUID) {
        mRepository = repository;

        mEquipmentGroupName = equipmentGroupName;
        mEquipmentChildName = equipmentChildName;
        mUserDefaultBgColor = userDefaultBgColor;
        mGateway = gateway;
        mUserUUid = uesrUUID;
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
        mRepository.loadRemoteToken(param, new LoadTokenOperationCallback.LoadTokenCallback() {
            @Override
            public void onLoadSucceed(OperationResult result, String token) {
                mView.dismissDialog();

                mView.handleLoginSucceed();
            }

            @Override
            public void onLoadFail(OperationResult result) {
                mView.dismissDialog();

                mView.handleLoginFail(result);
            }
        });
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
