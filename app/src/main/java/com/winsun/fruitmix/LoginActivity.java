package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends Activity implements View.OnClickListener, EditText.OnFocusChangeListener {

    @BindView(R.id.back)
    ImageView mBack;

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

    private String mEquipmentGroupName;
    private String mEquipmentChildName;
    private String mUserUUid;
    private String mPwd;
    private String mGateway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        mContext = this;

        mBack.setOnClickListener(this);
        mPwdEdit.setOnFocusChangeListener(this);
        mLoginBtn.setOnClickListener(this);

        Intent intent = getIntent();
        mEquipmentGroupName = intent.getStringExtra(Util.EQUIPMENT_GROUP_NAME);
        mEquipmentChildName = intent.getStringExtra(Util.EQUIPMENT_CHILD_NAME);
        mUserUUid = intent.getStringExtra(Util.USER_UUID);
        mGateway = intent.getStringExtra(Util.GATEWAY);

        mEquipmentGroupNameTextView.setText(mEquipmentGroupName);
        mEquipmentChildNameTextView.setText(mEquipmentChildName);

        StringBuilder stringBuilder = new StringBuilder();
        String[] splitStrings = mEquipmentChildName.split(" ");
        for (String splitString : splitStrings) {
            stringBuilder.append(splitString.substring(0, 1).toUpperCase());
        }
        mUserDefaultPortrait.setText(stringBuilder.toString());
        int color = (int) (Math.random() * 3);
        switch (color) {
            case 0:
                mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_blue);
                break;
            case 1:
                mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_green);
                break;
            case 2:
                mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_yellow);
                break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        OperationEvent stickyEvent = EventBus.getDefault().removeStickyEvent(OperationEvent.class);

        if (stickyEvent != null) {
            String action = stickyEvent.getAction();

            switch (action) {
                case Util.REMOTE_TOKEN_RETRIEVED: {

                    handleRetrieveToken(operationEvent);

                    break;
                }
                case Util.REMOTE_DEVICEID_RETRIEVED: {

                    handleRetrieveDeviceID(operationEvent);

                    break;
                }
                case Util.REMOTE_USER_RETRIEVED:

                    handleRetrieveUser();

                    break;
            }
        }

    }

    private void handleRetrieveUser() {
        Intent jumpIntent = new Intent(mContext, NavPagerActivity.class);
        jumpIntent.putExtra(Util.EQUIPMENT_CHILD_NAME, mEquipmentChildName);
        startActivity(jumpIntent);
        LoginActivity.this.setResult(RESULT_OK);
        finish();
    }

    private void handleRetrieveDeviceID(OperationEvent operationEvent) {

        OperationResult operationResult = operationEvent.getOperationResult();

        OperationResultType resultType = operationResult.getOperationResultType();

        switch (resultType) {
            case SUCCEED:

                Util.loginState = true;

                FNAS.retrieveUserMap(mContext);

                LocalCache.saveGateway(FNAS.Gateway, mContext);

                setGroupNameUserName(mEquipmentGroupName, mEquipmentChildName);
                setUuidPassword(FNAS.userUUID, mPwd);


                break;
            default:
                Util.loginState = false;
                Snackbar.make(mLoginBtn, operationResult.getResultMessage(this), Snackbar.LENGTH_SHORT).show();

                break;
        }
    }

    private void handleRetrieveToken(OperationEvent operationEvent) {
        OperationResult result = operationEvent.getOperationResult();

        OperationResultType resultType = result.getOperationResultType();

        switch (resultType) {
            case SUCCEED:

                if (!mGateway.equals(FNAS.Gateway)) {
                    LocalCache.CleanAll(LoginActivity.this);
                }

                LocalCache.Init(LoginActivity.this);

                FNAS.Gateway = mGateway;
                FNAS.userUUID = mUserUUid;

                FNAS.retrieveRemoteDeviceID(mContext);

                break;
            default:
                Util.loginState = false;
                Snackbar.make(mLoginBtn, result.getResultMessage(this), Snackbar.LENGTH_SHORT).show();
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.login_btn:
                Util.hideSoftInput(LoginActivity.this);

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

        FNAS.retrieveRemoteToken(mContext, mGateway, mUserUUid, mPwd);

    }


    private void setGroupNameUserName(String groupName, String userName) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.EQUIPMENT_GROUP_NAME, groupName);
        editor.putString(Util.EQUIPMENT_CHILD_NAME, userName);
        editor.apply();
    }

    private void setUuidPassword(String uuid, String password) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.USER_UUID, uuid);
        editor.putString(Util.PASSWORD, password);
        editor.apply();
    }

}
