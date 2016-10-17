package com.winsun.fruitmix;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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

    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter filter;
    private CustomReceiver customReceiver;

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

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        filter = new IntentFilter(Util.REMOTE_TOKEN_RETRIEVED);
        filter.addAction(Util.REMOTE_DEVICEID_RETRIEVED);
        filter.addAction(Util.REMOTE_USER_RETRIEVED);
        customReceiver = new CustomReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();

        localBroadcastManager.registerReceiver(customReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        localBroadcastManager.unregisterReceiver(customReceiver);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_TOKEN.name());
        intent.putExtra(Util.GATEWAY, mGateway);
        intent.putExtra(Util.USER_UUID, mUserUUid);
        intent.putExtra(Util.PASSWORD, mPwd);
        localBroadcastManager.sendBroadcast(intent);

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

    private class CustomReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Util.REMOTE_TOKEN_RETRIEVED)) {

                OperationResult result = OperationResult.valueOf(intent.getStringExtra(Util.OPERATION_RESULT_NAME));

                switch (result) {
                    case SUCCEED:

                        if (!mGateway.equals(FNAS.Gateway)) {
                            LocalCache.CleanAll();
                        }

                        LocalCache.Init(LoginActivity.this);

                        FNAS.Gateway = mGateway;
                        FNAS.userUUID = mUserUUid;

                        Intent operationIntent = new Intent(Util.OPERATION);
                        operationIntent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
                        operationIntent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_DEVICEID.name());
                        localBroadcastManager.sendBroadcast(operationIntent);

                        break;
                    case FAIL:
                        Util.loginState = false;
                        Snackbar.make(mLoginBtn, getString(R.string.password_error), Snackbar.LENGTH_SHORT).show();
                        break;
                }

            } else if (intent.getAction().equals(Util.REMOTE_DEVICEID_RETRIEVED)) {

                OperationResult result = OperationResult.valueOf(intent.getStringExtra(Util.OPERATION_RESULT_NAME));

                switch (result) {
                    case SUCCEED:

                        Util.loginState = true;

                        FNAS.retrieveUserMap(mContext);

                        LocalCache.saveGateway(FNAS.Gateway, mContext);

                        setGroupNameUserName(mEquipmentGroupName, mEquipmentChildName);
                        setUuidPassword(FNAS.userUUID, mPwd);


                        break;
                    case FAIL:
                        Util.loginState = false;
                        Snackbar.make(mLoginBtn, getString(R.string.password_error), Snackbar.LENGTH_SHORT).show();

                        break;
                }

            } else if (intent.getAction().equals(Util.REMOTE_USER_RETRIEVED)) {

                Intent jumpIntent = new Intent(mContext, NavPagerActivity.class);
                jumpIntent.putExtra(Util.EQUIPMENT_CHILD_NAME, mEquipmentChildName);
                startActivity(jumpIntent);
                LoginActivity.this.setResult(RESULT_OK);
                finish();

            }

        }
    }


}
