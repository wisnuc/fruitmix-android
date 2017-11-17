package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.file.data.download.FinishedTaskItem;

import java.util.List;

/**
 * Created by Administrator on 2017/7/28.
 */

public interface DownloadedFileDataSource {

    List<FinishedTaskItem> getCurrentLoginUserDownloadedFileRecord(String currentLoginUserUUID);

    boolean insertDownloadedFileRecord(FinishedTaskItem finishedTaskItem);

    void deleteDownloadedFileRecord(String fileUUID,String currentLoginUserUUID);

    void clearDownloadFileRecordInCache();

    boolean deleteDownloadedFile(String fileName);

}
