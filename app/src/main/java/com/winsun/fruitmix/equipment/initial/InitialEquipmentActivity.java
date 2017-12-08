package com.winsun.fruitmix.equipment.initial;

import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityInitialEquipmentBinding;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.util.ArrayList;
import java.util.List;

import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;
import me.drozdzynski.library.steppers.interfaces.OnFinishAction;

public class InitialEquipmentActivity extends BaseActivity {

    private SteppersView.Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityInitialEquipmentBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_initial_equipment);

        initToolbar(binding);

        mConfig = initialStepperViewConfig();

        List<SteppersItem> steppersItems = new ArrayList<>();

        SteppersItem steppersItem = createFirstSteppersItem();

        steppersItems.add(steppersItem);

        SteppersView steppersView = binding.steppersView;
        steppersView.setConfig(mConfig);
        steppersView.setItems(steppersItems);
        steppersView.build();

    }

    private void initToolbar(ActivityInitialEquipmentBinding binding) {
        Toolbar mToolbar = binding.toolbarLayout.toolbar;

        binding.toolbarLayout.title.setTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_white));

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.initial_title));

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);

        toolbarViewModel.setBaseView(this);
    }

    private SteppersView.Config initialStepperViewConfig() {

        SteppersView.Config config = new SteppersView.Config();

        config.setFragmentManager(getSupportFragmentManager());

        config.setOnFinishAction(new OnFinishAction() {
            @Override
            public void onFinish() {

            }
        });

        return config;

    }

    private SteppersItem createFirstSteppersItem(){

        SteppersItem steppersItem = new SteppersItem();
        steppersItem.setLabel("创建磁盘卷");
        steppersItem.setSubLabel("选择磁盘创建新的磁盘卷，所选磁盘的数据会被清除");

        return steppersItem;

    }



}
