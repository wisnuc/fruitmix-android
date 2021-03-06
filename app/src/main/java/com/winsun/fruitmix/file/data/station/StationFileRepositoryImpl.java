package com.winsun.fruitmix.file.data.station;

import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.FinishedTaskItemWrapper;
import com.winsun.fruitmix.file.data.model.FileTaskManager;
import com.winsun.fruitmix.file.data.download.FinishedTaskItem;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.upload.FileUploadItem;
import com.winsun.fruitmix.file.data.upload.FileUploadState;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithFile;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
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
    private DownloadedFileDataSource downloadedFileDataSource;

    private UploadFileDataSource uploadFileDataSource;

    List<AbstractRemoteFile> stationFiles;

    private String currentFolderUUID;

    boolean cacheDirty = true;

    private FileTaskManager fileTaskManager;

    private StationFileRepositoryImpl(FileTaskManager fileTaskManager, StationFileDataSource stationFileDataSource,
                                      DownloadedFileDataSource downloadedFileDataSource,
                                      UploadFileDataSource uploadFileDataSource, ThreadManager threadManager) {
        super(threadManager);
        this.stationFileDataSource = stationFileDataSource;
        this.downloadedFileDataSource = downloadedFileDataSource;
        this.uploadFileDataSource = uploadFileDataSource;
        this.fileTaskManager = fileTaskManager;

        stationFiles = new ArrayList<>();

    }

    public static StationFileRepositoryImpl getInstance(FileTaskManager fileTaskManager, StationFileDataSource stationFileDataSource,
                                                        DownloadedFileDataSource downloadedFileDataSource,
                                                        UploadFileDataSource uploadFileDataSource, ThreadManager threadManager) {
        if (instance == null)
            instance = new StationFileRepositoryImpl(fileTaskManager, stationFileDataSource,
                    downloadedFileDataSource, uploadFileDataSource, threadManager);
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

    public void getFile(final String rootUUID, final String folderUUID, final BaseLoadDataCallback<AbstractRemoteFile> callback) {

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

                handleGeneralFolder(rootUUID, folderUUID, runOnMainThreadCallback);

            }

        });

    }

    @Override
    public OperationResult getFileWithoutCreateNewThread(String rootUUID, final String folderUUID) {

        OperationResult result = stationFileDataSource.getFile(rootUUID, folderUUID);

        if (result instanceof OperationSuccessWithFile)
            handleGetFileSucceed(((OperationSuccessWithFile) result).getList(), folderUUID);

        return result;

    }

    private void handleGeneralFolder(final String rootUUID, final String folderUUID, final BaseLoadDataCallback<AbstractRemoteFile> runOnMainThreadCallback) {
        stationFileDataSource.getFile(rootUUID, folderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {

            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                handleGetFileSucceed(data, folderUUID);

                runOnMainThreadCallback.onSucceed(data, operationResult);

            }

            @Override
            public void onFail(OperationResult operationResult) {
                super.onFail(operationResult);

                handleGetFileFail(folderUUID);

                runOnMainThreadCallback.onFail(operationResult);

            }
        });
    }

    private void handleGetFileFail(String folderUUID) {
        currentFolderUUID = folderUUID;

        cacheDirty = false;
    }

    private void handleGetFileSucceed(List<AbstractRemoteFile> data, String folderUUID) {
        currentFolderUUID = folderUUID;

        for (AbstractRemoteFile file : data) {
            file.setParentFolderUUID(folderUUID);
        }

        stationFiles.clear();

        stationFiles.addAll(data);

        cacheDirty = false;

    }

    @Override
    public void downloadFile(final String currentUserUUID, final FileDownloadState fileDownloadState, final BaseOperateDataCallback<FileDownloadItem> callback) throws MalformedURLException, IOException, SocketTimeoutException {

        stationFileDataSource.downloadFile(fileDownloadState, new BaseOperateDataCallback<FileDownloadItem>() {
            @Override
            public void onSucceed(FileDownloadItem data, OperationResult result) {

                FinishedTaskItem finishedTaskItem = new FinishedTaskItem(data);

                finishedTaskItem.setFileCreatorUUID(currentUserUUID);

                boolean insertResult = downloadedFileDataSource.insertFileTask(finishedTaskItem);

                Log.d(TAG, "onSucceed: insertFileDownloadFinishTask result: " + insertResult);

                callback.onSucceed(fileDownloadState.getFileDownloadItem(), new OperationSuccess());

            }

            @Override
            public void onFail(OperationResult result) {
                callback.onFail(result);
            }
        });

    }

    void setCacheDirty() {
        cacheDirty = true;
    }


    @Override
    public void fillAllFinishTaskItemIntoFileTaskManager(String currentLoginUserUUID) {

        List<FinishedTaskItem> finishedTaskItems = new ArrayList<>();

        List<FinishedTaskItem> finishedDownloadTaskItems = downloadedFileDataSource.getCurrentLoginUserFileFinishedTaskItem(currentLoginUserUUID);

        finishedTaskItems.addAll(finishedDownloadTaskItems);

        List<FinishedTaskItem> finishUploadTaskItems = uploadFileDataSource.getCurrentLoginUserFileFinishedTaskItem(currentLoginUserUUID);

        finishedTaskItems.addAll(finishUploadTaskItems);

        for (FinishedTaskItem finishedTaskItem : finishedTaskItems) {
            fileTaskManager.addFinishedFileTaskItem(finishedTaskItem.getFileTaskItem());
        }

    }

    @Override
    public void clearAllFileTaskItemInCache() {

        fileTaskManager.clearFileTaskItems();

    }

    @Override
    public void deleteFileFinishedTaskItems(final Collection<FinishedTaskItemWrapper> finishedTaskItemWrappers, final String currentLoginUserUUID, BaseOperateDataCallback<Void> callback) {

        final BaseOperateDataCallback<Void> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                deleteFileFinishedTaskItemsInThread(finishedTaskItemWrappers, currentLoginUserUUID, runOnMainThreadCallback);
            }
        });

    }

    private void deleteFileFinishedTaskItemsInThread(Collection<FinishedTaskItemWrapper> finishedTaskItemWrappers, String currentLoginUserUUID, BaseOperateDataCallback<Void> callback) {
        boolean result = false;

        for (FinishedTaskItemWrapper finishedTaskItemWrapper : finishedTaskItemWrappers) {

            boolean deleteUploadFileTaskResult = uploadFileDataSource.deleteFileTask(finishedTaskItemWrapper.getFileUUID(), currentLoginUserUUID);

            if (!deleteUploadFileTaskResult) {

                //delete upload file task return false,means no record found,delete it in download record table

                result = downloadedFileDataSource.deleteDownloadedFile(finishedTaskItemWrapper.getFileName());

                if (result) {
                    downloadedFileDataSource.deleteFileTask(finishedTaskItemWrapper.getFileUUID(), currentLoginUserUUID);
                }

            } else
                result = true;

            if (result)
                fileTaskManager.deleteFileTaskItem(Collections.singletonList(finishedTaskItemWrapper));

        }

        if (result)
            callback.onSucceed(null, new OperationSuccess());
        else
            callback.onFail(new OperationIOException());
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

    @Override
    public OperationResult uploadFile(LocalFile file, String driveUUID, String dirUUID) {

        return stationFileDataSource.uploadFile(file, driveUUID, dirUUID);

    }

    @Override
    public OperationResult uploadFileWithProgress(LocalFile file, FileUploadState fileUploadState, String driveUUID, String dirUUID, String currentLoginUserUUID) {

        return stationFileDataSource.uploadFileWithProgress(file, fileUploadState, driveUUID, dirUUID);

    }

    @Override
    public boolean insertFileUploadTask(FileUploadItem fileUploadItem, String currentUserUUID) {
        FinishedTaskItem finishedTaskItem = new FinishedTaskItem(fileUploadItem);

        finishedTaskItem.setFileCreatorUUID(currentUserUUID);

        boolean insertResult = uploadFileDataSource.insertFileTask(finishedTaskItem);

        Log.d(TAG, "onSucceed: insertFileUploadFinishTask result: " + insertResult);

        return insertResult;
    }
}
