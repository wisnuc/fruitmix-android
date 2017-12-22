package com.winsun.fruitmix.plugin;

import android.content.Context;
import android.widget.CompoundButton;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackWrapper;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.plugin.data.PluginManageDataSource;
import com.winsun.fruitmix.plugin.data.PluginStatus;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;

import java.util.List;

/**
 * Created by Administrator on 2017/12/22.
 */

public class PluginManagePresenter implements ActiveView {

    private PluginManageDataSource mPluginManageDataSource;

    private PluginViewModel mPluginViewModel;

    private BaseView mBaseView;

    private SystemSettingDataSource mSystemSettingDataSource;

    public PluginManagePresenter(PluginManageDataSource pluginManageDataSource, PluginViewModel pluginViewModel, BaseView baseView,
                                 SystemSettingDataSource systemSettingDataSource) {
        mPluginManageDataSource = pluginManageDataSource;
        mPluginViewModel = pluginViewModel;
        mBaseView = baseView;
        mSystemSettingDataSource = systemSettingDataSource;
    }

    public void onCreate(Context context) {

        if (mSystemSettingDataSource.getLoginWithWechatCodeOrNot()) {

            mPluginViewModel.pluginUpdateEnable.set(false);

        } else {

            mPluginViewModel.pluginUpdateEnable.set(true);

            mPluginManageDataSource.getPluginStatus(PluginManageDataSource.TYPE_SAMBA, new BaseLoadDataCallbackWrapper<>(
                    new BaseLoadDataCallback<PluginStatus>() {
                        @Override
                        public void onSucceed(List<PluginStatus> data, OperationResult operationResult) {

                            mPluginViewModel.sambaOpenOrNot.set(data.get(0).isActive());
                        }

                        @Override
                        public void onFail(OperationResult operationResult) {

                        }
                    }, this
            ));

            mPluginManageDataSource.getPluginStatus(PluginManageDataSource.TYPE_DLNA, new BaseLoadDataCallbackWrapper<>(
                    new BaseLoadDataCallback<PluginStatus>() {
                        @Override
                        public void onSucceed(List<PluginStatus> data, OperationResult operationResult) {

                            mPluginViewModel.dlnaOpenOrNot.set(data.get(0).isActive());
                        }

                        @Override
                        public void onFail(OperationResult operationResult) {

                        }
                    }, this
            ));


        }

        mPluginManageDataSource.getBTStatus(new BaseLoadDataCallbackWrapper<>(
                new BaseLoadDataCallback<PluginStatus>() {
                    @Override
                    public void onSucceed(List<PluginStatus> data, OperationResult operationResult) {

                        mPluginViewModel.btOpenOrNot.set(data.get(0).isActive());
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                    }
                }, this
        ));

    }

    public void onDestroy() {
        mBaseView = null;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {

            case R.id.samba_switch:
                handleSambaStatusCheckedChange(buttonView.getContext(), buttonView, isChecked);
                break;
            case R.id.dlna_switch:
                handleDLNAStatusCheckedChange(buttonView.getContext(), buttonView, isChecked);
                break;
            case R.id.bt_switch:
                handleBTStatusCheckedChange(buttonView.getContext(), buttonView, isChecked);
                break;

        }

    }


    private void handleSambaStatusCheckedChange(final Context context, final CompoundButton button, final boolean isChecked) {

        final boolean sambaOpenOrNot = mPluginViewModel.sambaOpenOrNot.get();

        if (sambaOpenOrNot == isChecked)
            return;

        String action = isChecked ? "start" : "stop";

        final String actionStr = isChecked ? context.getString(R.string.start) : context.getString(R.string.stop);

        mBaseView.showProgressDialog(context.getString(R.string.operating_title, actionStr + context.getString(R.string.samba_service)));

        mPluginManageDataSource.updatePluginStatus(PluginManageDataSource.TYPE_SAMBA, action, new BaseOperateDataCallbackWrapper<Void>(
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                        mBaseView.dismissDialog();

                        mPluginViewModel.sambaOpenOrNot.set(isChecked);

                        mBaseView.showToast(context.getString(R.string.success, actionStr + context.getString(R.string.samba_service)));
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mBaseView.dismissDialog();

                        button.setChecked(sambaOpenOrNot);

                        mBaseView.showToast(operationResult.getResultMessage(context));
                    }
                }, this
        ));

    }

    private void handleDLNAStatusCheckedChange(final Context context, final CompoundButton button, final boolean isChecked) {

        final boolean dlnaOpenOrNot = mPluginViewModel.dlnaOpenOrNot.get();

        if (dlnaOpenOrNot == isChecked)
            return;

        String action = isChecked ? "start" : "stop";

        final String actionStr = isChecked ? context.getString(R.string.start) : context.getString(R.string.stop);

        mBaseView.showProgressDialog(context.getString(R.string.operating_title, actionStr + context.getString(R.string.dlna_service)));

        mPluginManageDataSource.updatePluginStatus(PluginManageDataSource.TYPE_DLNA, action, new BaseOperateDataCallbackWrapper<Void>(
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                        mBaseView.dismissDialog();

                        mPluginViewModel.dlnaOpenOrNot.set(isChecked);

                        mBaseView.showToast(context.getString(R.string.success, actionStr + context.getString(R.string.dlna_service)));
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mBaseView.dismissDialog();

                        button.setChecked(dlnaOpenOrNot);

                        mBaseView.showToast(operationResult.getResultMessage(context));
                    }
                }, this
        ));

    }

    private void handleBTStatusCheckedChange(final Context context, final CompoundButton button, final boolean isChecked) {

        final boolean btOpenOrNot = mPluginViewModel.btOpenOrNot.get();

        if (btOpenOrNot == isChecked)
            return;

        String op = isChecked ? "start" : "close";

        final String actionStr = isChecked ? context.getString(R.string.start) : context.getString(R.string.stop);

        mBaseView.showProgressDialog(context.getString(R.string.operating_title, actionStr + context.getString(R.string.dlna_service)));

        mPluginManageDataSource.updateBTStatus(op, new BaseOperateDataCallbackWrapper<Void>(
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                        mBaseView.dismissDialog();

                        mPluginViewModel.btOpenOrNot.set(isChecked);

                        mBaseView.showToast(context.getString(R.string.success, actionStr + context.getString(R.string.bt_download_service)));
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mBaseView.dismissDialog();

                        button.setChecked(btOpenOrNot);

                        mBaseView.showToast(operationResult.getResultMessage(context));
                    }
                }, this
        ));

    }


    @Override
    public boolean isActive() {
        return mBaseView != null;
    }
}
