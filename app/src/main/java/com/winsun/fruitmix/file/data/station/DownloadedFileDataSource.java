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

    void insertDownloadedFileRecord(DownloadedItem downloadedItem);

    void deleteDownloadedFileRecord(List<DownloadedItem> downloadedItems,String currentLoginUserUUID);

    void clearDownloadFileRecordInCache();

    void deleteDownloadedFile(String fileName);

}
