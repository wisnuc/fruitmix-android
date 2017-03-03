package com.winsun.fruitmix.mediaModule.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/7/28.
 */
public class Media implements Parcelable {

    private static final String TAG = Media.class.getSimpleName();

    private String uuid;
    private String thumb;
    private String time;
    private String width;
    private String height;
    private boolean selected;
    private boolean local;
    private String date;
    private boolean loaded;
    private String belongingMediaShareUUID;
    private boolean uploaded;
    private boolean sharing;
    private int orientationNumber;
    private String type;

    public Media() {
        uuid = "";
        orientationNumber = 1;
        belongingMediaShareUUID = "";
        width = "200";
        height = "200";
        thumb = "";
        type = "JPEG";
        date = "";
        belongingMediaShareUUID = "";
        local = false;
    }

    protected Media(Parcel in) {
        uuid = in.readString();
        thumb = in.readString();
        time = in.readString();
        width = in.readString();
        height = in.readString();
        selected = in.readByte() != 0;
        local = in.readByte() != 0;
        date = in.readString();
        loaded = in.readByte() != 0;
        belongingMediaShareUUID = in.readString();
        uploaded = in.readByte() != 0;
        orientationNumber = in.readInt();
        type = in.readString();
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
        dest.writeString(date);
        dest.writeByte((byte) (loaded ? 1 : 0));
        dest.writeString(belongingMediaShareUUID);
        dest.writeByte((byte) (uploaded ? 1 : 0));
        dest.writeInt(orientationNumber);
        dest.writeString(type);
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
        return uuid == null ? "" : uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTime() {
        return time == null ? "" : time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWidth() {
        return width == null ? "" : width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height == null ? "" : height;
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

    public String getDate() {
        return date == null ? "" : date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getThumb() {
        return thumb == null ? "" : thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getBelongingMediaShareUUID() {
        return belongingMediaShareUUID == null ? "" : belongingMediaShareUUID;
    }

    public void setBelongingMediaShareUUID(String belongingMediaShareUUID) {
        this.belongingMediaShareUUID = belongingMediaShareUUID;
    }

    public void restoreUploadState() {
        uploaded = false;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean isSharing() {
        return sharing;
    }

    public void setSharing(boolean sharing) {
        this.sharing = sharing;
    }

    public int getOrientationNumber() {
        return orientationNumber;
    }

    public void setOrientationNumber(int orientationNumber) {
        this.orientationNumber = orientationNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Media cloneSelf() {

        Media media = new Media();
        media.setUuid(getUuid());
        media.setThumb(getThumb());
        media.setTime(getTime());
        media.setWidth(getWidth());
        media.setHeight(getHeight());
        media.setSelected(isSelected());
        media.setLocal(isLocal());
        media.setDate(getDate());
        media.setLoaded(isLoaded());
        media.setBelongingMediaShareUUID(getBelongingMediaShareUUID());
        media.setUploaded(isUploaded());
        media.setSharing(isSharing());
        media.setOrientationNumber(getOrientationNumber());
        media.setType(getType());

        return media;
    }

    public String getKey() {
        if (isLocal())
            return getThumb();
        else
            return getUuid();
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;

        if(o instanceof Media){
            return ((Media) o).getUuid().equals(this.getUuid());
        }

        return super.equals(o);
    }
}
