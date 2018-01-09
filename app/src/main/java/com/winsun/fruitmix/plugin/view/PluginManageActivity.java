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

        initToolBar(binding, binding.toolbarLayout, getString(R.string.service_manage));

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
