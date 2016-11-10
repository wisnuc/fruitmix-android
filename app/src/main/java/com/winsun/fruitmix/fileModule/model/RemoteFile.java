package com.winsun.fruitmix.fileModule.model;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public class RemoteFile extends AbstractRemoteFile {

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public boolean openAbstractRemoteFile(Context context) {

        return FileUtil.openAbstractRemoteFile(context, getName());

    }

    @Override
    public void downloadFile(Context context) {

        FileDownloadItem fileDownloadItem = new FileDownloadItem(getName(), Long.parseLong(getSize()), getUuid());

        FileDownloadManager.INSTANCE.addFileDownloadItem(fileDownloadItem);
    }

    @Override
    public boolean checkIsDownloaded() {
        return new File(FileUtil.getDownloadFileStoreFolderPath(), getName()).exists();
    }

    @Override
    public boolean checkIsAlreadyDownloading() {

        FileDownloadManager fileDownloadManager = FileDownloadManager.INSTANCE;

        List<FileDownloadItem> fileDownloadItems = fileDownloadManager.getFileDownloadItems();

        for (FileDownloadItem fileDownloadItem : fileDownloadItems) {
            if (fileDownloadItem.getFileUUID().equals(getUuid()) && fileDownloadItem.getDownloadState() == DownloadState.DOWNLOADING) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<AbstractRemoteFile> listChildAbstractRemoteFileList() {
        throw new UnsupportedOperationException("File can not call list operation");
    }

    @Override
    public void initChildAbstractRemoteFileList(List<AbstractRemoteFile> abstractRemoteFiles) {
        throw new UnsupportedOperationException("File can not call this operation");
    }

    @Override
    public int getImageResource() {
        return R.drawable.file_icon;
    }

    @Override
    public String getTimeDateText() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss上传").format(new Date(Long.parseLong(getTime())));
    }

}
