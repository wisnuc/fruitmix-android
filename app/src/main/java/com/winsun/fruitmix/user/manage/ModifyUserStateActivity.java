package com.winsun.fruitmix.user.manage;

import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityModifyUserStateBinding;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class ModifyUserStateActivity extends BaseActivity implements BaseView {

    public static final String MODIFY_USER_UUID_KEY = "modify_user_uuid_key";

    private ModifyUserStatePresenter mModifyUserStatePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityModifyUserStateBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_modify_user_state);

        initToolBar(binding, binding.toolbarLayout, getString(R.string.modify_user_info));

        String modifyUserUUID = getIntent().getStringExtra(MODIFY_USER_UUID_KEY);

        mModifyUserStatePresenter = new ModifyUserStatePresenter(InjectUser.provideRepository(this),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(this), modifyUserUUID,
                this, binding, InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this));

        binding.setPresenter(mModifyUserStatePresenter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mModifyUserStatePresenter.onDestroy();
    }
}
