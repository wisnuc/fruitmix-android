package com.winsun.fruitmix.refactor.ui;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.CreateUserContract;
import com.winsun.fruitmix.refactor.presenter.CreateUserPresenterImpl;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateUserActivity extends BaseActivity implements View.OnClickListener, CreateUserContract.CreateUserView {

    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.user_name_layout)
    TextInputLayout userNameInputLayout;
    @BindView(R.id.user_name_edittext)
    TextInputEditText userNameEditText;
    @BindView(R.id.user_password_layout)
    TextInputLayout userPasswordInputLayout;
    @BindView(R.id.user_password_edittext)
    TextInputEditText userPasswordEditText;
    @BindView(R.id.confirm_password_layout)
    TextInputLayout confirmPasswordLayout;
    @BindView(R.id.confirm_password_edittext)
    TextInputEditText confirmPasswordEditText;
    @BindView(R.id.create_user_button)
    Button createUserBtn;

    private CreateUserContract.CreateUserPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        ButterKnife.bind(this);

        back.setOnClickListener(this);
        createUserBtn.setOnClickListener(this);

        mPresenter = new CreateUserPresenterImpl(Injection.injectDataRepository());
        mPresenter.attachView(this);

        mPresenter.loadUserNames();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.detachView();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back:
                mPresenter.handleBackEvent();
                break;
            case R.id.create_user_button:

                String userName = userNameEditText.getText().toString();

                String password = userPasswordEditText.getText().toString();

                String confirmPassword = confirmPasswordEditText.getText().toString();

                mPresenter.createUser(userName,password,confirmPassword);

                break;
        }

    }

    @Override
    public void showCorrectUserNameFormat() {
        userNameInputLayout.setErrorEnabled(false);
    }

    @Override
    public void showCorrectPasswordFormat() {
        confirmPasswordLayout.setErrorEnabled(false);
    }

    @Override
    public void showEmptyUserName() {
        userNameInputLayout.setErrorEnabled(true);
        userNameInputLayout.setError(getString(R.string.empty_username));
    }

    @Override
    public void showNotUniqueUserName() {
        userNameInputLayout.setErrorEnabled(true);
        userNameInputLayout.setError(getString(R.string.username_not_unique));
    }

    @Override
    public void showNotSamePassword() {
        confirmPasswordLayout.setErrorEnabled(true);
        confirmPasswordLayout.setError(getString(R.string.not_same_password));
    }

    @Override
    public void handleCreateUserFail(OperationResult result) {
        Toast.makeText(this, result.getResultMessage(this), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleCreateUserSucceed() {
        setResult(RESULT_OK);

        finish();
    }

    @Override
    public void finishActivity() {
        finish();
    }

}
