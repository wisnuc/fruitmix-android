package com.winsun.fruitmix.file.data.station;

import android.util.Log;

import com.winsun.fruitmix.model.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithFile;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/7/19.
 */

public class StationFileRepositoryImpl extends BaseDataRepository implements StationFileRepository {

    public static final String TAG = StationFileRepositoryImpl.class.getSimpleName();

    private static StationFileRepositoryImpl instance;

    private StationFileDataSource stationFileDataSource;

    boolean cacheDirty = true;

    private StationFileRepositoryImpl(StationFileDataSource stationFileDataSource, ThreadManager threadManager) {
        super(threadManager);
        this.stationFileDataSource = stationFileDataSource;

    }

    public static StationFileRepositoryImpl getInstance(StationFileDataSource stationFileDataSource, ThreadManager threadManager) {
        if (instance == null)
            instance = new StationFileRepositoryImpl(stationFileDataSource, threadManager);
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    @Override
    public void getRootDrive(final BaseLoadDataCallback<AbstractRemoteFile> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                stationFileDataSource.getRootDrive(createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    public void getFile(final String rootUUID, final String folderUUID, final String fileName, final BaseLoadDataCallback<AbstractRemoteFile> callback) {

/*        if (currentFolderUUID != null && !currentFolderUUID.equals(folderUUID))
            cacheDirty = true;

        if (!cacheDirty) {
            callback.onSucceed(new ArrayList<>(stationFiles), new OperationSuccess());
            return;
        }*/

        final BaseLoadDataCallback<AbstractRemoteFile> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                handleGeneralFolder(rootUUID, folderUUID, fileName, runOnMainThreadCallback);

            }

        });

    }

    @Override
    public OperationResult getFileWithoutCreateNewThread(String rootUUID, final String folderUUID, String folderName) {

        OperationResult result = stationFileDataSource.getFile(rootUUID, folderUUID);

        if (result instanceof OperationSuccessWithFile)
            handleGetFileSucceed(((OperationSuccessWithFile) result).getList(), rootUUID, folderUUID, folderName);

        return result;

    }

    private void handleGeneralFolder(final String rootUUID, final String folderUUID, final String folderName, final BaseLoadDataCallback<AbstractRemoteFile> runOnMainThreadCallback) {
        stationFileDataSource.getFile(rootUUID, folderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {

            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                handleGetFileSucceed(data, rootUUID, folderUUID, folderName);

                runOnMainThreadCallback.onSucceed(data, operationResult);

            }

            @Override
            public void onFail(OperationResult operationResult) {
                super.onFail(operationResult);

                handleGetFileFail();

                runOnMainThreadCallback.onFail(operationResult);

            }
        });
    }

    private void handleGetFileFail() {

        cacheDirty = false;
    }

    private void handleGetFileSucceed(List<AbstractRemoteFile> data, String rootUUID, String folderUUID, String folderName) {

        for (AbstractRemoteFile file : data) {
            file.setParentFolderUUID(folderUUID);
            file.setParentFolderName(folderName);
            file.setRootFolderUUID(rootUUID);
        }

        cacheDirty = false;

    }

    @Override
    public void createFolder(final String folderName, final String driveUUID, final String dirUUID, BaseOperateDataCallback<HttpResponse> callback) {

        final BaseOperateDataCallback<HttpResponse> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                stationFileDataSource.createFolder(folderName, driveUUID, dirUUID, runOnMainThreadCallback);
            }
        });

    }

    @Override
    public void createFolderWithoutCreateNewThread(String folderName, String driveUUID, String dirUUID, BaseOperateDataCallback<HttpResponse> callback) {
        stationFileDataSource.createFolder(folderName, driveUUID, dirUUID, callback);
    }

}
