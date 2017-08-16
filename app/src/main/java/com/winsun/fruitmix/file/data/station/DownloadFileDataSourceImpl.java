package com.winsun.fruitmix.file.data.station;

import android.content.Context;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.file.data.download.DownloadedItem;
import com.winsun.fruitmix.file.data.download.FileDownloadManager;
import com.winsun.fruitmix.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/28.
 */

public class DownloadFileDataSourceImpl implements DownloadedFileDataSource {

    private DBUtils dbUtils;
    private FileDownloadManager fileDownloadManager;

    private static DownloadedFileDataSource instance;

    public static DownloadedFileDataSource getInstance(Context context, FileDownloadManager fileDownloadManager) {

        if (instance == null)
            instance = new DownloadFileDataSourceImpl(context, fileDownloadManager);

        return instance;
    }

    private DownloadFileDataSourceImpl(Context context, FileDownloadManager fileDownloadManager) {

        dbUtils = DBUtils.getInstance(context);

        this.fileDownloadManager = fileDownloadManager;
    }

    @Override
    public List<DownloadedItem> getCurrentLoginUserDownloadedFileRecord(String currentLoginUserUUID) {

        List<DownloadedItem> downloadItems = dbUtils.getAllCurrentLoginUserDownloadedFile(currentLoginUserUUID);

        String[] fileNames = new File(FileUtil.getDownloadFileStoreFolderPath()).list();

        if (fileNames != null && fileNames.length != 0) {

            List<String> fileNameList = Arrays.asList(fileNames);

            Iterator<DownloadedItem> itemIterator = downloadItems.iterator();
            while (itemIterator.hasNext()) {
                DownloadedItem downloadedItem = itemIterator.next();

                if (!fileNameList.contains(downloadedItem.getFileName())) {
                    itemIterator.remove();
                    dbUtils.deleteDownloadedFileByUUID(downloadedItem.getFileUUID());
                }
            }

        }

        for (DownloadedItem downloadedItem : downloadItems) {
            fileDownloadManager.addDownloadedFile(downloadedItem.getFileDownloadItem());
        }

        return downloadItems;
    }

    @Override
    public boolean insertDownloadedFileRecord(DownloadedItem downloadedItem) {

        return dbUtils.insertDownloadedFile(downloadedItem) > 0;

    }

    @Override
    public void deleteDownloadedFileRecord(String fileUUID, String currentLoginUserUUID) {

        dbUtils.deleteDownloadedFileByUUIDAndCreatorUUID(fileUUID, currentLoginUserUUID);

        fileDownloadManager.deleteFileDownloadItem(Collections.singletonList(fileUUID));
    }

    @Override
    public void clearDownloadFileRecordInCache() {

        fileDownloadManager.clearFileDownloadItems();

    }

    @Override
    public boolean deleteDownloadedFile(String fileName) {

        //TODO: delete to real downloaded file

        return true;
    }
}
