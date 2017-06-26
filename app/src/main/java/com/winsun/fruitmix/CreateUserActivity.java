package com.winsun.fruitmix;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.winsun.fruitmix.create.user.CreateUserPresenter;
import com.winsun.fruitmix.create.user.CreateUserPresenterImpl;
import com.winsun.fruitmix.create.user.CreateUserView;
import com.winsun.fruitmix.databinding.ActivityCreateUserBinding;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CreateUserActivity extends BaseActivity implements CreateUserView {

    public static final String TAG = "CreateUserActivity";

    private ProgressDialog mDialog;

    private CreateUserPresenter createUserPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCreateUserBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_create_user);

        CreateUserViewModel createUserViewModel = new CreateUserViewModel();

        createUserPresenter = new CreateUserPresenterImpl(this);

        binding.setCreateUserViewModel(createUserViewModel);

        binding.setCreateUserPresenter(createUserPresenter);

        binding.setBaseView(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dismissDialog();
        mDialog = null;

        createUserPresenter.onDestroy();
    }

    @Override
    public void handleOperationEvent(OperationEvent operationEvent) {

        super.handleOperationEvent(operationEvent);

        action = operationEvent.getAction();

        switch (action) {
            case Util.REMOTE_USER_CREATED: {

                createUserPresenter.handleOperationEvent(operationEvent);

                OperationResult operationResult = operationEvent.getOperationResult();

                if (operationResult.getOperationResultType() == OperationResultType.SUCCEED) {
                    handleCreateUser();
                } else {
                    showToast(operationResult.getResultMessage(this));
                }

                break;
            }
        }

    }

    private void handleCreateUser() {
        setResult(RESULT_OK);

        finish();
    }

    @Override
    public void hideSoftInput() {
        Util.hideSoftInput(this);
    }

    @Override
    public void showProgressDialog(String message) {
        mDialog = ProgressDialog.show(CreateUserActivity.this, null, message, true, false);

    }

    @Override
    public void dismissDialog() {
        if (mDialog != null)
            mDialog.dismiss();
    }

    @Override
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public class CreateUserViewModel {

        private String userName;
        private String userPassword;
        private String userConfirmPassword;

        public final ObservableBoolean userNameErrorEnable = new ObservableBoolean(false);
        public final ObservableBoolean userConfirmPasswordErrorEnable = new ObservableBoolean(false);

        public final ObservableField<String> userNameError = new ObservableField<>();
        public final ObservableField<String> userConfirmPasswordError = new ObservableField<>();


        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserPassword() {
            return userPassword;
        }

        public void setUserPassword(String userPassword) {
            this.userPassword = userPassword;
        }

        public String getUserConfirmPassword() {
            return userConfirmPassword;
        }

        public void setUserConfirmPassword(String userConfirmPassword) {
            this.userConfirmPassword = userConfirmPassword;
        }
    }


}
