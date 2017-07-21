package com.winsun.fruitmix.file.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.fileModule.model.RemoteFolder;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/7/19.
 */

public class FileRepository {

    private static FileRepository instance;

    private StationFileDataSource stationFileDataSource;

    ConcurrentMap<String, AbstractRemoteFile> stationFileMap;

    boolean cacheDirty = true;

    private FileRepository(StationFileDataSource stationFileDataSource) {
        this.stationFileDataSource = stationFileDataSource;

        stationFileMap = new ConcurrentHashMap<>();
    }

    public static FileRepository getInstance(StationFileDataSource stationFileDataSource) {
        if (instance == null)
            instance = new FileRepository(stationFileDataSource);
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void getFile(final String folderUUID, String rootUUID, final BaseLoadDataCallback<AbstractRemoteFile> callback) {

        if (!cacheDirty) {
            callback.onSucceed(new ArrayList<>(stationFileMap.values()), new OperationSuccess());
            return;
        }

        stationFileDataSource.getFile(folderUUID, rootUUID, new BaseLoadDataCallbackImpl<AbstractRemoteFile>() {

            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);


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

                callback.onFail(operationResult);

                cacheDirty = false;
            }
        });

    }

    public void setCacheDirty() {
        cacheDirty = true;
    }
}
