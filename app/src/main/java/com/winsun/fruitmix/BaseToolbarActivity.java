package com.winsun.fruitmix;

import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.winsun.fruitmix.databinding.ActivityBaseToolbarBinding;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public abstract class BaseToolbarActivity extends BaseActivity {

    protected ToolbarViewModel toolbarViewModel;

    protected ActivityBaseToolbarBinding mActivityBaseToolbarBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mActivityBaseToolbarBinding = DataBindingUtil.setContentView(this, R.layout.activity_base_toolbar);

        mActivityBaseToolbarBinding.rootLayout.addView(generateContent());

        initToolbar(mActivityBaseToolbarBinding);

        setSupportActionBar(mActivityBaseToolbarBinding.toolbarLayout.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    protected abstract View generateContent();

    private void initToolbar(ActivityBaseToolbarBinding binding) {

        toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getToolbarTitle());

        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

    }

    protected void setToolbarWhiteStyle(ToolbarViewModel toolbarViewModel){

        toolbarViewModel.titleTextColorResID.set(ContextCompat.getColor(this, R.color.eighty_seven_percent_white));

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);

    }

    protected void setStatusBarToolbarBgColor(int colorResID) {

        Toolbar toolbar = mActivityBaseToolbarBinding.toolbarLayout.toolbar;

        toolbar.setBackgroundColor(ContextCompat.getColor(this, colorResID));

        Util.setStatusBarColor(this, colorResID);

    }

    protected abstract String getToolbarTitle();

}
