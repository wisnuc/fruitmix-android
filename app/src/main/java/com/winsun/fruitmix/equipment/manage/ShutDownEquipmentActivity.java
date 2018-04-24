package com.winsun.fruitmix.equipment.manage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ActivityShutDownEquipmentBinding;
import com.winsun.fruitmix.equipment.manage.data.EquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.data.InjectEquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.presenter.ManageEquipmentPresenter;
import com.winsun.fruitmix.equipment.search.data.Equipment;
import com.winsun.fruitmix.equipment.search.data.EquipmentFoundedListener;
import com.winsun.fruitmix.equipment.search.data.EquipmentMDNSSearchManager;
import com.winsun.fruitmix.equipment.search.data.EquipmentSearchManager;
import com.winsun.fruitmix.equipment.search.data.InjectEquipment;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.util.Util;

import java.lang.ref.WeakReference;

public class ShutDownEquipmentActivity extends BaseActivity implements ManageEquipmentPresenter {

    private EquipmentInfoDataSource equipmentInfoDataSource;

    private ProgressDialog enterMaintenanceProgressDialog;

    private EquipmentSearchManager mEquipmentSearchManager;

    private String currentEquipmentIP;

    public static final int RESULT_ENTER_EQUIPMENT_SEARCH_ACTIVITY = 0x2010;

    public static final int EQUIPMENT_SHUTDOWN = 1;
    public static final int EQUIPMENT_REBOOT = 2;

    private int currentEquipmentState = EQUIPMENT_SHUTDOWN;

    public static final int DISCOVERY_TIMEOUT = 1;
    public static final int CONTINUE_DISCOVERY = 2;

    private CustomHandler mCustomHandler;

    private static class CustomHandler extends Handler {

        private WeakReference<ShutDownEquipmentActivity> mShutDownEquipmentActivityWeakReference;

        public CustomHandler(Looper looper, ShutDownEquipmentActivity shutDownEquipmentActivity) {
            super(looper);

            mShutDownEquipmentActivityWeakReference = new WeakReference<>(shutDownEquipmentActivity);

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            ShutDownEquipmentActivity shutDownEquipmentActivity = mShutDownEquipmentActivityWeakReference.get();

            if (shutDownEquipmentActivity == null)
                return;

            switch (msg.what) {

                case CONTINUE_DISCOVERY:

                    Log.d(TAG, "handleMessage: continue discover");

                    shutDownEquipmentActivity.startDiscover();

                    break;
                case DISCOVERY_TIMEOUT:

                    Log.d(TAG, "handleMessage: discover timeout");

                    shutDownEquipmentActivity.currentEquipmentState = EQUIPMENT_REBOOT;

                    break;

            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityShutDownEquipmentBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_shut_down_equipment);

        initToolBar(binding, binding.toolbarLayout, getString(R.string.reboot_shutdown));

        binding.setManageEquipmentPresenter(this);

        equipmentInfoDataSource = InjectEquipmentInfoDataSource.provideInstance(this);

        String currentIPWithHttpHead = InjectSystemSettingDataSource.provideSystemSettingDataSource(this).getCurrentEquipmentIp();

        if (currentIPWithHttpHead.contains(Util.HTTP)) {
            String[] result = currentIPWithHttpHead.split(Util.HTTP);

            currentEquipmentIP = result[1];

        } else {

            currentEquipmentIP = currentIPWithHttpHead;
        }

        mCustomHandler = new CustomHandler(Looper.getMainLooper(), this);

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

        showProgressDialog(getString(R.string.operating_title, getString(R.string.send_request)));

        equipmentInfoDataSource.rebootAndEnterMaintenanceMode(new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                dismissDialog();

                showToast(getString(R.string.success, getString(R.string.send_request)));

                showWaitingForEnterMaintenance();

                startDiscover();

            }

            @Override
            public void onFail(OperationResult result) {

                dismissDialog();

                showToast(getString(R.string.fail, getString(R.string.send_request)));

            }
        });

    }

    private void showWaitingForEnterMaintenance() {

        enterMaintenanceProgressDialog = new ProgressDialog(this);

        enterMaintenanceProgressDialog.setMessage(getString(R.string.operating_title, getString(R.string.reboot_and_enter_maintenance)));

        enterMaintenanceProgressDialog.setCancelable(false);

        enterMaintenanceProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.quit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                cancelDiscover();

                mCustomHandler.removeMessages(DISCOVERY_TIMEOUT);
                mCustomHandler.removeMessages(CONTINUE_DISCOVERY);

                enterEquipmentSearchActivity();
            }
        });

        enterMaintenanceProgressDialog.show();

    }

    private void startDiscover() {

        mEquipmentSearchManager = InjectEquipment.provideEquipmentSearchManager(this);

        mEquipmentSearchManager.startDiscovery(new EquipmentFoundedListener() {
            @Override
            public void call(Equipment equipment) {

                if (currentEquipmentState == EQUIPMENT_SHUTDOWN && checkEquipment(equipment)) {

                    Log.d(TAG, "call: send continue discover");

                    cancelDiscover();

                    mCustomHandler.removeMessages(DISCOVERY_TIMEOUT);

                    mCustomHandler.sendEmptyMessageDelayed(CONTINUE_DISCOVERY, 5 * 1000);

                } else if (currentEquipmentState == EQUIPMENT_REBOOT && checkEquipment(equipment)) {

                    Log.d(TAG, "call: current equipment found");

                    cancelDiscover();

                    enterMaintenanceProgressDialog.dismiss();

                    showEnterMaintenanceDialog();

                }

            }
        });

        mCustomHandler.sendEmptyMessageDelayed(DISCOVERY_TIMEOUT, 5 * 1000);

    }

    private void cancelDiscover() {

        mEquipmentSearchManager.stopDiscovery();

    }

    private boolean checkEquipment(Equipment equipment) {

        return equipment.getHosts().contains(currentEquipmentIP);

    }


    private void showEnterMaintenanceDialog() {

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.enter_maintenance))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        enterEquipmentSearchActivity();

                    }
                }).create().show();

    }

    private void enterEquipmentSearchActivity() {

        setResult(RESULT_ENTER_EQUIPMENT_SEARCH_ACTIVITY);

        finish();

    }


}
