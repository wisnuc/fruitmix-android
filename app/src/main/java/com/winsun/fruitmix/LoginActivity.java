package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackWrapper;
import com.winsun.fruitmix.databinding.ActivityLoginBinding;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginPresenter;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.login.LoginViewModel;
import com.winsun.fruitmix.token.param.StationTokenParam;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.Util;

public class LoginActivity extends BaseActivity implements LoginPresenter {

    public static final String TAG = "LoginActivity";

    Toolbar mToolbar;

    private Context mContext;

    private String mUserUUid;
    private String mGateway;

    private String mEquipmentGroupName;

    private LoginUseCase loginUseCase;

    private LoginViewModel loginViewModel;

    private ActiveView activeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        mContext = this;

        mToolbar = binding.toolbarLayout.toolbar;

        if (Util.checkRunningOnLollipopOrHigher()) {
            mToolbar.setElevation(0f);
        }

        initToolBar(binding, binding.toolbarLayout, "");

        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

        getSupportActionBar().setElevation(0f);

        Intent intent = getIntent();
        mEquipmentGroupName = intent.getStringExtra(Util.USER_GROUP_NAME);
        String mEquipmentChildName = intent.getStringExtra(Util.USER_NAME);
        mUserUUid = intent.getStringExtra(Util.USER_UUID);
        mGateway = intent.getStringExtra(Util.GATEWAY);

        loginViewModel = new LoginViewModel();
        loginViewModel.userName.set(mEquipmentChildName);
        loginViewModel.userNameFirstLetter.set(Util.getUserNameForAvatar(mEquipmentChildName));

        binding.setLoginViewModel(loginViewModel);

        binding.setLoginPresenter(this);

        loginUseCase = InjectLoginUseCase.provideLoginUseCase(mContext);

        activeView = new ActiveView() {
            @Override
            public boolean isActive() {
                return mContext != null;
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

    }

    private void handleLoginSucceed() {

        Log.d(TAG, "handleLoginSucceed: ");

        LoginActivity.this.setResult(RESULT_OK);
        finish();
    }

    /**
     * use uuid and password to login
     */
    @Override
    public void login() {

        Util.hideSoftInput(LoginActivity.this);

        if (!Util.isNetworkConnected(mContext)) {

            showToast(getString(R.string.no_network));

            return;
        }

        String mPwd = loginViewModel.getPassword();

        showProgressDialog(getString(R.string.operating_title, getString(R.string.login)));

        final StationTokenParam stationTokenParam = new StationTokenParam(mGateway, mUserUUid, mPwd, mEquipmentGroupName);

        loginInThread(stationTokenParam);

    }

    private void loginInThread(StationTokenParam stationTokenParam) {
        loginUseCase.loginWithLoadTokenParam(stationTokenParam, new BaseOperateDataCallbackWrapper<>(new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult operationResult) {

                dismissDialog();

                handleLoginSucceed();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                dismissDialog();

                showToast(operationResult.getResultMessage(mContext));

            }
        }, activeView));
    }

}
