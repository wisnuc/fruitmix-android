package com.winsun.fruitmix.firmware;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackWrapper;
import com.winsun.fruitmix.databinding.ActivityFirmwareBinding;
import com.winsun.fruitmix.databinding.ConfirmInstallAppifiBinding;
import com.winsun.fruitmix.firmware.data.FirmwareDataSource;
import com.winsun.fruitmix.firmware.model.Firmware;
import com.winsun.fruitmix.firmware.model.FirmwareState;
import com.winsun.fruitmix.firmware.model.NewFirmwareState;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Administrator on 2017/12/28.
 */

public class FirmwarePresenter implements ActiveView {

    public static final String TAG = FirmwarePresenter.class.getSimpleName();

    private ToolbarViewModel mToolbarViewModel;
    private LoadingViewModel mLoadingViewModel;
    private NoContentViewModel mNoContentViewModel;

    private FirmwareViewModel mFirmwareViewModel;

    private FirmwareDataSource mFirmwareDataSource;

    private FirmwareView mFirmwareView;

    private Firmware currentFirmware;

    private static final int REFRESH_VIEW = 0x1001;

    private static class RefreshFirmwareHandler extends Handler {

        private WeakReference<FirmwarePresenter> mFirmwarePresenterWeakReference;

        /**
         * Use the provided {@link Looper} instead of the default one.
         *
         * @param looper The looper, must not be null.
         */
        public RefreshFirmwareHandler(Looper looper, FirmwarePresenter firmwarePresenter) {
            super(looper);

            mFirmwarePresenterWeakReference = new WeakReference<>(firmwarePresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                case REFRESH_VIEW:

                    Log.d(TAG, "handleMessage: refreshView after 2 seconds");

                    FirmwarePresenter presenter = mFirmwarePresenterWeakReference.get();

                    if (presenter != null)
                        presenter.refreshView();

                    break;
            }

        }
    }

    private RefreshFirmwareHandler mRefreshFirmwareHandler;

    private ActivityFirmwareBinding mActivityFirmwareBinding;

    FirmwarePresenter(ToolbarViewModel toolbarViewModel, LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel,
                      FirmwareViewModel firmwareViewModel, FirmwareDataSource firmwareDataSource, FirmwareView firmwareView,
                      ActivityFirmwareBinding binding) {
        mToolbarViewModel = toolbarViewModel;
        mLoadingViewModel = loadingViewModel;
        mNoContentViewModel = noContentViewModel;
        mFirmwareViewModel = firmwareViewModel;
        mFirmwareDataSource = firmwareDataSource;
        mFirmwareView = firmwareView;
        mActivityFirmwareBinding = binding;

        mRefreshFirmwareHandler = new RefreshFirmwareHandler(Looper.getMainLooper(), this);
    }

    public void refreshView() {

        mFirmwareDataSource.getFirmware(new BaseLoadDataCallbackWrapper<>(
                new BaseLoadDataCallback<Firmware>() {
                    @Override
                    public void onSucceed(List<Firmware> data, OperationResult operationResult) {

                        mLoadingViewModel.showLoading.set(false);
                        mNoContentViewModel.showNoContent.set(false);

                        currentFirmware = data.get(0);

                        mFirmwareViewModel.setData(mActivityFirmwareBinding,currentFirmware, mFirmwareView.getContext());

                        handleGetFirmwareFinished();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mLoadingViewModel.showLoading.set(false);
                        mNoContentViewModel.showNoContent.set(true);

                        handleGetFirmwareFinished();

                    }
                }, this
        ));

    }

    private void handleGetFirmwareFinished() {

        if (!mToolbarViewModel.showMenu.get()) {

            mToolbarViewModel.showMenu.set(true);
            mToolbarViewModel.setToolbarMenuBtnOnClickListener(new ToolbarViewModel.ToolbarMenuBtnOnClickListener() {
                @Override
                public void onClick() {
                    checkUpdate();
                }
            });

            checkUpdate();

        }

        if (mFirmwareView != null)
            mRefreshFirmwareHandler.sendEmptyMessageDelayed(REFRESH_VIEW, 2 * 1000);

    }

