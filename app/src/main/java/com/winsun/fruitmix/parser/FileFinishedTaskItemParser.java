package com.winsun.fruitmix.parser;

import android.database.Cursor;

import com.winsun.fruitmix.db.DBHelper;
import com.winsun.fruitmix.file.data.download.FinishedTaskItem;
import com.winsun.fruitmix.file.data.model.FileTaskItem;

/**
 * Created by Administrator on 2017/11/17.
 */

public class FileFinishedTaskItemParser {

    public FinishedTaskItem fillFileTaskItem(FileTaskItem fileTaskItem, Cursor cursor,String currentUserUUID){

        String fileName = cursor.getString(cursor.getColumnIndex(DBHelper.FILE_KEY_NAME));
        String fileUUID = cursor.getString(cursor.getColumnIndex(DBHelper.FILE_KEY_UUID));
        long fileSize = cursor.getLong(cursor.getColumnIndex(DBHelper.FILE_KEY_SIZE));
        long fileTime = cursor.getLong(cursor.getColumnIndex(DBHelper.FILE_KEY_TIME));

        String fileCreatorUUID;
        if (cursor.isNull(cursor.getColumnIndex(DBHelper.FILE_KEY_CREATOR_UUID))) {
            fileCreatorUUID = currentUserUUID;
        } else {
            fileCreatorUUID = cursor.getString(cursor.getColumnIndex(DBHelper.FILE_KEY_CREATOR_UUID));
        }

        fileTaskItem.setFileName(fileName);
        fileTaskItem.setFileUUID(fileUUID);
        fileTaskItem.setFileSize(fileSize);

        FinishedTaskItem finishedTaskItem = new FinishedTaskItem(fileTaskItem);

        finishedTaskItem.setFileCreatorUUID(fileCreatorUUID);
        finishedTaskItem.setFileTime(fileTime);

        return finishedTaskItem;
    }

}
