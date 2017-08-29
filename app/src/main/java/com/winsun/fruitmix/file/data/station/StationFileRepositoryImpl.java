package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.download.DownloadedFileWrapper;
import com.winsun.fruitmix.file.data.download.DownloadedItem;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFolder;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/7/19.
 */

public class StationFileRepositoryImpl extends BaseDataRepository implements StationFileRepository {

    private static StationFileRepositoryImpl instance;

    private StationFileDataSource stationFileDataSource;
    private DownloadedFileDataSource downloadedFileDataSource;

    List<AbstractRemoteFile> stationFiles;

    private String currentFolderUUID;

    boolean cacheDirty = true;

    private StationFileRepositoryImpl(StationFileDataSource stationFileDataSource, DownloadedFileDataSource downloadedFileDataSource, ThreadManager threadManager) {
        super(threadManager);
        this.stationFileDataSource = stationFileDataSource;
        this.downloadedFileDataSource = downloadedFileDataSource;

        stationFiles = new ArrayList<>();
    }

    public static StationFileRepositoryImpl getInstance(StationFileDataSource stationFileDataSource, DownloadedFileDataSource downloadedFileDataSource, ThreadManager threadManager) {
        if (instance == null)
            instance = new StationFileRepositoryImpl(stationFileDataSource, downloadedFileDataSource, threadManager);
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
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

                stationFileDataSource.getFile(rootUUID, folderUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {

                    @Override
                    public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                        super.onSucceed(data, operationResult);

                        currentFolderUUID = folderUUID;

                        for (AbstractRemoteFile file : data) {
                            file.setParentFolderUUID(folderUUID);
                        }

                        stationFiles.clear();

                        stationFiles.addAll(data);

                        runOnMainThreadCallback.onSucceed(data, operationResult);

                        cacheDirty = false;

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {
                        super.onFail(operationResult);

                        currentFolderUUID = folderUUID;

                        runOnMainThreadCallback.onFail(operationResult);

                        cacheDirty = false;
                    }
                });

            }
        });

    }

    @Override
    public void downloadFile(final String currentUserUUID, FileDownloadState fileDownloadState, final BaseOperateDataCallback<FileDownloadItem> callback) throws MalformedURLException, IOException, SocketTimeoutException {

        stationFileDataSource.downloadFile(fileDownloadState, new BaseOperateDataCallback<FileDownloadItem>() {
            @Override
            public void onSucceed(FileDownloadItem data, OperationResult result) {

                DownloadedItem downloadedItem = new DownloadedItem(data);

                downloadedItem.setFileTime(System.currentTimeMillis());
                downloadedItem.setFileCreatorUUID(currentUserUUID);

                downloadedFileDataSource.insertDownloadedFileRecord(downloadedItem);

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
    public List<DownloadedItem> getCurrentLoginUserDownloadedFileRecord(String currentLoginUserUUID) {

        return downloadedFileDataSource.getCurrentLoginUserDownloadedFileRecord(currentLoginUserUUID);

    }

    @Override
    public void clearDownloadFileRecordInCache() {

        downloadedFileDataSource.clearDownloadFileRecordInCache();

    }

    @Override
    public void deleteDownloadedFile(final Collection<DownloadedFileWrapper> downloadedFileWrappers, final String currentLoginUserUUID, BaseOperateDataCallback<Void> callback) {

        final BaseOperateDataCallback<Void> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                deleteDownloadedFileInThread(downloadedFileWrappers, currentLoginUserUUID, runOnMainThreadCallback);
            }
        });

    }

    private void deleteDownloadedFileInThread(Collection<DownloadedFileWrapper> downloadedFileWrappers, String currentLoginUserUUID, BaseOperateDataCallback<Void> callback) {
        boolean result = false;

        for (DownloadedFileWrapper downloadedFileWrapper : downloadedFileWrappers) {

            result = downloadedFileDataSource.deleteDownloadedFile(downloadedFileWrapper.getFileName());

            if (result)
                downloadedFileDataSource.deleteDownloadedFileRecord(downloadedFileWrapper.getFileUUID(), currentLoginUserUUID);
        }

        if (result)
            callback.onSucceed(null, new OperationSuccess());
        else
            callback.onFail(new OperationSQLException());
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
    public void uploadFile(LocalFile file, String driveUUID, String dirUUID, BaseOperateDataCallback<Boolean> callback) {

        stationFileDataSource.uploadFile(file, driveUUID, dirUUID, callback);

    }
}
