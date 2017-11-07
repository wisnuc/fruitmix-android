package com.winsun.fruitmix.mediaModule.model;

import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/10/29.
 */

public class Video extends Media {

    private String name;
    private long size;
    private long duration;

    public Video() {
        setName("");
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getFormatDuration() {

        return Util.formatDuration(getDuration());

    }

}
