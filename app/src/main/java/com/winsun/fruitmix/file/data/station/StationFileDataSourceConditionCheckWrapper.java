package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.base.data.BaseDataOperator;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.upload.FileUploadState;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

/**
 * Created by Administrator on 2018/3/1.
 */

public class StationFileDataSourceConditionCheckWrapper implements StationFileDataSource {

    private BaseDataOperator mBaseDataOperator;

    private StationFileDataSource mStationFileDataSource;

    public StationFileDataSourceConditionCheckWrapper(BaseDataOperator baseDataOperator, StationFileDataSource stationFileDataSource) {
        mBaseDataOperator = baseDataOperator;
        mStationFileDataSource = stationFileDataSource;
    }

    @Override
    public void getRootDrive(BaseLoadDataCallback<AbstractRemoteFile> callback) {

        mStationFileDataSource.getRootDrive(callback);

    }

    @Override
    public void getFile(String rootUUID, String folderUUID, BaseLoadDataCallback<AbstractRemoteFile> callback) {

        mStationFileDataSource.getFile(rootUUID,folderUUID,callback);

    }

    @Override
    public OperationResult getFile(String rootUUID, String folderUUID) {
        return mStationFileDataSource.getFile(rootUUID,folderUUID);
    }

    @Override
    public void downloadFile(final FileDownloadState fileDownloadState, final BaseOperateDataCallback<FileDownloadItem> callback) throws MalformedURLException, IOException, SocketTimeoutException {

        mBaseDataOperator.preConditionCheck(false,new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                try {
                    mStationFileDataSource.downloadFile(fileDownloadState, new BaseOperateDataCallback<FileDownloadItem>() {

                        @Override
                        public void onSucceed(FileDownloadItem data, OperationResult result) {
                            callback.onSucceed(data,result);
                        }

                        @Override
                        public void onFail(OperationResult operationResult) {

                            if (mBaseDataOperator.needRetryWhenFail(operationResult)){

                                try {
                                    downloadFile(fileDownloadState,callback);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                            else
                                callback.onFail(operationResult);

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFail(OperationResult operationResult) {

                try {
                    mStationFileDataSource.downloadFile(fileDownloadState, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    public void createFolder(String folderName, String driveUUID, String dirUUID, BaseOperateDataCallback<HttpResponse> callback) {

        mStationFileDataSource.createFolder(folderName,driveUUID,dirUUID,callback);

    }

    @Override
    public OperationResult uploadFile(LocalFile file, String driveUUID, String dirUUID) {
        return mStationFileDataSource.uploadFile(file,driveUUID,dirUUID);
    }

    @Override
    public OperationResult uploadFileWithProgress(LocalFile file, FileUploadState fileUploadState, String driveUUID, String dirUUID) {
        return mStationFileDataSource.uploadFileWithProgress(file,fileUploadState,driveUUID,dirUUID);
    }
}
