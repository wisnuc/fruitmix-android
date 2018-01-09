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
import com.winsun.fruitmix.user.manage.UserManageActivity;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class EquipmentManageActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = EquipmentManageActivity.class.getSimpleName();

    private SystemSettingDataSource systemSettingDataSource;

    public static final int REQUEST_SHUTDOWN_EQUIPMENT = 0x2001;

    public static final int RESULT_SHUTDOWN_EQUIPMENT = 0x1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityEquipmentManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_equipment_manage);

        initToolBar(binding,binding.toolbarLayout,getString(R.string.equipment_manage));

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

                if (checkIsSupported()) {
                    Intent intent = new Intent(this, EquipmentInfoActivity.class);
                    intent.putExtra(EquipmentInfoActivity.INFO_TYPE_KEY, EquipmentInfoActivity.EQUIPMENT_INFO);
                    startActivity(intent);
                }

                break;
            case R.id.network_layout:

                if (checkIsSupported()) {
                    Intent intent = new Intent(this, EquipmentInfoActivity.class);
                    intent.putExtra(EquipmentInfoActivity.INFO_TYPE_KEY, EquipmentInfoActivity.NETWORK_INFO);
                    startActivity(intent);
                }

                break;
            case R.id.time_layout:

                if (checkIsSupported()) {
                    Intent intent = new Intent(this, EquipmentInfoActivity.class);
                    intent.putExtra(EquipmentInfoActivity.INFO_TYPE_KEY, EquipmentInfoActivity.TIME_INFO);
                    startActivity(intent);
                }

                break;
            case R.id.reboot_shutdown_layout:

                if (checkIsSupported())
                    startActivityForResult(new Intent(this, ShutDownEquipmentActivity.class), REQUEST_SHUTDOWN_EQUIPMENT);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult: requestCode == REQUEST_SHUTDOWN_EQUIPMENT: " + (requestCode == REQUEST_SHUTDOWN_EQUIPMENT)
                + " resultCode == RESULT_ENTER_EQUIPMENT_SEARCH_ACTIVITY: " + (resultCode == ShutDownEquipmentActivity.RESULT_ENTER_EQUIPMENT_SEARCH_ACTIVITY));

        if (requestCode == REQUEST_SHUTDOWN_EQUIPMENT && resultCode == ShutDownEquipmentActivity.RESULT_ENTER_EQUIPMENT_SEARCH_ACTIVITY) {

            setResult(RESULT_SHUTDOWN_EQUIPMENT);

            finish();

        }

    }
}
