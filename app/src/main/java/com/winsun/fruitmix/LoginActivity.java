package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, EditText.OnFocusChangeListener {

    public static final String TAG = "LoginActivity";

    @BindView(R.id.title)
    TextView mTitleTextView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.equipment_group_name)
    TextView mEquipmentGroupNameTextView;

    @BindView(R.id.equipment_child_name)
    TextView mEquipmentChildNameTextView;

    @BindView(R.id.pwd_edit)
    EditText mPwdEdit;

    @BindView(R.id.login_btn)
    Button mLoginBtn;

    @BindView(R.id.user_default_portrait)
    TextView mUserDefaultPortrait;

    private Context mContext;

    private String mUserUUid;
    private String mPwd;
    private String mGateway;

    private String mEquipmentGroupName;

    private ProgressDialog mDialog;

    private LoginUseCase loginUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        mContext = this;

        mTitleTextView.setText(getString(R.string.login));

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPwdEdit.setOnFocusChangeListener(this);
        mLoginBtn.setOnClickListener(this);

        Intent intent = getIntent();
        mEquipmentGroupName = intent.getStringExtra(Util.USER_GROUP_NAME);
        String mEquipmentChildName = intent.getStringExtra(Util.USER_NAME);
        mUserUUid = intent.getStringExtra(Util.USER_UUID);
        mGateway = intent.getStringExtra(Util.GATEWAY);
        int color = intent.getIntExtra(Util.USER_BG_COLOR, 0);

        mEquipmentGroupNameTextView.setText(mEquipmentGroupName);
        mEquipmentChildNameTextView.setText(mEquipmentChildName);

        mUserDefaultPortrait.setText(Util.getUserNameFirstLetter(mEquipmentChildName));

        User user = new User();
        user.setDefaultAvatarBgColor(color);

        mUserDefaultPortrait.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());

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
        Intent jumpIntent = new Intent(mContext, NavPagerActivity.class);
        startActivity(jumpIntent);
        LoginActivity.this.setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.login_btn:
                Util.hideSoftInput(LoginActivity.this);

                if (!Util.getNetworkState(mContext)) {
                    Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }

                mPwd = mPwdEdit.getText().toString();
                login();
                break;
            default:
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            mPwdEdit.setHint("");
        } else {
            mPwdEdit.setHint(getString(R.string.password_text));
        }
    }

    /**
     * use uuid and password to login
     */
    private void login() {

        mDialog = ProgressDialog.show(mContext, null, String.format(getString(R.string.operating_title), getString(R.string.login)), true, false);

        final LoadTokenParam loadTokenParam = new LoadTokenParam(mGateway, mUserUUid, mPwd, mEquipmentGroupName);

        ThreadManager.getInstance().runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                loginInThread(loadTokenParam);
            }
        });


        FNAS.retrieveRemoteToken(mContext, mGateway, mUserUUid, mPwd);

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
