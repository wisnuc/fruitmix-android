package com.winsun.fruitmix.firmware.data;

import com.winsun.fruitmix.model.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.firmware.model.Firmware;
import com.winsun.fruitmix.thread.manage.ThreadManager;

/**
 * Created by Administrator on 2017/12/28.
 */

public class FirmwareRepository extends BaseDataRepository implements FirmwareDataSource {

    private FirmwareDataSource mFirmwareDataSource;

    public FirmwareRepository(ThreadManager threadManager, FirmwareDataSource firmwareDataSource) {
        super(threadManager);
        mFirmwareDataSource = firmwareDataSource;
    }

    @Override
    public void installFirmware(final String versionName, final BaseOperateDataCallback<Void> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mFirmwareDataSource.installFirmware(versionName, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void updateFirmwareState(final String state, final BaseOperateDataCallback<Void> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mFirmwareDataSource.updateFirmwareState(state, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void updateDownloadFirmwareState(final String versionName, final String state, final BaseOperateDataCallback<Void> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mFirmwareDataSource.updateDownloadFirmwareState(versionName, state, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void checkFirmwareUpdate(final BaseOperateDataCallback<Void> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mFirmwareDataSource.checkFirmwareUpdate(createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void getFirmware(final BaseLoadDataCallback<Firmware> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mFirmwareDataSource.getFirmware(createLoadCallbackRunOnMainThread(callback));
            }
        });

    }
}
