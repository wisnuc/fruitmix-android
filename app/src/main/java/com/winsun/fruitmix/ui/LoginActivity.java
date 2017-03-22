package com.winsun.fruitmix.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.common.BaseActivity;
import com.winsun.fruitmix.common.Injection;
import com.winsun.fruitmix.contract.LoginContract;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.presenter.LoginPresenterImpl;
import com.winsun.fruitmix.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity implements View.OnClickListener, EditText.OnFocusChangeListener, LoginContract.LoginView {

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

    private LoginContract.LoginPresenter mPresenter;

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
        String equipmentGroupName = intent.getStringExtra(Util.USER_GROUP_NAME);
        String equipmentChildName = intent.getStringExtra(Util.USER_NAME);
        String userUUid = intent.getStringExtra(Util.USER_UUID);
        String gateway = intent.getStringExtra(Util.GATEWAY);
        int color = intent.getIntExtra(Util.USER_BG_COLOR, 0);

        mPresenter = new LoginPresenterImpl(Injection.injectDataRepository(mContext), equipmentGroupName, equipmentChildName, color, gateway, userUUid);
        mPresenter.attachView(this);
        mPresenter.showEquipmentAndUser();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.detachView();

        mContext = null;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                mPresenter.handleBackEvent();
                break;
            case R.id.login_btn:

                String mPwd = mPwdEdit.getText().toString();

                mPresenter.login(mPwd);

                break;
            default:
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        mPresenter.onFocusChange(hasFocus);
    }

    @Override
    public void setEquipmentGroupNameText(String equipmentGroupNameText) {
        mEquipmentGroupNameTextView.setText(equipmentGroupNameText);
    }

    @Override
    public void setEquipmentChildNameText(String equipmentChildNameText) {
        mEquipmentChildNameTextView.setText(equipmentChildNameText);
    }

    @Override
    public void setUserDefaultPortraitText(String userDefaultPortraitText) {
        mUserDefaultPortrait.setText(userDefaultPortraitText);
    }

    @Override
    public void setUserDefaultPortraitBgColor(int userDefaultPortraitBgColor) {

        switch (userDefaultPortraitBgColor) {
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
    public void handleLoginSucceed() {
        Intent jumpIntent = new Intent(mContext, MainPageActivity.class);
        startActivity(jumpIntent);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void handleLoginFail(OperationResult result) {
        Toast.makeText(this, result.getResultMessage(this), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void hidePwdEditHint() {
        mPwdEdit.setHint("");
    }

    @Override
    public void showPwdEditHint() {
        mPwdEdit.setHint(getString(R.string.password_text));
    }

    @Override
    public void showAlertDialog(int messageResID, int positiveBtnResID, int negativeBtnResID, DialogInterface.OnClickListener positiveBtnOnClickListener, DialogInterface.OnClickListener negativeBtnOnClickListener) {

        new AlertDialog.Builder(mContext).setMessage(getString(messageResID)).setPositiveButton(getString(positiveBtnResID), positiveBtnOnClickListener)
                .setNegativeButton(getString(negativeBtnResID), negativeBtnOnClickListener).setCancelable(false).create().show();
    }
}
