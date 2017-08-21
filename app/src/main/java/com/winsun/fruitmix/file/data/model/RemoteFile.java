package com.winsun.fruitmix.file.data.model;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.FileUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/25.
 */

public class RemoteFile extends AbstractRemoteFile {

    private String fileHash;

    public RemoteFile() {

        setFileTypeResID(R.drawable.file_icon);
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    public boolean openAbstractRemoteFile(Context context,String rootUUID) {

        return FileUtil.openAbstractRemoteFile(context, getName());

    }

    @Override
    public String getTimeDateText() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss上传").format(new Date(Long.parseLong(getTime())));
    }

}
