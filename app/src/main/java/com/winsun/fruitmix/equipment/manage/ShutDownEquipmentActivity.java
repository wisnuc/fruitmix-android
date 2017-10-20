package com.winsun.fruitmix.equipment.manage;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ActivityShutDownEquipmentBinding;
import com.winsun.fruitmix.equipment.manage.data.EquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.data.InjectEquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.presenter.ManageEquipmentPresenter;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class ShutDownEquipmentActivity extends BaseActivity implements ManageEquipmentPresenter {

    private EquipmentInfoDataSource equipmentInfoDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityShutDownEquipmentBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_shut_down_equipment);

        Toolbar mToolbar = binding.toolbarLayout.toolbar;

        binding.toolbarLayout.title.setTextColor(ContextCompat.getColor(this,R.color.eighty_seven_percent_white));

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.setBaseView(this);

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);
        toolbarViewModel.titleText.set(getString(R.string.reboot_shutdown));

        binding.setToolbarViewModel(toolbarViewModel);

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

        binding.setManageEquipmentPresenter(this);

        equipmentInfoDataSource = InjectEquipmentInfoDataSource.provideInstance(this);

    }

    @Override
    public void shutdownEquipment() {

        showProgressDialog(getString(R.string.operating_title, getString(R.string.shutdown)));

        equipmentInfoDataSource.shutdownEquipment(new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                dismissDialog();

                showToast(getString(R.string.success, getString(R.string.shutdown)));

            }

            @Override
            public void onFail(OperationResult result) {

                dismissDialog();

                showToast(getString(R.string.fail, getString(R.string.shutdown)));

            }
        });

    }

    @Override
    public void rebootEquipment() {

        showProgressDialog(getString(R.string.operating_title, getString(R.string.reboot)));

        equipmentInfoDataSource.rebootEquipment(new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                dismissDialog();

                showToast(getString(R.string.success, getString(R.string.reboot)));

            }

            @Override
            public void onFail(OperationResult result) {

                dismissDialog();

                showToast(getString(R.string.fail, getString(R.string.reboot)));

            }
        });

    }

    @Override
    public void rebootAndEnterMaintenanceMode() {

        showProgressDialog(getString(R.string.operating_title, getString(R.string.reboot_and_enter_maintenance)));

        equipmentInfoDataSource.rebootAndEnterMaintenanceMode(new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                dismissDialog();

                showToast(getString(R.string.success, getString(R.string.reboot_and_enter_maintenance)));

            }

            @Override
            public void onFail(OperationResult result) {

                dismissDialog();

                showToast(getString(R.string.fail, getString(R.string.reboot_and_enter_maintenance)));

            }
        });

    }
}
