package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.download.DownloadedItem;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFolder;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/7/19.
 */

public class StationFileRepositoryImpl implements StationFileRepository {

    private static StationFileRepositoryImpl instance;

    private StationFileDataSource stationFileDataSource;
    private DownloadedFileDataSource downloadedFileDataSource;

    ConcurrentMap<String, AbstractRemoteFile> stationFileMap;

    private String currentFolderUUID;

    boolean cacheDirty = true;

    private StationFileRepositoryImpl(StationFileDataSource stationFileDataSource, DownloadedFileDataSource downloadedFileDataSource) {
        this.stationFileDataSource = stationFileDataSource;
        this.downloadedFileDataSource = downloadedFileDataSource;

        stationFileMap = new ConcurrentHashMap<>();
    }

    public static StationFileRepositoryImpl getInstance(StationFileDataSource stationFileDataSource, DownloadedFileDataSource downloadedFileDataSource) {
        if (instance == null)
            instance = new StationFileRepositoryImpl(stationFileDataSource, downloadedFileDataSource);
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void getFile(final String folderUUID, String rootUUID, final BaseLoadDataCallback<AbstractRemoteFile> callback) {

        if (currentFolderUUID != null && !currentFolderUUID.equals(folderUUID))
            cacheDirty = true;

        if (!cacheDirty) {
            callback.onSucceed(new ArrayList<>(stationFileMap.values()), new OperationSuccess());
            return;
        }

        stationFileDataSource.getFile(folderUUID, rootUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {

            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                currentFolderUUID = folderUUID;

                for (AbstractRemoteFile file : data) {
                    file.setParentFolderUUID(folderUUID);
                }

                stationFileMap.clear();

                AbstractRemoteFile remoteFolder = new RemoteFolder();
                remoteFolder.setUuid(folderUUID);
                remoteFolder.initChildAbstractRemoteFileList(data);

                stationFileMap.put(folderUUID, remoteFolder);

                callback.onSucceed(data, operationResult);

                cacheDirty = false;

            }

            @Override
            public void onFail(OperationResult operationResult) {
                super.onFail(operationResult);

                currentFolderUUID = folderUUID;

                callback.onFail(operationResult);

                cacheDirty = false;
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
    public void deleteDownloadedFile(String fileName) {

        downloadedFileDataSource.deleteDownloadedFile(fileName);

    }

    @Override
    public void insertDownloadedFileRecord(DownloadedItem downloadedItem) {

        downloadedFileDataSource.insertDownloadedFileRecord(downloadedItem);

    }

    @Override
    public void deleteDownloadedFileRecord(List<DownloadedItem> downloadedItems, String currentLoginUserUUID) {

        downloadedFileDataSource.deleteDownloadedFileRecord(downloadedItems, currentLoginUserUUID);

    }

    @Override
    public void clearDownloadFileRecordInCache() {

        downloadedFileDataSource.clearDownloadFileRecordInCache();

    }
}
