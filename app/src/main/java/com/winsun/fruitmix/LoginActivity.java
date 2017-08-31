package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.databinding.ActivityLoginBinding;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginPresenter;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.login.LoginViewModel;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements LoginPresenter {

    public static final String TAG = "LoginActivity";

    Toolbar mToolbar;

    private Context mContext;

    private String mUserUUid;
    private String mGateway;

    private String mEquipmentGroupName;

    private ProgressDialog mDialog;

    private LoginUseCase loginUseCase;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        mContext = this;

        mToolbar = binding.toolbarLayout.toolbar;

        if(Util.checkRunningOnLollipopOrHigher()){
            mToolbar.setElevation(0f);
        }

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.setToolbarNavigationOnClickListener(new ToolbarViewModel.ToolbarNavigationOnClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);

        binding.setToolbarViewModel(toolbarViewModel);

        mToolbar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0f);

        Intent intent = getIntent();
        mEquipmentGroupName = intent.getStringExtra(Util.USER_GROUP_NAME);
        String mEquipmentChildName = intent.getStringExtra(Util.USER_NAME);
        mUserUUid = intent.getStringExtra(Util.USER_UUID);
        mGateway = intent.getStringExtra(Util.GATEWAY);

        loginViewModel = new LoginViewModel();
        loginViewModel.userName.set(mEquipmentChildName);
        loginViewModel.userNameFirstLetter.set(Util.getUserNameFirstLetter(mEquipmentChildName));

        binding.setLoginViewModel(loginViewModel);

        binding.setLoginPresenter(this);

        loginUseCase = InjectLoginUseCase.provideLoginUseCase(mContext);

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

        dismissDialog();
        mDialog = null;
    }

    private void dismissDialog() {
        if (mDialog != null)
            mDialog.dismiss();
    }

    private void startNavPagerActivity() {

        Log.d(TAG, "startNavPagerActivity: ");
        
        Intent jumpIntent = new Intent(mContext, NavPagerActivity.class);
        startActivity(jumpIntent);
        LoginActivity.this.setResult(RESULT_OK);
        finish();
    }

    /**
     * use uuid and password to login
     */
    @Override
    public void login() {

        Util.hideSoftInput(LoginActivity.this);

        if (!Util.getNetworkState(mContext)) {
            Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            return;
        }

        String mPwd = loginViewModel.getPassword();

        mDialog = ProgressDialog.show(mContext, null, String.format(getString(R.string.operating_title), getString(R.string.login)), true, false);

        final LoadTokenParam loadTokenParam = new LoadTokenParam(mGateway, mUserUUid, mPwd, mEquipmentGroupName);

        loginInThread(loadTokenParam);

    }

    private void loginInThread(LoadTokenParam loadTokenParam) {
        loginUseCase.loginWithLoadTokenParam(loadTokenParam, new BaseLoadDataCallback<String>() {
            @Override
            public void onSucceed(List<String> data, OperationResult operationResult) {

                dismissDialog();

                startNavPagerActivity();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                dismissDialog();

                Toast.makeText(LoginActivity.this, operationResult.getResultMessage(mContext), Toast.LENGTH_SHORT).show();

            }
        });
    }


}
