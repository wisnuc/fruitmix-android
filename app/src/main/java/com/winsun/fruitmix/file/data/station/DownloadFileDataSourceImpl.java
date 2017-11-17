package com.winsun.fruitmix.file.data.station;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.file.data.download.FinishedTaskItem;
import com.winsun.fruitmix.file.data.download.FileTaskManager;
import com.winsun.fruitmix.util.FileUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/28.
 */

public class DownloadFileDataSourceImpl implements DownloadedFileDataSource {

    private DBUtils dbUtils;
    private FileTaskManager fileTaskManager;

    private static DownloadedFileDataSource instance;

    public static DownloadedFileDataSource getInstance(DBUtils dbUtils, FileTaskManager fileTaskManager) {

        if (instance == null)
            instance = new DownloadFileDataSourceImpl(dbUtils, fileTaskManager);

        return instance;
    }

    private DownloadFileDataSourceImpl(DBUtils dbUtils, FileTaskManager fileTaskManager) {

        this.dbUtils = dbUtils;

        this.fileTaskManager = fileTaskManager;
    }

    @Override
    public List<FinishedTaskItem> getCurrentLoginUserDownloadedFileRecord(String currentLoginUserUUID) {

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

        for (FinishedTaskItem finishedTaskItem : downloadItems) {
            fileTaskManager.addDownloadedFile(finishedTaskItem.getFileTaskItem());
        }

        return downloadItems;
    }

    @Override
    public boolean insertDownloadedFileRecord(FinishedTaskItem finishedTaskItem) {

        return dbUtils.insertDownloadedFile(finishedTaskItem) > 0;

    }

    @Override
    public void deleteDownloadedFileRecord(String fileUUID, String currentLoginUserUUID) {

        dbUtils.deleteDownloadedFileByUUIDAndCreatorUUID(fileUUID, currentLoginUserUUID);

        fileTaskManager.deleteFileDownloadItem(Collections.singletonList(fileUUID));
    }

    @Override
    public void clearDownloadFileRecordInCache() {

        fileTaskManager.clearFileDownloadItems();

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
