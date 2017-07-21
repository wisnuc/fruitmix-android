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

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
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
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        switch (action) {
            case Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED: {

                EventBus.getDefault().removeStickyEvent(operationEvent);

                if (mDialog != null)
                    mDialog.dismiss();

                OperationResult operationResult = operationEvent.getOperationResult();

                if (operationResult.getOperationResultType() == OperationResultType.SUCCEED) {

                    DBUtils dbUtils = DBUtils.getInstance(this);
                    LocalCache.LocalLoggedInUsers.addAll(dbUtils.getAllLoggedInUser());

                    Log.i(TAG, "LocalLoggedInUsers size: " + LocalCache.LocalLoggedInUsers.size());

                    if (LocalCache.LocalLoggedInUsers.isEmpty()) {

                        LocalCache.setCurrentUploadDeviceID(mContext, LocalCache.DeviceID);
                        LocalCache.setAutoUploadOrNot(mContext, true);

                        saveLoggedUser();

                        startNavPagerActivity(false);

                    } else {

                        saveLoggedUser();

                        startNavPagerActivity(true);

                    }

                } else {

                    Toast.makeText(this, operationResult.getResultMessage(this), Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }

    }

    private void saveLoggedUser() {
        User currentUser = LocalCache.getUser(this);

        LoggedInUser loggedInUser = new LoggedInUser(LocalCache.DeviceID, FNAS.JWT, FNAS.Gateway, mEquipmentGroupName, currentUser);
        long result = DBUtils.getInstance(this).insertLoggedInUserInDB(Collections.singletonList(loggedInUser));

        Log.i(TAG, "saveLoggedUser: result:" + result);

        LocalCache.LocalLoggedInUsers.add(loggedInUser);
    }

    private void startNavPagerActivity(boolean needShowAutoUploadDialog) {
        Intent jumpIntent = new Intent(mContext, NavPagerActivity.class);
        jumpIntent.putExtra(Util.NEED_SHOW_AUTO_UPLOAD_DIALOG, needShowAutoUploadDialog);
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

        FNAS.retrieveRemoteToken(mContext, mGateway, mUserUUid, mPwd);

    }


}
