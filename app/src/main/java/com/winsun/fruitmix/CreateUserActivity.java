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
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.OperateUserViewModel;
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

        initToolBar(binding,binding.toolbarLayout,getString(R.string.create_user));

        OperateUserViewModel operateUserViewModel = new OperateUserViewModel();

        createUserPresenter = new CreateUserPresenterImpl(InjectUser.provideRepository(this),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(this),this);

        binding.setCreateUserViewModel(operateUserViewModel);

        binding.setCreateUserPresenter(createUserPresenter);

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


}
