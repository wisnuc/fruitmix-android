package com.winsun.fruitmix.file.data.model;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/10/25.
 */

public class LocalFile extends AbstractLocalFile {

    private String fileHash;
    private String size;

    public LocalFile() {

        setFileTypeResID(R.drawable.file_icon);

    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

}
