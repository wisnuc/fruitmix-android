package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/7/28.
 */

public interface StationFileRepository {

    void getRootDrive(BaseLoadDataCallback<AbstractRemoteFile> callback);

    void getFile(String rootUUID, final String folderUUID,String folderName, final BaseLoadDataCallback<AbstractRemoteFile> callback);

    OperationResult getFileWithoutCreateNewThread(String rootUUID, final String folderUUID,String folderName);

    void createFolder(String folderName, String driveUUID, String dirUUID, BaseOperateDataCallback<HttpResponse> callback);

    void createFolderWithoutCreateNewThread(String folderName, String driveUUID, String dirUUID, BaseOperateDataCallback<HttpResponse> callback);

}
