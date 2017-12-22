package com.winsun.fruitmix.plugin.view;

import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityPluginManageBinding;
import com.winsun.fruitmix.plugin.PluginManagePresenter;
import com.winsun.fruitmix.plugin.PluginViewModel;
import com.winsun.fruitmix.plugin.data.InjectPluginManageDataSource;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class PluginManageActivity extends BaseActivity {

    private PluginManagePresenter pluginManagePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPluginManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_plugin_manage);

        Toolbar mToolbar = binding.toolbarLayout.toolbar;

        binding.toolbarLayout.title.setTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_white));

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.setBaseView(this);

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);
        toolbarViewModel.titleText.set(getString(R.string.modify_user_info));

        binding.setToolbarViewModel(toolbarViewModel);

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

        PluginViewModel pluginViewModel = new PluginViewModel();

        binding.setPluginViewModel(pluginViewModel);

        pluginManagePresenter = new PluginManagePresenter(InjectPluginManageDataSource.provideInstance(this),
                pluginViewModel, this, InjectSystemSettingDataSource.provideSystemSettingDataSource(this));

        binding.setPluginPresenter(pluginManagePresenter);

        pluginManagePresenter.onCreate(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        pluginManagePresenter.onDestroy();
    }
}
