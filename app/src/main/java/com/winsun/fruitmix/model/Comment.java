package com.winsun.fruitmix.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/7/12.
 */
public class Comment {

    private int id;
    private String creator;
    private String time;
    private String formatTime;
    private String shareId;
    private String text;

    public Comment(int id, String creator, String time, String shareId, String formatTime, String text) {
        this.id = id;
        this.creator = creator;
        this.time = time;
        this.shareId = shareId;
        this.formatTime = formatTime;
        this.text = text;
    }

    public Comment() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFormatTime() {
        return formatTime;
    }

    public void setFormatTime(String formatTime) {
        this.formatTime = formatTime;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", creator='" + creator + '\'' +
                ", time='" + time + '\'' +
                ", formatTime='" + formatTime + '\'' +
                ", shareId='" + shareId + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
