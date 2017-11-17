package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.file.data.download.FinishedTaskItem;
import com.winsun.fruitmix.util.FileUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/28.
 */

public class DownloadFileDataSourceImpl implements DownloadedFileDataSource {

    private DBUtils dbUtils;

    private static DownloadedFileDataSource instance;

    public static DownloadedFileDataSource getInstance(DBUtils dbUtils) {

        if (instance == null)
            instance = new DownloadFileDataSourceImpl(dbUtils);

        return instance;
    }

    private DownloadFileDataSourceImpl(DBUtils dbUtils) {

        this.dbUtils = dbUtils;
    }

    @Override
    public List<FinishedTaskItem> getCurrentLoginUserFileFinishedTaskItem(String currentLoginUserUUID) {

        List<FinishedTaskItem> downloadItems = dbUtils.getAllCurrentLoginUserDownloadedFile(currentLoginUserUUID);

        String[] fileNames = new File(FileUtil.getDownloadFileStoreFolderPath()).list();

        if (fileNames != null && fileNames.length != 0) {

            List<String> fileNameList = Arrays.asList(fileNames);

            Iterator<FinishedTaskItem> itemIterator = downloadItems.iterator();
            while (itemIterator.hasNext()) {
                FinishedTaskItem finishedTaskItem = itemIterator.next();

                if (!fileNameList.contains(finishedTaskItem.getFileName())) {
                    itemIterator.remove();
                    dbUtils.deleteDownloadedFileByUUID(finishedTaskItem.getFileUUID());
                }
            }

        }

        return downloadItems;
    }

    @Override
    public boolean insertFileTask(FinishedTaskItem finishedTaskItem) {

        return dbUtils.insertDownloadedFile(finishedTaskItem) > 0;

    }

    @Override
    public boolean deleteFileTask(String fileUUID, String currentLoginUserUUID) {

        return dbUtils.deleteFileDownloadedTaskByUUIDAndCreatorUUID(fileUUID, currentLoginUserUUID) > 0;

    }

    @Override
    public boolean deleteDownloadedFile(String fileName) {

        File file = new File(FileUtil.getDownloadFileStoreFolderPath(), fileName);

        if (file.isFile() && file.exists()) {

            return file.delete();

        } else if (!file.exists()) {
            return true;
        }

        return false;
    }
}
