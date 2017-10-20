package com.winsun.fruitmix.equipment.manage;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityEquipmentManageBinding;
import com.winsun.fruitmix.equipment.manage.viewmodel.EquipmentManageViewModel;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.user.manage.UserManageActivity;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class EquipmentManageActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = EquipmentManageActivity.class.getSimpleName();

    private SystemSettingDataSource systemSettingDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityEquipmentManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_equipment_manage);

        Toolbar mToolbar = binding.toolbarLayout.toolbar;

        binding.toolbarLayout.title.setTextColor(ContextCompat.getColor(this,R.color.eighty_seven_percent_white));

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.setBaseView(this);

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);
        toolbarViewModel.titleText.set(getString(R.string.equipment_manage));

        binding.setToolbarViewModel(toolbarViewModel);

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

        binding.userManageLayout.setOnClickListener(this);
        binding.equipmentLayout.setOnClickListener(this);
        binding.networkLayout.setOnClickListener(this);
        binding.timeLayout.setOnClickListener(this);
        binding.rebootShutdownLayout.setOnClickListener(this);

        EquipmentManageViewModel equipmentManageViewModel = new EquipmentManageViewModel();

        systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(this);

        equipmentManageViewModel.showUserManage.set(true);

        binding.setEquipmentManageViewModel(equipmentManageViewModel);

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.user_manage_layout:

                Util.startActivity(this, UserManageActivity.class);

                break;
            case R.id.equipment_layout:

                if (checkIsSupported()){
                    Intent intent =new Intent(this,EquipmentInfoActivity.class);
                    intent.putExtra(EquipmentInfoActivity.INFO_TYPE_KEY,EquipmentInfoActivity.EQUIPMENT_INFO);
                    startActivity(intent);
                }

                break;
            case R.id.network_layout:

                if (checkIsSupported()){
                    Intent intent =new Intent(this,EquipmentInfoActivity.class);
                    intent.putExtra(EquipmentInfoActivity.INFO_TYPE_KEY,EquipmentInfoActivity.NETWORK_INFO);
                    startActivity(intent);
                }

                break;
            case R.id.time_layout:

                if (checkIsSupported()){
                    Intent intent =new Intent(this,EquipmentInfoActivity.class);
                    intent.putExtra(EquipmentInfoActivity.INFO_TYPE_KEY,EquipmentInfoActivity.TIME_INFO);
                    startActivity(intent);
                }

                break;
            case R.id.reboot_shutdown_layout:

                if (checkIsSupported())
                    Util.startActivity(this, ShutDownEquipmentActivity.class);

                break;
            default:
                Log.d(TAG, "onClick: should not enter default case");
        }

    }

    private boolean checkIsSupported() {
        if (systemSettingDataSource.getLoginWithWechatCodeOrNot()) {
            showToast(getString(R.string.operation_not_support));
            return false;
        } else
            return true;
    }
}
