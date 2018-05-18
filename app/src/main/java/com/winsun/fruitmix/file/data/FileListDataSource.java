package com.winsun.fruitmix.file.data;

import android.content.Context;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;

/**
 * Created by Administrator on 2018/2/24.
 */

public interface FileListDataSource {

    void reset(Context context);

    void setCurrentUserUUID(String currentUserUUID);

    String getCurrentFolderName();

    void getFilesInCurrentFolder(Context context, BaseLoadDataCallback<AbstractRemoteFile> callback);

    boolean checkCanGoToUpperLevel();

    void getFilesInParentFolder(Context context,BaseLoadDataCallback<AbstractRemoteFile> callback);

    void getFilesInFolder(Context context,AbstractRemoteFile folder,BaseLoadDataCallback<AbstractRemoteFile> callback);

    FileDownloadParam createFileDownloadParam(AbstractRemoteFile abstractRemoteFile);

}
