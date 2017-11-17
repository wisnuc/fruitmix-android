package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.file.data.download.FinishedTaskItem;

import java.util.List;

/**
 * Created by Administrator on 2017/11/17.
 */

public class UploadFileDataSourceImpl implements UploadFileDataSource {

    private DBUtils dbUtils;

    public UploadFileDataSourceImpl(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public List<FinishedTaskItem> getCurrentLoginUserFileFinishedTaskItem(String currentLoginUserUUID) {

        return dbUtils.getAllCurrentLoginUserUploadedFile(currentLoginUserUUID);

    }

    @Override
    public boolean insertFileTask(FinishedTaskItem finishedTaskItem) {

        return dbUtils.insertFileUploadTaskItem(finishedTaskItem) > 0;
    }

    @Override
    public boolean deleteFileTask(String fileUUID, String currentLoginUserUUID) {

        return dbUtils.deleteFileUploadTaskByUUIDAndCreatorUUID(fileUUID, currentLoginUserUUID) > 0;

    }

}
