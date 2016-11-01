package com.winsun.fruitmix.mediaModule.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/7/12.
 */
public class Comment implements Parcelable {

    private long id;
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

    protected Comment(Parcel in) {
        id = in.readLong();
        creator = in.readString();
        time = in.readString();
        formatTime = in.readString();
        shareId = in.readString();
        text = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(creator);
        dest.writeString(time);
        dest.writeString(formatTime);
        dest.writeString(shareId);
        dest.writeString(text);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