    public void onDestroy() {
        mFirmwareView = null;
    }


    @Override
    public boolean isActive() {
        return mFirmwareView != null;
    }

    public void installOrRetryOnClick(Context context) {

        if (currentFirmware.getNewFirmwareState() == NewFirmwareState.FAILED) {

            reDownloadAppifi(currentFirmware.getNewFirmwareVersion(),context);

        } else if (currentFirmware.getNewFirmwareState() == NewFirmwareState.READY) {

            installAppifi(currentFirmware.getNewFirmwareVersion(), context);

        }

    }

    private void installAppifi(final String version, final Context context) {

        ConfirmInstallAppifiBinding binding = ConfirmInstallAppifiBinding.inflate(LayoutInflater.from(context),
                null, false);

        binding.installInstruction.setText(context.getString(R.string.install_instruction, version));

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.firmware_install))
                .setView(binding.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        installFirmware(version,context);

                    }
                })
                .create().show();

    }

    private void installFirmware(String version, final Context context) {

        mFirmwareView.showProgressDialog(context.getString(R.string.operating_title,context.getString(R.string.send_request)));

        mFirmwareDataSource.installFirmware(version, new BaseOperateDataCallbackWrapper<Void>(
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                        mFirmwareView.dismissDialog();

                        mFirmwareView.showToast(context.getString(R.string.success,context.getString(R.string.send_request)));

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mFirmwareView.dismissDialog();

                        mFirmwareView.showToast(context.getString(R.string.fail,context.getString(R.string.send_request)));

                    }
                }, this
        ));
    }

    private void reDownloadAppifi(String version,final Context context) {

        mFirmwareView.showProgressDialog(context.getString(R.string.operating_title,context.getString(R.string.send_request)));

        mFirmwareDataSource.updateDownloadFirmwareState(version, "Ready", new BaseOperateDataCallbackWrapper<Void>(
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                        mFirmwareView.dismissDialog();

                        mFirmwareView.showToast(context.getString(R.string.success,context.getString(R.string.send_request)));

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mFirmwareView.dismissDialog();

                        mFirmwareView.showToast(context.getString(R.string.fail,context.getString(R.string.send_request)));

                    }
                }, this
        ));

    }

    public void startOrStopOnClick(final Context context) {

        if (currentFirmware.getFirmwareState() == FirmwareState.STARTED) {

            mFirmwareDataSource.updateFirmwareState("Stopped", new BaseOperateDataCallbackWrapper<Void>(
                    new BaseOperateDataCallback<Void>() {
                        @Override
                        public void onSucceed(Void data, OperationResult result) {

                            mFirmwareView.showToast(context.getString(R.string.success,context.getString(R.string.send_request)));

                        }

                        @Override
                        public void onFail(OperationResult operationResult) {

                            mFirmwareView.showToast(context.getString(R.string.fail,context.getString(R.string.send_request)));

                        }
                    }, this
            ));

        } else if (currentFirmware.getFirmwareState() == FirmwareState.STOPPED) {

            mFirmwareDataSource.updateFirmwareState("Started", new BaseOperateDataCallbackWrapper<Void>(
                    new BaseOperateDataCallback<Void>() {
                        @Override
                        public void onSucceed(Void data, OperationResult result) {

                            mFirmwareView.showToast(context.getString(R.string.success,context.getString(R.string.send_request)));

                        }

                        @Override
                        public void onFail(OperationResult operationResult) {

                            mFirmwareView.showToast(context.getString(R.string.fail,context.getString(R.string.send_request)));

                        }
                    }, this
            ));


        }

    }

    private void checkUpdate() {

        mFirmwareDataSource.checkFirmwareUpdate(new BaseOperateDataCallbackWrapper<>(
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                    }
                }, this
        ));

    }


}
