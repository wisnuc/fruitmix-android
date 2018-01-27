package com.winsun.fruitmix.file.data.download;

import android.util.Log;

import com.winsun.fruitmix.file.data.download.param.FileDownloadParam;
import com.winsun.fruitmix.file.data.model.FileTaskItem;

import java.util.concurrent.Future;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileDownloadItem extends FileTaskItem {

    public static final String TAG = FileDownloadItem.class.getSimpleName();

    private FileDownloadParam mFileDownloadParam;

    private FileDownloadState fileDownloadState;

    public FileDownloadItem() {
    }

    public FileDownloadItem(String fileName, long fileSize, String fileUUID) {
        super(fileUUID,fileName,fileSize);
    }

    public FileDownloadItem(String fileName, long fileSize, String fileUUID, FileDownloadParam fileDownloadParam) {
        super(fileUUID,fileName,fileSize);
        mFileDownloadParam = fileDownloadParam;
    }

    public void setFileDownloadState(FileDownloadState fileDownloadState) {
        this.fileDownloadState = fileDownloadState;

        fileDownloadState.setFileUUID(getFileUUID());
        fileDownloadState.setFileName(getFileName());
        fileDownloadState.setFileSize(getFileSize());

        fileDownloadState.startWork();

        fileDownloadState.notifyDownloadStateChanged();
    }

    public FileDownloadParam getFileDownloadParam() {
        return mFileDownloadParam;
    }


    @Override
    public TaskState getTaskState() {
        return fileDownloadState.getDownloadState();
    }

    @Override
    public String getUnionKey() {
        return getFileUUID();
    }

    public FileDownloadState getFileDownloadState() {
        return fileDownloadState;
    }


}
