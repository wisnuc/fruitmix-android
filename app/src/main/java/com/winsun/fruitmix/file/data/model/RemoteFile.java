package com.winsun.fruitmix.file.data.model;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    public void setName(String name) {
        super.setName(name);

        setFileTypeResID(FileUtil.getFileTypeResID(getName()));
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public AbstractFile copySelf() {

        RemoteFile file = new RemoteFile();

        file.setName(getName());
        file.setFileTypeResID(getFileTypeResID());
        file.setSize(getSize());
        file.setTime(getTime());
        file.setFileHash(getFileHash());
        file.setUuid(getUuid());
        file.setParentFolderName(getParentFolderName());
        file.setParentFolderPath(getParentFolderPath());
        file.setParentFolderUUID(getParentFolderUUID());
        file.setRootFolderUUID(getRootFolderUUID());

        return file;

    }
}
