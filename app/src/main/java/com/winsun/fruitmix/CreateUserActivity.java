package com.winsun.fruitmix;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import com.winsun.fruitmix.create.user.CreateUserPresenter;
import com.winsun.fruitmix.create.user.CreateUserPresenterImpl;
import com.winsun.fruitmix.create.user.CreateUserView;
import com.winsun.fruitmix.databinding.ActivityCreateUserBinding;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;


public class CreateUserActivity extends BaseActivity implements CreateUserView {

    public static final String TAG = "CreateUserActivity";

    private CreateUserPresenter createUserPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCreateUserBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_create_user);

        Toolbar mToolbar = binding.toolbarLayout.toolbar;

        if (Util.checkRunningOnLollipopOrHigher()) {
            mToolbar.setElevation(0f);
        }

        binding.toolbarLayout.title.setTextColor(ContextCompat.getColor(this, R.color.white));

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

        CreateUserViewModel createUserViewModel = new CreateUserViewModel();

        createUserPresenter = new CreateUserPresenterImpl(this, InjectUser.provideRepository(this));

        binding.setCreateUserViewModel(createUserViewModel);

        binding.setCreateUserPresenter(createUserPresenter);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.create_user));

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);

        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        createUserPresenter.onDestroy();
    }


    @Override
    public void hideSoftInput() {
        Util.hideSoftInput(this);
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
