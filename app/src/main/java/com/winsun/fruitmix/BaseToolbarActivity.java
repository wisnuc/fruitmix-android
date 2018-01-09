package com.winsun.fruitmix;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.winsun.fruitmix.databinding.ActivityBaseToolbarBinding;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public abstract class BaseToolbarActivity extends BaseActivity {

    protected ToolbarViewModel toolbarViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ActivityBaseToolbarBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_base_toolbar);

        binding.rootLayout.addView(generateContent());

        initToolbar(binding);

        setSupportActionBar(binding.toolbarLayout.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    protected abstract View generateContent();

    private void initToolbar(ActivityBaseToolbarBinding binding) {

        toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getToolbarTitle());

        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

    }

    protected abstract String getToolbarTitle();

}
