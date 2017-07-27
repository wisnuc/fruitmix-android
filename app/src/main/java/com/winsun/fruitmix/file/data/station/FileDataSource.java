package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;

/**
 * Created by Administrator on 2017/7/27.
 */

public interface FileDataSource {

    void getFile(final String folderUUID, String rootUUID, final BaseLoadDataCallback<AbstractRemoteFile> callback);

}
