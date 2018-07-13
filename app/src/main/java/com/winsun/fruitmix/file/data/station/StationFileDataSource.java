package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/7/27.
 */

public interface StationFileDataSource {

    void getRootDrive(BaseLoadDataCallback<AbstractRemoteFile> callback);

    void getFile(String rootUUID,final String folderUUID, final BaseLoadDataCallback<AbstractRemoteFile> callback);

    OperationResult getFile(String rootUUID,String folderUUID);

    void createFolder(String folderName,String driveUUID,String dirUUID,BaseOperateDataCallback<HttpResponse> callback);


}
