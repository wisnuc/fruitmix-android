package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.file.data.download.DownloadedItem;

import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;

import java.util.List;

/**
 * Created by Administrator on 2017/7/28.
 */

public interface DownloadedFileDataSource {

    List<DownloadedItem> getCurrentLoginUserDownloadedFileRecord(String currentLoginUserUUID);

    boolean insertDownloadedFileRecord(DownloadedItem downloadedItem);

    void deleteDownloadedFileRecord(String fileUUID,String currentLoginUserUUID);

    void clearDownloadFileRecordInCache();

    boolean deleteDownloadedFile(String fileName);

}
