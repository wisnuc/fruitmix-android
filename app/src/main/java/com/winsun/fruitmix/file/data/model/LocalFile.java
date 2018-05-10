package com.winsun.fruitmix.file.data.model;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/10/25.
 */

public class LocalFile extends AbstractLocalFile {

    private String fileHash;

    public LocalFile() {

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

}
