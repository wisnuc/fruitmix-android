package com.winsun.fruitmix;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.winsun.fruitmix.databinding.ActivityModifyUserNameBinding;
import com.winsun.fruitmix.modify.user.ModifyUserPresenter;
import com.winsun.fruitmix.modify.user.ModifyUserView;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.OperateUserViewModel;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class ModifyUserNameActivity extends BaseActivity implements ModifyUserView {

    private ModifyUserPresenter modifyUserPresenter;

    private Context mContext;

    public static final String USER_NAME_KEY = "user_name_key";

    public static final String USER_UUID_KEY = "user_uuid_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityModifyUserNameBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_modify_user_name);

        mContext = this;

        Toolbar mToolbar = binding.toolbarLayout.toolbar;

        binding.toolbarLayout.title.setTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_white));

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

        final OperateUserViewModel operateUserViewModel = new OperateUserViewModel();

        final String userName = getIntent().getStringExtra(USER_NAME_KEY);

        operateUserViewModel.setUserName(userName);

        binding.setOperateUserViewModel(operateUserViewModel);

        final String userUUID = getIntent().getStringExtra(USER_UUID_KEY);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.modify_user_name));

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);

        toolbarViewModel.setBaseView(this);

        toolbarViewModel.showSelect.set(true);

        toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(mContext,R.color.eighty_seven_percent_white));

        toolbarViewModel.selectTextResID.set(R.string.finish_text);

        toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
            @Override
            public void onClick() {

                modifyUserPresenter.modifyUserName(mContext, operateUserViewModel, userUUID,userName);

            }
        });

        binding.setToolbarViewModel(toolbarViewModel);

        modifyUserPresenter = new ModifyUserPresenter(InjectUser.provideRepository(mContext),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(mContext), this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        modifyUserPresenter.onDestroy();
    }

    @Override
    public void hideSoftInput() {
        Util.hideSoftInput(this);
    }
}
