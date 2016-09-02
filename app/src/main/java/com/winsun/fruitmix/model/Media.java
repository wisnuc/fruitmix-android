package com.winsun.fruitmix.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/7/28.
 */
public class Media implements Parcelable {

    private String uuid;
    private String thumb;
    private String time;
    private String width;
    private String height;
    private boolean selected;
    private boolean local;
    private String title;

    public Media() {

    }

    protected Media(Parcel in) {
        uuid = in.readString();
        thumb = in.readString();
        time = in.readString();
        width = in.readString();
        height = in.readString();
        selected = in.readByte() != 0;
        local = in.readByte() != 0;
        title = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(thumb);
        dest.writeString(time);
        dest.writeString(width);
        dest.writeString(height);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeByte((byte) (local ? 1 : 0));
        dest.writeString(title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
