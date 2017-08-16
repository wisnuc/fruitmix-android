package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.download.DownloadedFileWrapper;
import com.winsun.fruitmix.file.data.download.DownloadedItem;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/28.
 */

public interface StationFileRepository {

    void getFile(final String folderUUID, String rootUUID, final BaseLoadDataCallback<AbstractRemoteFile> callback);

    void downloadFile(String currentUserUUID, FileDownloadState fileDownloadState, BaseOperateDataCallback<FileDownloadItem> callback) throws MalformedURLException, IOException, SocketTimeoutException;

    List<DownloadedItem> getCurrentLoginUserDownloadedFileRecord(String currentLoginUserUUID);

    void clearDownloadFileRecordInCache();

    void deleteDownloadedFile(Collection<DownloadedFileWrapper> downloadedFileWrappers, String currentLoginUserUUID, BaseOperateDataCallback<Void> callback);


}
