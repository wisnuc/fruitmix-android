package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.file.data.download.FinishedTaskItem;

import java.util.List;

/**
 * Created by Administrator on 2017/7/28.
 */

public interface DownloadedFileDataSource extends FileTaskDataSource{

    boolean deleteDownloadedFile(String fileName);

}
