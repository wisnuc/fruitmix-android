package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
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
import java.util.List;

/**
 * Created by Administrator on 2017/7/27.
 */

public interface StationFileDataSource {

    void getRootDrive(BaseLoadDataCallback<AbstractRemoteFile> callback);

    void getFile(String rootUUID,final String folderUUID, final BaseLoadDataCallback<AbstractRemoteFile> callback);

    OperationResult getFile(String rootUUID,String folderUUID);

    void downloadFile(FileDownloadState fileDownloadState, BaseOperateDataCallback<FileDownloadItem> callback) throws MalformedURLException, IOException, SocketTimeoutException;

    void createFolder(String folderName,String driveUUID,String dirUUID,BaseOperateDataCallback<HttpResponse> callback);

    OperationResult uploadFile(LocalFile file, String driveUUID, String dirUUID);

    OperationResult uploadFileWithProgress(LocalFile file, FileUploadState fileUploadState, String driveUUID, String dirUUID);

}
