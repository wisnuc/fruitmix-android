package com.winsun.fruitmix.file.data.model;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/8/9.
 */

public abstract class AbstractFile {

    private String name;

    private long time;

    private int fileTypeResID;

    private long size;

    public abstract boolean isFolder();

    public String getName() {
        return name != null ? name : "";
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }


    public String getDateText() {

        return time != 0 ? Util.formatDate(getTime()) : "";

    }

    public String getTimeText() {

        return time != 0 ? Util.formatTime(getTime()) : "";

    }

    public String getFormatName(Context context) {

        String fileName = getName();

        StringBuilder name = new StringBuilder();

        if (fileName.length() > 4) {
            name.append(fileName.substring(0, 4));

            name.append(context.getString(R.string.android_ellipsize));
        } else
            name.append(fileName);

        return name.toString();

    }

    public abstract AbstractFile copySelf();

}
