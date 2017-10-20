package com.winsun.fruitmix;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.winsun.fruitmix.databinding.ActivityModifyUserPasswordBinding;
import com.winsun.fruitmix.modify.user.ModifyUserPasswordViewModel;
import com.winsun.fruitmix.modify.user.ModifyUserPresenter;
import com.winsun.fruitmix.modify.user.ModifyUserView;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.OperateUserViewModel;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class ModifyUserPasswordActivity extends BaseActivity implements ModifyUserView {

    private ModifyUserPresenter modifyUserPresenter;

    private Context mContext;

    public static final String USER_UUID_KEY = "use_uuid_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityModifyUserPasswordBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_modify_user_password);

        mContext = this;

        final String userUUID = getIntent().getStringExtra(USER_UUID_KEY);

        Toolbar mToolbar = binding.toolbarLayout.toolbar;

        binding.toolbarLayout.title.setTextColor(ContextCompat.getColor(this, R.color.white));

        binding.toolbarLayout.select.setTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_white));

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

        final ModifyUserPasswordViewModel modifyUserPasswordViewModel = new ModifyUserPasswordViewModel();

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.modify_password));

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);

        toolbarViewModel.setBaseView(this);

        toolbarViewModel.showSelect.set(true);

        toolbarViewModel.selectTextResID.set(R.string.finish_text);

        toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
            @Override
            public void onClick() {

                modifyUserPresenter.modifyUserPassword(mContext, modifyUserPasswordViewModel, userUUID);

            }
        });

        binding.setToolbarViewModel(toolbarViewModel);

        binding.setModifyUserPasswordViewModel(modifyUserPasswordViewModel);

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
