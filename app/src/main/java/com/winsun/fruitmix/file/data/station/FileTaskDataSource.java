package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.file.data.download.FinishedTaskItem;

import java.util.List;

/**
 * Created by Administrator on 2017/11/17.
 */

public interface FileTaskDataSource {

    List<FinishedTaskItem> getCurrentLoginUserFileFinishedTaskItem(String currentLoginUserUUID);

    boolean insertFileTask(FinishedTaskItem finishedTaskItem);

    boolean deleteFileTask(String fileUUID,String currentLoginUserUUID);

}
