package com.winsun.fruitmix.fileModule.model;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;
import com.winsun.fruitmix.util.Util;

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

        File file = new File(FileUtil.getDownloadFileStoreFolderPath(), getName());

        try {
            FileUtil.openFile(context, file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public void downloadFile(Context context) {

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.DOWNLOAD_FILE.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_FILE.name());
        intent.putExtra(Util.FILE_UUID, getUuid());
        intent.putExtra(Util.FILE_NAME, getName());
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public boolean checkIsDownloaded() {
        return new File(FileUtil.getDownloadFileStoreFolderPath(),getName()).exists();
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
