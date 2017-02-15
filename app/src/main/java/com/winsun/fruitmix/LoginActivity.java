package com.winsun.fruitmix;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.eventbus.LoggedInUserRequestEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
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

        mBack.setOnClickListener(this);
        mPwdEdit.setOnFocusChangeListener(this);
        mLoginBtn.setOnClickListener(this);

        Intent intent = getIntent();
        mEquipmentGroupName = intent.getStringExtra(Util.USER_GROUP_NAME);
        String mEquipmentChildName = intent.getStringExtra(Util.USER_NAME);
        mUserUUid = intent.getStringExtra(Util.USER_UUID);
        mGateway = intent.getStringExtra(Util.GATEWAY);

        mEquipmentGroupNameTextView.setText(mEquipmentGroupName);
        mEquipmentChildNameTextView.setText(mEquipmentChildName);

        mUserDefaultPortrait.setText(Util.getUserNameFirstLetter(mEquipmentChildName));
        int color = intent.getIntExtra(Util.USER_BG_COLOR, 0);
        switch (color) {
            case 1:
                mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_blue);
                break;
            case 2:
                mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_green);
                break;
            case 3:
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        switch (action) {
            case Util.REFRESH_VIEW_AFTER_DATA_RETRIEVED: {

                if (mDialog != null)
                    mDialog.dismiss();

                OperationResult operationResult = operationEvent.getOperationResult();

                if (operationResult.getOperationResultType() == OperationResultType.SUCCEED) {

                    User currentUser = LocalCache.getUser(this);

                    LoggedInUser loggedInUser = new LoggedInUser(LocalCache.DeviceID, FNAS.JWT, FNAS.Gateway, mEquipmentGroupName, currentUser);
                    EventBus.getDefault().post(new LoggedInUserRequestEvent(OperationType.CREATE, OperationTargetType.LOCAL_LOGGED_IN_USER, loggedInUser));

                    handleRetrieveUser();
                } else {
                    Toast.makeText(this, operationResult.getResultMessage(this), Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }

    }

    private void handleRetrieveUser() {
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

        mDialog = ProgressDialog.show(mContext, null, getString(R.string.operating_title), true, false);

        FNAS.retrieveRemoteToken(mContext, mGateway, mUserUUid, mPwd);

    }


}
