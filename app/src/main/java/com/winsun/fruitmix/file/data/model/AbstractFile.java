package com.winsun.fruitmix.file.data.model;

import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/8/9.
 */

public abstract class AbstractFile {

    private String name;

    private long time;

    private int fileTypeResID;

    public abstract boolean isFolder();

    public String getName() {
        return name;
    }

    public int getFileTypeResID() {
        return fileTypeResID;
    }

    public void setFileTypeResID(int fileTypeResID) {
        this.fileTypeResID = fileTypeResID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDateText() {

        return Util.formatDate(getTime());

    }

    public String getTimeText() {

        return Util.formatTime(getTime());

    }


}
